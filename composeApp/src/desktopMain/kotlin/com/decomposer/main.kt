package com.decomposer

import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import com.decomposer.ui.DecomposerTheme
import com.decomposer.ui.MainApp
import decomposer.composeapp.generated.resources.Res
import decomposer.composeapp.generated.resources.ic_launcher
import org.jetbrains.compose.resources.painterResource

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
