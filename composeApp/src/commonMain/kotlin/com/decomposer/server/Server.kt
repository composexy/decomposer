package com.decomposer.server

import com.decomposer.runtime.Command
import com.decomposer.runtime.CommandKeys
import com.decomposer.runtime.connection.ConnectionContract
import com.decomposer.runtime.connection.model.CompositionRoots
import com.decomposer.runtime.connection.model.DeviceType
import com.decomposer.runtime.connection.model.ProjectSnapshot
import com.decomposer.runtime.connection.model.SessionData
import com.decomposer.runtime.connection.model.VirtualFileIr
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.KotlinxWebsocketSerializationConverter
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.install
import io.ktor.server.engine.EmbeddedServer
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.response.respond
import io.ktor.server.routing.RoutingContext
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import io.ktor.server.websocket.DefaultWebSocketServerSession
import io.ktor.server.websocket.WebSockets
import io.ktor.server.websocket.pingPeriod
import io.ktor.server.websocket.receiveDeserialized
import io.ktor.server.websocket.sendSerialized
import io.ktor.server.websocket.timeout
import io.ktor.server.websocket.webSocket
import io.ktor.websocket.close
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.serialization.json.Json
import kotlin.time.Duration.Companion.seconds
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class DefaultServer(private val serverPort: Int) {

    private val _sessionStateFlow = MutableStateFlow<SessionState>(SessionState.Idle)
    val sessionStateFlow = _sessionStateFlow
    private var embeddedServer: EmbeddedServer<*, *>? = null

    fun start() {
        embeddedServer = embeddedServer(Netty, serverPort) {
            install(WebSockets) {
                pingPeriod = PING_INTERVAL_SECONDS.seconds
                timeout = CONNECTION_TIMEOUT_SECONDS.seconds
                contentConverter = KotlinxWebsocketSerializationConverter(Json)
            }
            install(ContentNegotiation) {
                json(
                    Json {
                        prettyPrint = true
                    }
                )
            }
            routing {
                get(ConnectionContract.DEFAULT_CONNECTION_PATH) {
                    processSessionCreation()
                }
                webSocket("/session/{id}") {
                    val sessionId = call.parameters["id"]
                    try {
                        val sessionState = sessionStateFlow.value
                        when {
                            sessionState !is SessionState.Connected -> {
                                println("No active session!")
                            }
                            sessionState.session.sessionId != sessionId -> {
                                println("Expected ${sessionState.session.sessionId} received $sessionId")
                            }
                            else -> {
                                with(sessionState.session) { handleSession() }
                            }
                        }
                    } catch (ex: ClosedReceiveChannelException) {
                        println("Session $sessionId is closed!")
                        _sessionStateFlow.emit(SessionState.Disconnected(sessionId!!))
                    } catch (ex: Throwable) {
                        println("Encountered session error ${ex.stackTraceToString()}")
                        _sessionStateFlow.emit(SessionState.Disconnected(sessionId!!))
                    } finally {
                        println("Session $sessionId ended.")
                        _sessionStateFlow.emit(SessionState.Idle)
                    }
                }
            }
        }.start(wait = false)
    }

    fun stop() {
        embeddedServer?.stop()
    }

    private suspend fun RoutingContext.processSessionCreation() {
        val sessionState = _sessionStateFlow.value
        if (sessionState is SessionState.Connected) {
            println("Cleaning existing session ${sessionState.session.sessionId}")
            sessionState.session.close()
            _sessionStateFlow.value = SessionState.Disconnected(sessionState.session.sessionId)
        }
        val deviceType = call.request.headers[ConnectionContract.HEADER_DEVICE_TYPE]
        when (deviceType) {
            DeviceType.ANDROID.name -> {
                val sessionId = Uuid.random().toString()
                _sessionStateFlow.emit(SessionState.Connected(Session(sessionId)))
                call.respond(HttpStatusCode.OK, SessionData(sessionId, sessionUrl(sessionId)))
            }
            else -> {
                call.respond(
                    status = HttpStatusCode.BadRequest,
                    message = "Only android device supported!"
                )
            }
        }
    }

    private fun sessionUrl(sessionId: String) = "/session/$sessionId"

    companion object {
        private const val PING_INTERVAL_SECONDS = 5
        private const val CONNECTION_TIMEOUT_SECONDS = 5
    }
}

class Session(val sessionId: String) {
    private var projectSnapshot: ProjectSnapshot? = null
    private val virtualFileIrByFilePath = mutableMapOf<String, VirtualFileIr>()
    private val requests = MutableSharedFlow<Request<*>>()
    private var websocketSession: DefaultWebSocketServerSession? = null

    internal suspend fun DefaultWebSocketServerSession.handleSession() {
        websocketSession = this
        println("Start handling session $sessionId")
        requests.collect {
            when (it) {
                is ProjectSnapshotRequest -> {
                    sendSerialized(it.command)
                    val received = receiveDeserialized<ProjectSnapshot>()
                    projectSnapshot = received
                    it.receive.send(received)
                }
                is VirtualFileIrRequest -> {
                    sendSerialized(it.command)
                    val virtualFileIr = receiveDeserialized<VirtualFileIr>()
                    val filePath = it.command.parameters[0]
                    virtualFileIrByFilePath[filePath] = virtualFileIr
                    it.receive.send(virtualFileIr)
                }
                is CompositionDataRequest -> {
                    sendSerialized(it.command)
                    val compositionData = receiveDeserialized<CompositionRoots>()
                    it.receive.send(compositionData)
                }
            }
        }
    }

    suspend fun getProjectSnapshot(): ProjectSnapshot {
        val cached = projectSnapshot
        if (cached != null) {
            return cached
        }
        val request = ProjectSnapshotRequest()
        requests.emit(request)
        return request.receive.receive()
    }

    suspend fun getVirtualFileIr(filePath: String): VirtualFileIr {
        val cached = virtualFileIrByFilePath[filePath]
        if (cached != null) {
            return cached
        }
        val request = VirtualFileIrRequest(filePath)
        requests.emit(request)
        return request.receive.receive()
    }

    suspend fun getCompositionData(): CompositionRoots {
        val request = CompositionDataRequest()
        requests.emit(request)
        return request.receive.receive()
    }

    suspend fun close() {
        websocketSession?.close()
    }

    private sealed class Request<T>(
        val command: Command,
        val receive: Channel<T> = Channel(1)
    )

    private class ProjectSnapshotRequest : Request<ProjectSnapshot>(
        command = Command(CommandKeys.PROJECT_SNAPSHOT)
    )

    private class VirtualFileIrRequest(filePath: String) : Request<VirtualFileIr>(
        command = Command(CommandKeys.VIRTUAL_FILE_IR, listOf(filePath))
    )

    private class CompositionDataRequest : Request<CompositionRoots>(
        command = Command(CommandKeys.COMPOSITION_DATA)
    )
}

sealed interface SessionState {

    data object Idle : SessionState

    class Started(port: Int): SessionState

    class Disconnected(val sessionId: String) : SessionState

    class Connected(val session: Session) : SessionState
}
