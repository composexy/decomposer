package com.decomposer.sample

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.Popup

@Composable
fun SimplePopup() {
    Popup {
        Text(
            modifier = Modifier.size(160.dp),
            text = "I am SimplePopup!",
            fontSize = 24.sp,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun SimpleDialog() {
    var showDialog: Boolean by remember { mutableStateOf(true) }
    if (showDialog) {
        Dialog(
            onDismissRequest = {
                showDialog = false
            }
        ) {
            Text(
                modifier = Modifier.size(160.dp),
                text = "I am SimpleDialog!",
                fontSize = 24.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}
