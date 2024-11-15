package com.decomposer.runtime.connection

import com.decomposer.runtime.Command
import com.decomposer.runtime.CommandHandler
import com.decomposer.runtime.Logger
import com.decomposer.runtime.connection.model.DeviceDescriptor
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.pingInterval
import io.ktor.client.plugins.websocket.receiveDeserialized
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.http.HttpMethod
import io.ktor.serialization.kotlinx.KotlinxWebsocketSerializationConverter
import io.ktor.websocket.CloseReason
import io.ktor.websocket.close
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import okhttp3.Interceptor
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
    private var sessionJob: Job? = null
    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    private var session: DefaultClientWebSocketSession? = null
    private val connection = HttpClient(OkHttp) {
        engine {
            val deviceDescriptor = buildDeviceDescriptor()
            preconfigured = OkHttpClient.Builder()
                .pingInterval(PING_INTERVAL_SECONDS, TimeUnit.SECONDS)
                .addInterceptor(deviceHeaderInterceptor(deviceDescriptor))
                .build()
        }

        install(WebSockets) {
            pingInterval = PING_INTERVAL_SECONDS.seconds
            contentConverter = KotlinxWebsocketSerializationConverter(Json)
        }
    }

    fun start() {
        serverProbeJob?.cancel()
        serverProbeJob = coroutineScope.launch {
            while (true) {
                try {
                    connection.webSocket(
                        method = HttpMethod.Get,
                        host = "localhost",
                        port = serverPort,
                        path = "/connect"
                    ) {
                        serverProbeJob?.cancel()
                        serverProbeJob = null
                        runSession(session = this)
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
        session?.let { closeSession(it) }
    }

    private fun runSession(session: DefaultClientWebSocketSession) = with(session) {
        this@Client.session = session
        sessionJob?.cancel()
        sessionJob = launch {
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

    private fun closeSession(session: DefaultClientWebSocketSession) = with(session) {
        launch {
            close(CloseReason(CloseReason.Codes.NORMAL, "close"))
            sessionJob?.cancel()
            sessionJob = null
        }
    }

    private fun deviceHeaderInterceptor(device: DeviceDescriptor): Interceptor =
        Interceptor { chain ->
            val builder = chain.request().newBuilder()
            with(builder) {
                header(ConnectionContract.HEADER_DEVICE_TYPE, device.deviceType.name)
                header(ConnectionContract.HEADER_SERIAL_NUMBER, device.serialNumber)
            }
            return@Interceptor chain.proceed(builder.build())
        }

    abstract fun buildDeviceDescriptor(): DeviceDescriptor

    companion object {
        private const val PROBE_INTERVAL_SECONDS = 5L
        private const val PING_INTERVAL_SECONDS = 20L
    }
}
