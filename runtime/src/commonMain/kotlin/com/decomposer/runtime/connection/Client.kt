package com.decomposer.runtime.connection

import com.decomposer.runtime.Command
import com.decomposer.runtime.CommandHandler
import com.decomposer.runtime.Logger
import com.decomposer.runtime.connection.model.DeviceDescriptor
import com.decomposer.runtime.connection.model.SessionData
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.pingInterval
import io.ktor.client.plugins.websocket.receiveDeserialized
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.headers
import io.ktor.http.path
import io.ktor.serialization.kotlinx.KotlinxWebsocketSerializationConverter
import io.ktor.serialization.kotlinx.json.json
import io.ktor.websocket.CloseReason
import io.ktor.websocket.close
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import java.net.ConnectException
import java.util.concurrent.TimeUnit
import kotlin.time.Duration.Companion.seconds

internal abstract class Client(
    private val serverPort: Int,
    private val commandHandlers: Set<CommandHandler>
) : Logger {
    private val loggerTag = this::class.java.simpleName
    private var serverProbeJob: Job? = null
    private var sessionStartJob: Job? = null
    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    private var websocketSession: DefaultClientWebSocketSession? = null
    private val httpClient = HttpClient(OkHttp) {
        engine {
            preconfigured = OkHttpClient.Builder()
                .pingInterval(PING_INTERVAL_SECONDS, TimeUnit.SECONDS)
                .build()
        }
        install(ContentNegotiation) {
            json(
                Json {
                    prettyPrint = true
                }
            )
        }
        install(WebSockets) {
            pingInterval = PING_INTERVAL_SECONDS.seconds
            contentConverter = KotlinxWebsocketSerializationConverter(Json)
        }
    }

    fun start() {
        serverProbeJob?.cancel()
        serverProbeJob = coroutineScope.launch {
            val device = buildDeviceDescriptor()
            while (true) {
                try {
                    val response = httpClient.get {
                        url {
                            host = "localhost"
                            port = ConnectionContract.DEFAULT_SERVER_PORT
                            path(ConnectionContract.DEFAULT_CONNECTION_PATH)
                        }
                        header(ConnectionContract.HEADER_DEVICE_TYPE, device.deviceType.name)
                    }
                    when (response.status) {
                        HttpStatusCode.OK -> {
                            val sessionData = response.body<SessionData>()
                            sessionStartJob?.cancel()
                            sessionStartJob = coroutineScope.launch {
                                httpClient.webSocket(
                                    method = HttpMethod.Get,
                                    host = "localhost",
                                    port = serverPort,
                                    path = sessionData.sessionUrl
                                ) {
                                    runSession(session = this)
                                    sessionStartJob?.cancel()
                                    sessionStartJob = null
                                }
                            }
                            serverProbeJob?.cancel()
                            serverProbeJob = null
                            break
                        }
                        else -> {
                            log(Logger.Level.INFO, loggerTag, "Unexpected status code: ${response.status}")
                        }
                    }
                } catch (ex: Exception) {
                    when (ex) {
                        is ConnectException -> log(Logger.Level.INFO, loggerTag, "${ex.message}")
                        else -> log(Logger.Level.ERROR, loggerTag, ex.stackTraceToString())
                    }
                }
                delay(PROBE_INTERVAL_SECONDS.seconds)
            }
        }
    }

    fun stop() {
        serverProbeJob?.cancel()
        serverProbeJob = null
        sessionStartJob?.cancel()
        sessionStartJob = null
        websocketSession?.let {
            with(it) {
                launch {
                    close(CloseReason(CloseReason.Codes.NORMAL, "close"))
                    it.cancel()
                    websocketSession = null
                }
            }
        }
    }

    private fun runSession(session: DefaultClientWebSocketSession) = with(session) {
        this@Client.websocketSession = session
        launch {
            while (true) {
                val command = receiveDeserialized<Command>()
                commandHandlers.forEach { handler ->
                    if (handler.expectedKey == command.key) {
                        launch {
                            with(handler) {
                                session.processCommand(command)
                            }
                        }
                    }
                }
            }
        }
    }

    abstract fun buildDeviceDescriptor(): DeviceDescriptor

    companion object {
        private const val PROBE_INTERVAL_SECONDS = 5L
        private const val PING_INTERVAL_SECONDS = 20L
    }
}
