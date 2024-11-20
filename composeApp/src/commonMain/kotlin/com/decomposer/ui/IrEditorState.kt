package com.decomposer.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.RememberObserver
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.AnnotatedString
import com.decomposer.ir.KotlinFile

@Composable
fun rememberIrEditorState(kotlinFile: KotlinFile) = remember(kotlinFile.filePath) {
    IrEditorState(kotlinFile)
}

@Stable
class IrEditorState(private val kotlinFile: KotlinFile) : RememberObserver {
    var annotatedStringState by mutableStateOf<AnnotatedString?>(null)

    override fun onRemembered() {
        val visualData = IrVisualBuilder(kotlinFile).visualize()
        annotatedStringState = visualData.annotatedString
    }

    override fun onAbandoned() = Unit
    override fun onForgotten() = Unit
}
