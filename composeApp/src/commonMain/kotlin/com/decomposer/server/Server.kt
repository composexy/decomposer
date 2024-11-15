package com.decomposer.server

import androidx.compose.ui.util.fastCbrt
import com.decomposer.runtime.connection.ConnectionContract
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import io.ktor.server.websocket.WebSockets
import io.ktor.server.websocket.pingPeriod
import io.ktor.server.websocket.timeout
import io.ktor.server.websocket.webSocket
import kotlin.time.Duration.Companion.seconds

internal class DefaultServer(private val serverPort: Int) {
    fun start() {
        embeddedServer(Netty, serverPort) {
            install(WebSockets) {
                pingPeriod = PING_INTERVAL_SECONDS.seconds
                timeout = CONNECTION_TIMEOUT_SECONDS.seconds
            }
            routing {
                get(ConnectionContract.DEFAULT_CONNECTION_PATH) {

                }
                webSocket("/session/{id}") {
                    val sessionId = call.parameters["id"]
                }
            }
        }.start(wait = false)
    }

    companion object {
        private const val PING_INTERVAL_SECONDS = 5
        private const val CONNECTION_TIMEOUT_SECONDS = 15
    }
}
