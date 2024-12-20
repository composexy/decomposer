package com.decomposer.sample

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp

private var first by mutableStateOf(0.5f)
private var second by mutableStateOf(0.9f)

@Composable
fun SimpleStateReader() {
    Column {
        Box(modifier = Modifier.graphicsLayer {
            alpha = first
        }.size(240.dp).background(Color.Yellow))
        Box(modifier = Modifier.graphicsLayer(
            alpha = second
        ).size(240.dp).background(Color.Blue))
    }
}
