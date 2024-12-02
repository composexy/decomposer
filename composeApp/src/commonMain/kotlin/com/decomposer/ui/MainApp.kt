package com.decomposer.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import com.decomposer.runtime.connection.ConnectionContract
import com.decomposer.server.AdbConnectResult
import kotlinx.coroutines.launch

@Composable
fun MainApp() {
    val serverPort = System.getenv(
        "DECOMPOSER_SERVER_PORT"
    )?.toIntOrNull() ?: ConnectionContract.DEFAULT_SERVER_PORT
    val connectionState = rememberConnectionState(serverPort)
    val adbConnectState by connectionState.adbConnectState.collectAsState()
    val sessionState by connectionState.sessionState.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    Surface(modifier = Modifier.fillMaxSize()) {
        val contentState = remember {
            derivedStateOf {
                if (adbConnectState is AdbConnectResult.Success) {
                    PanelContentState.Editor
                } else {
                    PanelContentState.DeviceDiscovery
                }
            }
        }
        AnimatedContent(contentState) {
            when (contentState.value) {
                PanelContentState.DeviceDiscovery -> {
                    DeviceDiscovery(
                        modifier = Modifier.fillMaxSize(),
                        adbState = adbConnectState,
                        onConnect = {
                            coroutineScope.launch {
                                if (adbConnectState != AdbConnectResult.Success) {
                                    connectionState.adbConnect()
                                }
                            }
                        }
                    )
                }
                PanelContentState.Editor -> {
                    Panels(
                        modifier = Modifier.fillMaxSize(),
                        sessionState = sessionState
                    )
                }
            }
        }
    }

    DisposableEffect(connectionState) {
        connectionState.serverConnect()
        onDispose {
            connectionState.serverDisconnect()
        }
    }
}

private enum class PanelContentState {
    DeviceDiscovery, Editor
}
