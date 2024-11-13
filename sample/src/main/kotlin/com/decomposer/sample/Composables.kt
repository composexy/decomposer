package com.decomposer.sample

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun Greeting(modifier: Modifier, message: String) {
    Text(modifier = modifier, text = message)
}
