package com.decomposer

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import com.decomposer.ui.AppSetting
import com.decomposer.ui.DecomposerTheme
import com.decomposer.ui.LocalFontSize
import com.decomposer.ui.LocalTheme
import com.decomposer.ui.MainApp
import com.decomposer.ui.Theme
import decomposer.composeapp.generated.resources.Res
import decomposer.composeapp.generated.resources.ic_launcher
import org.jetbrains.compose.resources.painterResource

fun main() = application {
    CompositionLocalProvider(
        LocalTheme provides if (AppSetting.darkTheme) Theme.dark else Theme.light,
        LocalFontSize provides AppSetting.fontSize
    ) {
        Window(
            onCloseRequest = ::exitApplication,
            title = "Decomposer",
            state = WindowState(placement = WindowPlacement.Maximized),
            icon = painterResource(Res.drawable.ic_launcher)
        ) {
            DecomposerTheme {
                MainApp()
            }
        }
    }
}
