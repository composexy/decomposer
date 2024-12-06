package com.decomposer.ui

import androidx.compose.foundation.HorizontalScrollbar
import androidx.compose.foundation.Image
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.Checkbox
import androidx.compose.material.RadioButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.decomposer.runtime.connection.model.LayoutNode
import com.decomposer.runtime.connection.model.RecomposeScope
import com.decomposer.runtime.connection.model.SubcomposeState
import com.decomposer.server.Session
import decomposer.composeapp.generated.resources.Res
import decomposer.composeapp.generated.resources.refresh
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import kotlin.reflect.KClass

@Composable
fun CompositionPanel(
    modifier: Modifier = Modifier,
    session: Session,
    onShowPopup: (@Composable () -> Unit) -> Unit
) {
    var compositionTree: FilterableTree by remember {
        mutableStateOf(FilterableTree.EMPTY_TREE)
    }

    var filteredNodeKind: NodeKind by remember {
        mutableStateOf(NodeKind.ALL)
    }

    var filterSystem: Boolean by remember {
        mutableStateOf(true)
    }

    val filteredCompositionTree: FilterableTree by remember {
        derivedStateOf {
            val filters = mutableSetOf<KClass<*>>().also {
                if (filterSystem) {
                    it.add(System::class)
                }
                when (filteredNodeKind) {
                    NodeKind.RECOMPOSE_SCOPE -> it.add(RecomposeScope::class)
                    NodeKind.LAYOUT_NODE -> it.add(LayoutNode::class)
                    NodeKind.SUBCOMPOSITION -> it.add(SubcomposeState::class)
                    else -> { }
                }
            }
            when (filteredNodeKind) {
                NodeKind.ALL -> compositionTree
                else -> compositionTree.filterSubTree(filters)
            }
        }
    }

    val coroutineScope = rememberCoroutineScope { Dispatchers.Default }

    Column(
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().wrapContentHeight()
        ) {
            CompositionPanelBar(
                modifier = Modifier.wrapContentHeight().fillMaxWidth(),
                filteredNodeKind = filteredNodeKind,
                onRefresh = {
                    coroutineScope.launch {
                        val compositionRoots = session.getCompositionData()
                        compositionTree = compositionRoots.buildCompositionTree(
                            showContextPopup = {
                                onShowPopup(it)
                            },
                            sourceNavigation = { path, start, end ->

                            }
                        )
                    }
                },
                onSelectedOption = { filteredNodeKind = it },
                onFilterSystemChange = { filterSystem = it },
                filterSystem = filterSystem
            )
        }
        Expander(
            onFoldAll = {
                compositionTree.foldAll()
            },
            onExpandAll = {
                compositionTree.foldAll()
            }
        )
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            val verticalScrollState = rememberLazyListState()
            val horizontalScrollState = rememberScrollState()

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .horizontalScroll(horizontalScrollState)
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    state = verticalScrollState,
                    contentPadding = PaddingValues(vertical = 4.dp, horizontal = 12.dp)
                ) {
                    val nodes = filteredCompositionTree.flattenNodes
                    items(nodes.size) {
                        nodes[it].TreeNodeRow()
                    }
                }
            }

            VerticalScrollbar(
                modifier = Modifier.align(Alignment.CenterEnd),
                adapter = rememberScrollbarAdapter(verticalScrollState)
            )

            HorizontalScrollbar(
                modifier = Modifier.align(Alignment.BottomCenter),
                adapter = rememberScrollbarAdapter(horizontalScrollState)
            )
        }
    }
}

@Composable
fun CompositionPanelBar(
    modifier: Modifier,
    filteredNodeKind: NodeKind,
    filterSystem: Boolean,
    onRefresh: () -> Unit,
    onSelectedOption: (NodeKind) -> Unit,
    onFilterSystemChange: (Boolean) -> Unit
) {
    Row(modifier = modifier) {
        CompositionRefresh(onRefresh)
        FilterSystem(
            checked = filterSystem,
            onCheckedChanged = {
                onFilterSystemChange(it)
            }
        )
        FilterOption(
            selected = filteredNodeKind == NodeKind.ALL,
            text = NodeKind.ALL.tag,
            onSelected = {
                onSelectedOption(NodeKind.ALL)
            }
        )
        FilterOption(
            selected = filteredNodeKind == NodeKind.RECOMPOSE_SCOPE,
            text = NodeKind.RECOMPOSE_SCOPE.tag,
            onSelected = {
                onSelectedOption(NodeKind.RECOMPOSE_SCOPE)
            }
        )
        FilterOption(
            selected = filteredNodeKind == NodeKind.LAYOUT_NODE,
            text = NodeKind.LAYOUT_NODE.tag,
            onSelected = {
                onSelectedOption(NodeKind.LAYOUT_NODE)
            }
        )
        FilterOption(
            selected = filteredNodeKind == NodeKind.SUBCOMPOSITION,
            text = NodeKind.SUBCOMPOSITION.tag,
            onSelected = {
                onSelectedOption(NodeKind.SUBCOMPOSITION)
            }
        )
    }
}

@Composable
fun CompositionRefresh(onRefresh: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    Row(
        Modifier
            .wrapContentSize()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .hoverable(interactionSource)
            .pointerHoverIcon(PointerIcon.Hand)
            .clickable { onRefresh() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(Res.drawable.refresh),
            contentDescription = "Refresh composition",
            modifier = Modifier.size(32.dp)
        )
        DefaultPanelText(
            text = "Refresh",
            modifier = Modifier.padding(start = 4.dp)
        )
    }
}

@Composable
fun FilterSystem(
    checked: Boolean,
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
            text = "Filter system",
        )
    }
}

@Composable
fun FilterOption(
    selected: Boolean,
    text: String,
    onSelected: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    Row(
        Modifier
            .wrapContentSize()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .hoverable(interactionSource)
            .pointerHoverIcon(PointerIcon.Hand)
            .toggleable(
                value = selected,
                onValueChange = { onSelected() },
                role = Role.RadioButton
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = null
        )
        DefaultPanelText(
            text = text,
            modifier = Modifier.padding(start = 4.dp)
        )
    }
}

enum class NodeKind(val tag: String) {
    ALL("All"),
    RECOMPOSE_SCOPE("RecomposeScope"),
    LAYOUT_NODE("LayoutNode"),
    SUBCOMPOSITION("Subcomposition")
}

sealed interface GroupOrigin
data object System : GroupOrigin
data object App : GroupOrigin
