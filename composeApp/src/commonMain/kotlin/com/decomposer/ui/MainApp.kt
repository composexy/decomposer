package com.decomposer.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.decomposer.runtime.connection.ConnectionContract
import com.decomposer.server.AdbConnectResult
import com.decomposer.server.SessionState

@Composable
fun MainApp() {
    val serverPort = System.getenv(
        "DECOMPOSER_SERVER_PORT"
    )?.toIntOrNull() ?: ConnectionContract.DEFAULT_SERVER_PORT
    val connectionState = rememberConnectionState(serverPort)

    val adbConnectState by connectionState.adbConnectState.collectAsState()
    val sessionState by connectionState.sessionState.collectAsState()

    Surface(modifier = Modifier.fillMaxSize()) {

    }

    LaunchedEffect(adbConnectState) {
        if (adbConnectState != AdbConnectResult.Success) {
            connectionState.adbConnect()
        } else {
            connectionState.serverConnect()
        }
    }

    DisposableEffect(connectionState) {
        onDispose {
            connectionState.serverDisconnect()
        }
    }
}
