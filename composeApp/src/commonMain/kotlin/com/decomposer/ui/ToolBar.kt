package com.decomposer.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.Checkbox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import java.nio.file.Paths

@Composable
fun ToolBar(
    modifier: Modifier,
    panelsState: PanelsState
) {
    Row(modifier = modifier) {
        ToolBarCheckBox(
            checked = panelsState.fileTreeVisible,
            text = "Show file tree",
            onCheckedChanged = {
                panelsState.fileTreeVisible = it
            }
        )
        ToolBarCheckBox(
            checked = panelsState.irViewerVisible,
            text = "Show ir tree",
            onCheckedChanged = {
                panelsState.irViewerVisible = it
            }
        )
        ToolBarCheckBox(
            checked = panelsState.compositionViewerVisible,
            text = "Show composition",
            onCheckedChanged = {
                panelsState.compositionViewerVisible = it
            }
        )
    }
}

@Composable
fun ToolBarCheckBox(
    checked: Boolean,
    text: String,
    onCheckedChanged: (Boolean) -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    Row(
        Modifier
            .wrapContentSize()
            .toggleable(
                value = checked,
                onValueChange = { onCheckedChanged(!checked) },
                role = Role.Checkbox
            )
            .hoverable(interactionSource)
            .pointerHoverIcon(PointerIcon.Hand)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = null,
            modifier = Modifier.padding(4.dp)
        )
        DefaultPanelText(
            text = text,
        )
    }
}
