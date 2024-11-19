package com.decomposer.server

import com.decomposer.ir.IrProcessor
import com.decomposer.runtime.Command
import com.decomposer.runtime.CommandKeys
import com.decomposer.runtime.connection.ConnectionContract
import com.decomposer.runtime.connection.model.DeviceType
import com.decomposer.runtime.connection.model.ProjectSnapshot
import com.decomposer.runtime.connection.model.SessionData
import com.decomposer.runtime.connection.model.VirtualFileIr
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.KotlinxWebsocketSerializationConverter
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.install
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
import kotlinx.serialization.json.Json
import kotlin.time.Duration.Companion.seconds
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
internal class DefaultServer(private val serverPort: Int) {

    private val sessions = mutableMapOf<String, Session>()

    fun start() {
        embeddedServer(Netty, serverPort) {
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
                    val session = sessions[sessionId]
                    if (session == null) {
                        println("Cannot find session $sessionId")
                    } else {
                        with(session) {
                            handleSession()
                        }
                    }
                }
            }
        }.start(wait = false)
    }

    private suspend fun RoutingContext.processSessionCreation() {
        val deviceType = call.request.headers[ConnectionContract.HEADER_DEVICE_TYPE]
        when (deviceType) {
            DeviceType.ANDROID.name -> {
                val sessionId = Uuid.random().toString()
                call.respond(HttpStatusCode.OK, SessionData(sessionId, sessionUrl(sessionId)))
                sessions[sessionId] = Session()
            }
            else -> {
                call.respond(HttpStatusCode.BadRequest)
            }
        }
    }

    private fun sessionUrl(sessionId: String) = "/session/$sessionId"

    companion object {
        private const val PING_INTERVAL_SECONDS = 5
        private const val CONNECTION_TIMEOUT_SECONDS = 15
    }
}

internal class Session {
    private val irProcessor = IrProcessor()
    private lateinit var projectSnapshot: ProjectSnapshot
    private val virtualFileIrByFilePath = mutableMapOf<String, VirtualFileIr>()

    internal suspend fun DefaultWebSocketServerSession.handleSession() {
        sendSerialized(Command(CommandKeys.PROJECT_SNAPSHOT))
        projectSnapshot = receiveDeserialized<ProjectSnapshot>()
        projectSnapshot.fileTree.forEach {
            sendSerialized(Command(CommandKeys.VIRTUAL_FILE_IR, listOf(it)))
            val virtualFileIr = receiveDeserialized<VirtualFileIr>()
            virtualFileIrByFilePath[it] = virtualFileIr
            irProcessor.processVirtualFileIr(virtualFileIr)
        }
    }
}
