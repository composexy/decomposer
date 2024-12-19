package com.decomposer.sample.ir

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun Simple() {
    Text(text = "Hello World!", modifier = Modifier.padding(1.dp))
}
