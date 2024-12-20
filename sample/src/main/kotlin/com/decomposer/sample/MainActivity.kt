package com.decomposer.sample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val selectedIndex = contentSelected
            if (selectedIndex == null) {
                ContentList()
            } else {
                contentList[selectedIndex].content()
            }
        }
    }

    @Suppress("DEPRECATION")
    override fun onBackPressed() {
        if (contentSelected == null) {
            super.onBackPressed()
        } else {
            contentSelected = null
        }
    }
}

@Composable
fun ContentList() {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(contentList.size) {
            Text(
                modifier = Modifier.fillMaxWidth()
                    .padding(16.dp)
                    .clickable { contentSelected = it },
                text = contentList[it].displayName,
                textAlign = TextAlign.Center,
                fontSize = 16.sp
            )
        }
    }
}

var contentSelected: Int? by mutableStateOf(null)

val contentList = listOf(
    SampleContent(displayName = "Simple Text", content = { SimpleText() }),
    SampleContent(displayName = "Simple Dialog", content = { SimpleDialog() }),
    SampleContent(displayName = "Simple Popup", content = { SimplePopup() }),
    SampleContent(displayName = "Simple BoxWithConstraints", content = { SimpleBoxWithConstraints() }),
    SampleContent(displayName = "Simple LazyColumn", content = { SimpleLazyColumn() }),
    SampleContent(displayName = "Simple State Reader", content = { SimpleStateReader() })
)

class SampleContent(
    val displayName: String,
    val content: @Composable () -> Unit
)
