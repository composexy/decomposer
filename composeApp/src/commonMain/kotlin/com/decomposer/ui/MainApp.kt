package com.decomposer.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.decomposer.runtime.connection.ConnectionContract
import com.decomposer.server.AdbConnectResult
import kotlinx.coroutines.delay
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
                when (adbConnectState) {
                    AdbConnectResult.Success,
                    AdbConnectResult.Skipped -> PanelContentState.Editor
                    else -> PanelContentState.DeviceDiscovery
                }
            }
        }
        AnimatedContent(contentState) {
            when (contentState.value) {
                PanelContentState.DeviceDiscovery -> {
                    DeviceDiscovery(
                        modifier = Modifier.fillMaxSize(),
                        adbState = adbConnectState,
                        versions = Versions,
                        onConnect = {
                            coroutineScope.launch {
                                if (adbConnectState != AdbConnectResult.Success) {
                                    connectionState.adbConnect()
                                }
                            }
                        },
                        onSkip = {
                            if (adbConnectState != AdbConnectResult.Success) {
                                connectionState.skipConnect()
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

    DetectAdbDisconnect(connectionState)

    DisposableEffect(connectionState) {
        connectionState.serverConnect()
        onDispose {
            connectionState.serverDisconnect()
        }
    }
}

@Composable
fun DetectAdbDisconnect(connectionState: ConnectionState) {
    val adbConnectState by connectionState.adbConnectState.collectAsState()
    LaunchedEffect(adbConnectState) {
        if (adbConnectState == AdbConnectResult.Success) {
            while (true) {
                delay(1000)
                connectionState.adbConnect()
            }
        }
    }
}

private enum class PanelContentState {
    DeviceDiscovery, Editor
}

object AppSetting {
    var darkTheme: Boolean by mutableStateOf(true)
    var fontSize: Int by mutableIntStateOf(14)
}

object Versions {
    const val DECOMPOSER_VERSION = "0.1.0"
    const val TARGET_COMPOSE_RUNTIME_VERSION = "1.7.1"
    const val TARGET_KOTLIN_VERSION = "2.1.0"
}
