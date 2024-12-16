package com.decomposer.sample

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp

@Composable
fun SimpleText() {
    Text(
        modifier = Modifier.wrapContentHeight().fillMaxWidth(),
        text = "Hello Decomposer!",
        fontSize = 24.sp,
        textAlign = TextAlign.Center,
    )
}
