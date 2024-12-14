package com.decomposer.sample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.Popup

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        ComposeView(this).also {
            setContentView(it)
        }.setContent {
            val stateList = remember {
                mutableStateListOf(12, 123)
            }
            val stateMap = remember {
                mutableStateMapOf(2 to "123", 4 to "123123")
            }
            Box {
                Text("Lazy")
            }
            Empty()
            Greeting(modifier = Modifier.width(100.dp), message = "HiHi")
            Footer(modifier = Modifier.width(100.dp), message = "HiHi")
            val state1 = remember {
                mutableStateOf("Hi! ${stateList[0]} ${stateMap[2]}")
            }
            Text(modifier = Modifier, text = "${state1.value} ${textState.value}")
            LazyColumn {
                item {
                    Text("Lazy")
                }
            }
            Popup {
                Text("Popup")
            }
            Dialog(
                onDismissRequest = {}
            ) {
                Text("Dialog")
            }
        }
    }
}

val textState = mutableStateOf("World")

@Composable
fun Empty() {
    val color = LocalContentColor.current
}

@Composable
fun Footer(modifier: Modifier, message: String) {
    val footerState = remember {
        mutableStateOf("Hi!")
    }
    Box {
        Text(modifier = modifier, text = "${footerState.value} $message ${textState.value}")
    }
    Box {
        Text(modifier = modifier, text = "${footerState.value} $message ${textState.value}")
    }
    Column {
        Text(modifier = modifier, text = "${footerState.value} $message ${textState.value}")
        Text(modifier = modifier, text = "${footerState.value} $message ${textState.value}")
        Text(modifier = modifier, text = "${footerState.value} $message ${textState.value}")
    }
}
