package com.composexy.decomposer

import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.singleWindowApplication

fun main() = singleWindowApplication(
    title = "decomposer",
    state = WindowState(placement = WindowPlacement.Maximized)
) {
    Application()
}
