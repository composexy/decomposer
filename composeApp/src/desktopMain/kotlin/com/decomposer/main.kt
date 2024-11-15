package com.decomposer

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.decomposer.runtime.connection.ConnectionContract
import com.decomposer.server.DefaultServer

internal lateinit var server: DefaultServer
internal fun startServer() {
    val serverPort = System.getenv(
        "DECOMPOSER_SERVER_PORT"
    )?.toIntOrNull() ?: ConnectionContract.DEFAULT_SERVER_PORT
    server = DefaultServer(serverPort = serverPort).also {
        it.start()
        attemptAdbReverse(serverPort)
    }
}

private fun attemptAdbReverse(port: Int) {
    val processBuilder = ProcessBuilder("adb", "reverse", "tcp:$port", "tcp:$port")
    val process = processBuilder.start()
    val exitCode = process.waitFor()
    println("ADB reverse command exit code: $exitCode")
}

fun main() {
    startServer()
    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "decomposer",
        ) {
            App()
        }
    }
}
