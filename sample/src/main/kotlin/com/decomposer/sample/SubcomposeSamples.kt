package com.decomposer.sample

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp

@Composable
fun SimpleBoxWithConstraints() {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        Text(
            modifier = Modifier.wrapContentHeight().fillMaxWidth(),
            text = """
                Hello SimpleBoxWithConstraints!
                Your minWidth is ${constraints.minWidth}
                Your maxWidth is ${constraints.maxWidth}
                Your minHeight is ${constraints.minHeight}
                Your maxHeight is ${constraints.maxHeight}
            """.trimIndent(),
            fontSize = 24.sp,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun SimpleLazyColumn() {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(200) {
            Text(
                modifier = Modifier.wrapContentHeight().fillMaxWidth(),
                text = "Index $it",
                fontSize = 24.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}
