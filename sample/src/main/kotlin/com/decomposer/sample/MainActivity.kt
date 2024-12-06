package com.decomposer.sample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        /*
        setContent {
            Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                Column {
                    Greeting(
                        message = "Hello Decomposer!",
                        modifier = Modifier.padding(innerPadding)
                    )
                    Footer(
                        message = "Bye!",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }*/
        ComposeView(this).also {
            setContentView(it)
        }.setContent {
            val state1 = remember {
                mutableStateOf("Hi!")
            }
            Text(modifier = Modifier, text = "${state1.value} ${textState.value}")
            LazyColumn {
                item {
                    Text("Lazy")
                }
            }
        }
    }
}

val textState = mutableStateOf("World")

@Composable
fun Footer(modifier: Modifier, message: String) {
    val footerState = remember {
        mutableStateOf("Hi!")
    }
    Box {
        Text(modifier = modifier, text = "${footerState.value} $message ${textState.value}")
    }
}
