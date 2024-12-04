package com.decomposer.server

import com.decomposer.runtime.connection.ConnectionContract
import com.decomposer.runtime.connection.model.Command
import com.decomposer.runtime.connection.model.CommandKeys
import com.decomposer.runtime.connection.model.CommandResponse
import com.decomposer.runtime.connection.model.CompositionDataResponse
import com.decomposer.runtime.connection.model.CompositionRoots
import com.decomposer.runtime.connection.model.DeviceType
import com.decomposer.runtime.connection.model.ProjectSnapshot
import com.decomposer.runtime.connection.model.ProjectSnapshotResponse
import com.decomposer.runtime.connection.model.SessionData
import com.decomposer.runtime.connection.model.VirtualFileIr
import com.decomposer.runtime.connection.model.VirtualFileIrResponse
import com.decomposer.runtime.connection.model.commandResponseSerializer
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
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import java.util.concurrent.TimeUnit
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
                json(commandResponseSerializer)
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
                    } catch (ex: Throwable) {
                        println("Encountered session error ${ex.stackTraceToString()}")
                    } finally {
                        println("Session $sessionId ended.")
                        _sessionStateFlow.emit(SessionState.Disconnected(sessionId!!))
                    }
                }
            }
        }.start(wait = false)
        _sessionStateFlow.value = SessionState.Started(port = serverPort)
    }

    fun stop() {
        embeddedServer?.stop(
            shutdownGracePeriod = 0,
            shutdownTimeout = 0,
            timeUnit = TimeUnit.MILLISECONDS
        )
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
    private val projectSnapshotRequests =
        MutableSharedFlow<ProjectSnapshotRequest>(replay = 1, extraBufferCapacity = 5)
    private val virtualFileIrRequests =
        MutableSharedFlow<VirtualFileIrRequest>(replay = 1, extraBufferCapacity = 20)
    private val compositionDataRequests =
        MutableSharedFlow<CompositionDataRequest>(replay = 1, extraBufferCapacity = 5)
    private val projectSnapshotWaiters = mutableListOf<SendChannel<ProjectSnapshot>>()
    private val virtualFileIrWaiters =
        mutableMapOf<String, MutableList<SendChannel<VirtualFileIr>>>()
    private val compositionDataWaiters = mutableListOf<SendChannel<CompositionRoots>>()
    private var websocketSession: DefaultWebSocketServerSession? = null

    internal suspend fun DefaultWebSocketServerSession.handleSession() {
        websocketSession = this
        println("Start handling session $sessionId")
        coroutineScope {
            launch {
                projectSnapshotRequests.filterNotNull().collect {
                    sendSerialized(it.command)
                    synchronized(projectSnapshotWaiters) {
                        projectSnapshotWaiters.add(it.receive)
                    }
                }
            }

            launch {
                virtualFileIrRequests.filterNotNull().collect {
                    sendSerialized(it.command)
                    synchronized(virtualFileIrWaiters) {
                        virtualFileIrWaiters.computeIfAbsent(it.filePath) {
                            mutableListOf()
                        }.add(it.receive)
                    }
                }
            }

            launch {
                compositionDataRequests.filterNotNull().collect {
                    sendSerialized(it.command)
                    synchronized(compositionDataWaiters) {
                        compositionDataWaiters.add(it.receive)
                    }
                }
            }

            launch {
                while (true) {
                    when (val response = receiveDeserialized<CommandResponse>()) {
                        is CompositionDataResponse -> {
                            val waiters = synchronized(compositionDataWaiters) {
                                mutableListOf<SendChannel<CompositionRoots>>().also {
                                    it.addAll(compositionDataWaiters)
                                    compositionDataWaiters.clear()
                                }
                            }
                            waiters.forEach {
                                it.send(response.compositionRoots)
                            }
                        }
                        is ProjectSnapshotResponse -> {
                            val waiters = synchronized(projectSnapshotWaiters) {
                                mutableListOf<SendChannel<ProjectSnapshot>>().also {
                                    it.addAll(projectSnapshotWaiters)
                                    projectSnapshotWaiters.clear()
                                }
                            }
                            waiters.forEach {
                                it.send(response.projectSnapshot)
                            }
                        }
                        is VirtualFileIrResponse -> {
                            val waiters = synchronized(virtualFileIrWaiters) {
                                virtualFileIrWaiters[response.virtualFileIr.filePath]?.let {
                                    mutableListOf<SendChannel<VirtualFileIr>>().also { list ->
                                        list.addAll(it)
                                        it.clear()
                                    }
                                } ?: emptyList()
                            }
                            waiters.forEach {
                                it.send(response.virtualFileIr)
                            }
                        }
                    }
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
        projectSnapshotRequests.emit(request)
        return request.receive.receive()
    }

    suspend fun getVirtualFileIr(filePath: String): VirtualFileIr {
        val cached = virtualFileIrByFilePath[filePath]
        if (cached != null) {
            return cached
        }
        val request = VirtualFileIrRequest(filePath)
        virtualFileIrRequests.emit(request)
        return request.receive.receive()
    }

    suspend fun getCompositionData(): CompositionRoots {
        val request = CompositionDataRequest()
        compositionDataRequests.emit(request)
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

    private class VirtualFileIrRequest(val filePath: String) : Request<VirtualFileIr>(
        command = Command(CommandKeys.VIRTUAL_FILE_IR, listOf(filePath))
    )

    private class CompositionDataRequest : Request<CompositionRoots>(
        command = Command(CommandKeys.COMPOSITION_DATA)
    )
}

sealed interface SessionState {

    data object Idle : SessionState

    class Started(val port: Int): SessionState

    class Disconnected(val sessionId: String) : SessionState

    class Connected(val session: Session) : SessionState
}
