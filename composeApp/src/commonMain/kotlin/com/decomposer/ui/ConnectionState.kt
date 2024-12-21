package com.decomposer.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.decomposer.runtime.connection.ConnectionContract
import com.decomposer.server.AdbConnectResult
import com.decomposer.server.AdbConnection
import com.decomposer.server.DefaultServer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ConnectionState(port: Int) {
    private val server: DefaultServer = DefaultServer(port)
    private val adbConnection: AdbConnection = AdbConnection(port)

    private val _adbConnectState = MutableStateFlow<AdbConnectResult>(AdbConnectResult.Idle)
    val adbConnectState: StateFlow<AdbConnectResult> = _adbConnectState

    val sessionState = server.sessionStateFlow

    fun skipConnect() {
        _adbConnectState.value = AdbConnectResult.Skipped
    }
    
    fun adbConnect() {
        val connectResult = adbConnection.connect()
        _adbConnectState.value = connectResult
    }
    
    fun serverConnect() {
        server.start()
    }
    
    fun serverDisconnect() {
        server.stop()
    }
}

@Composable
fun rememberConnectionState(port: Int = ConnectionContract.DEFAULT_SERVER_PORT): ConnectionState {
    return remember(port) {
        ConnectionState(port)
    }
}
