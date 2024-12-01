package com.decomposer.ui

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.Checkbox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp

@Composable
fun ToolBar(
    modifier: Modifier,
    toolBarState: ToolBarState
) {
    Row(modifier = modifier) {
        ToolBarCheckBox(
            checked = toolBarState.fileTreeVisible,
            text = "Show file tree",
            onCheckedChanged = {
                toolBarState.fileTreeVisible = it
            }
        )
        ToolBarCheckBox(
            checked = toolBarState.irViewerVisible,
            text = "Show ir tree",
            onCheckedChanged = {
                toolBarState.irViewerVisible = it
            }
        )
        ToolBarCheckBox(
            checked = toolBarState.compositionViewerVisible,
            text = "Show composition",
            onCheckedChanged = {
                toolBarState.compositionViewerVisible = it
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
    Row(
        Modifier
            .wrapContentSize()
            .toggleable(
                value = checked,
                onValueChange = { onCheckedChanged(!checked) },
                role = Role.Checkbox
            )
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = null
        )
        DefaultPanelText(
            text = text,
        )
    }
}
