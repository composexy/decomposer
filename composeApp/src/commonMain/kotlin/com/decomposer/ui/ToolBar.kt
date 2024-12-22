package com.decomposer.ui

import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.Checkbox
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Slider
import androidx.compose.material.SliderDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ToolBar(
    modifier: Modifier,
    panelsState: PanelsState
) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        FontSizeChooser()
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
        ToolBarCheckBox(
            checked = AppSetting.darkTheme,
            text = "Dark theme",
            onCheckedChanged = {
                AppSetting.darkTheme = it
            }
        )
    }
}

@Composable
fun FontSizeChooser() {
    val size = with(LocalDensity.current) {
        (LocalFontSize.current * 1.25).sp.toDp()
    }
    Row(
        modifier = Modifier.padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        var sliderValue by remember { mutableFloatStateOf(AppSetting.fontSize.toFloat()) }

        Slider(
            modifier = Modifier.width(size * 10),
            value = sliderValue,
            onValueChange = { sliderValue = it },
            onValueChangeFinished = { AppSetting.fontSize = sliderValue.toInt() },
            valueRange = 10f..40f,
            steps = 31,
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colors.secondary,
                activeTrackColor = MaterialTheme.colors.secondary
            )
        )
        DefaultPanelText(text = "${AppSetting.fontSize}sp")
    }
}

@Composable
fun ToolBarCheckBox(
    checked: Boolean,
    text: String,
    onCheckedChanged: (Boolean) -> Unit
) {
    val scale = AppSetting.fontSize.toFloat() / 24.0f
    val interactionSource = remember { MutableInteractionSource() }
    Row(
        Modifier
            .wrapContentWidth()
            .toggleable(
                value = checked,
                onValueChange = { onCheckedChanged(!checked) },
                role = Role.Checkbox
            )
            .hoverable(interactionSource)
            .pointerHoverIcon(PointerIcon.Hand),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = null,
            modifier = Modifier.scale(scale).padding(4.dp)
        )
        DefaultPanelText(text = text)
    }
}
