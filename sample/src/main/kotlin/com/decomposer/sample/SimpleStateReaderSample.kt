package com.decomposer.sample

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp

private var alphaFirst by mutableStateOf(0.5f)
private var alphaSecond by mutableStateOf(0.9f)
private var alphaThird by mutableStateOf(0.8f)

@Composable
fun SimpleStateReader() {
    Column {
        Box(modifier = Modifier.graphicsLayer {
            alpha = alphaFirst
        }.size(240.dp).background(Color.Yellow))
        Box(modifier = Modifier.graphicsLayer(
            alpha = alphaSecond
        ).size(240.dp).background(Color.Blue))
        val showThird: Boolean by remember {
            derivedStateOf { alphaThird > 0.5f }
        }
        if (showThird) {
            Box(modifier = Modifier.graphicsLayer {
                alpha = alphaThird
            }.size(240.dp).background(Color.Gray))
        }
    }
}
