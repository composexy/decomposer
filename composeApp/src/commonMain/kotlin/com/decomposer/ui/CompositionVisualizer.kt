package com.decomposer.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowColumn
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.decomposer.runtime.connection.model.ComposeState
import com.decomposer.runtime.connection.model.CompositionRoot
import com.decomposer.runtime.connection.model.CompositionRoots
import com.decomposer.runtime.connection.model.Group
import com.decomposer.runtime.connection.model.IntKey
import com.decomposer.runtime.connection.model.ObjectKey

@Composable
private fun BaseGroupRow(
    level: Int,
    name: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .wrapContentHeight()
            .fillMaxWidth()
            .padding(start = 24.dp * level)
    ) {
        val interactionSource = remember { MutableInteractionSource() }

        Text(
            text = name,
            modifier = Modifier
                .align(Alignment.CenterVertically)
                .clipToBounds()
                .hoverable(interactionSource)
                .pointerHoverIcon(PointerIcon.Hand)
                .clickable { onClick() },
            softWrap = true,
            overflow = TextOverflow.Ellipsis,
            maxLines = 1,
            fontFamily = Fonts.jetbrainsMono(),
            fontSize = 32.sp,
            fontWeight = FontWeight.Light,
            lineHeight = 48.sp
        )
    }
}

@Composable
private fun StateRow(
    level: Int,
    state: ComposeState
) {

}

fun CompositionRoots.buildCompositionTree(
    showContextPopup: (@Composable () -> Unit) -> Unit,
    sourceNavigation: (String, Int, Int) -> Unit
): FilterableTree {
    return FilterableTree(
        root = RootsNode(this, showContextPopup, sourceNavigation)
    )
}

private class RootsNode(
    private val compositionRoots: CompositionRoots,
    val showContextPopup: (@Composable () -> Unit) -> Unit,
    val sourceNavigation: (String, Int, Int) -> Unit
) : BaseTreeNode() {

    override val name = "Application"
    override val children: List<TreeNode>
        get() {
            return compositionRoots.compositionData.map {
                it.map(level + 1, showContextPopup, sourceNavigation)
            }
        }
    override val tags: List<Any> = emptyList()
    override val level = 0

    @OptIn(ExperimentalLayoutApi::class)
    @Composable
    override fun TreeNodeRow() {
        BaseGroupRow(level, name) {
            showContextPopup @Composable {
                Column(
                    modifier = Modifier.fillMaxWidth().wrapContentHeight()
                ) {
                    DefaultPanelText(
                        text = "Found states:",
                        modifier = Modifier.fillMaxWidth()
                    )
                    FlowColumn(
                        modifier = Modifier.fillMaxWidth()
                    ) {

                    }
                }
            }
        }
    }
}

private fun CompositionRoot.map(
    level: Int,
    showContextPopup: (@Composable () -> Unit) -> Unit,
    sourceNavigation: (String, Int, Int) -> Unit
): TreeNode {
    TODO()
}

private val Group.name: String
    get() {
        val sourceInfo = this.attributes.sourceInformation
        val key = this.attributes.key
        val sourceKey = if (sourceInfo != null) {
            val startIndex =
                when {
                    sourceInfo.startsWith("C(") -> 2
                    sourceInfo.startsWith("CC(") -> 3
                    else -> -1
                }
            val endIndex = sourceInfo.indexOf(')')
            if (endIndex > 2 && startIndex >= 0)
                sourceInfo.substring(startIndex, endIndex)
            else null
        } else null
        if (sourceKey?.isNotBlank() == true) return sourceKey
        val wellKnownKey = if (key is IntKey) {
            WellKnownKeys.entries.firstOrNull {
                it.intKey == key.value
            }?.displayName
        } else null
        if (wellKnownKey?.isNotBlank() == true) return wellKnownKey
        val objectKey = if (key is ObjectKey) {
            "KeyGroup(${key.value})"
        } else null
        return objectKey ?: "Group"
    }

private enum class WellKnownKeys(val intKey: Int, val displayName: String) {
    ROOT(100, "Root"),
    NODE(125, "ComposeNode"),
    DEFAULTS(-127, "Defaults"),
    INVOCATION(200, "Invocation"),
    PROVIDER(201, "Provider"),
    COMPOSITION_LOCAL_MAP(202, "CompositionLocalMap"),
    PROVIDER_VALUES(203, "ProviderValues"),
    PROVIDER_MAPS(204, "ProviderMaps"),
    REFERENCE(206, "Reference"),
    REUSE(207, "Reuse")
}