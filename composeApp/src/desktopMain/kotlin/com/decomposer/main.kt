package com.decomposer

import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import com.decomposer.runtime.connection.ConnectionContract
import com.decomposer.server.DefaultServer
import com.decomposer.ui.DecomposerTheme
import com.decomposer.ui.MainApp
import decomposer.composeapp.generated.resources.Res
import decomposer.composeapp.generated.resources.ic_launcher
import org.jetbrains.compose.resources.painterResource

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

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Decomposer",
        state = WindowState(width = 2560.dp, height = 1536.dp),
        icon = painterResource(Res.drawable.ic_launcher)
    ) {
        DecomposerTheme {
            MainApp()
        }
    }
}
