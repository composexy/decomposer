package com.decomposer.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.decomposer.server.SessionState

@Composable
fun Panels(
    modifier: Modifier,
    sessionState: SessionState,
    panelsState: PanelsState = rememberPanelsState()
) {
    when (sessionState) {
        is SessionState.Disconnected -> {
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                DefaultPanelText(
                    text = """
                        Session ${sessionState.sessionId} was disconnected!
                    """.trimIndent(),
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
        SessionState.Idle -> {
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                DefaultPanelText(
                    text = """
                        Waiting for server to boot!
                    """.trimIndent(),
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
        is SessionState.Started -> {
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                DefaultPanelText(
                    text = """
                        Server boot up at port ${sessionState.port}!
                    """.trimIndent(),
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
        is SessionState.Connected -> {
            Column(
                modifier = modifier
            ) {
                ToolBar(
                    modifier = Modifier.wrapContentHeight().fillMaxWidth(),
                    toolBarState = panelsState.toolBarState
                )
            }
        }
    }
}

@Composable
fun rememberPanelsState(): PanelsState {
    return remember { PanelsState() }
}

@Composable
fun DefaultPanelText(
    text: String,
    modifier: Modifier = Modifier
) {
    Text(
        modifier = modifier,
        text = text,
        textAlign = TextAlign.Center,
        fontFamily = Fonts.jetbrainsMono(),
        fontSize = 24.sp,
        fontWeight = FontWeight.Light,
        lineHeight = 36.sp
    )
}

class PanelsState {
    val toolBarState: ToolBarState = ToolBarState()
}

class ToolBarState {
    var fileTreeVisible by mutableStateOf(true)
    var irViewerVisible by mutableStateOf(true)
    var compositionViewerVisible by mutableStateOf(false)
}
