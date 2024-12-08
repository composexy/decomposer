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
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.LocalContentColor
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.decomposer.server.Session
import decomposer.composeapp.generated.resources.Res
import decomposer.composeapp.generated.resources.expand_data
import decomposer.composeapp.generated.resources.fold_data
import decomposer.composeapp.generated.resources.refresh
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import kotlin.reflect.KClass

@Composable
fun CompositionPanel(
    modifier: Modifier = Modifier,
    session: Session,
    navigationContext: NavigationContext?,
    onShowPopup: (@Composable () -> Unit) -> Unit
) {
    var loading: Boolean by remember {
        mutableStateOf(true)
    }

    var full: FilterableTree by remember { mutableStateOf(FilterableTree.EMPTY_TREE) }

    val core: FilterableTree by remember {
        derivedStateOf { full.subtree(CoreGroup::class.withSlots()) }
    }

    var hideWrapper: Boolean by remember {
        mutableStateOf(false)
    }

    val tree: FilterableTree by remember {
        derivedStateOf { if (hideWrapper) core else full }
    }

    var selectedTreeKind: TreeKind by remember { mutableStateOf(TreeKind.FULL) }

    var hideEmpty: Boolean by remember { mutableStateOf(true) }
    var hideLeaf: Boolean by remember { mutableStateOf(false) }
    var keepLevel: Boolean by remember { mutableStateOf(false) }

    val subtree: FilterableTree by remember {
        derivedStateOf {
            when (selectedTreeKind) {
                TreeKind.FULL -> tree
                TreeKind.RECOMPOSE_SCOPE -> tree.subtree(RecomposeScopeGroup::class.withSlots())
                TreeKind.COMPOSE_NODE -> tree.subtree(ComposeNodeGroup::class.withSlots())
                TreeKind.COMPOSITION -> tree.subtree(CompositionGroup::class.withSlots())
            }
        }
    }

    val coroutineScope = rememberCoroutineScope { Dispatchers.Default }

    fun loadCompositionTree() {
        coroutineScope.launch {
            loading = true
            val compositionRoots = session.getCompositionData()
            full = compositionRoots.buildCompositionTree(
                navigationContext = navigationContext,
                showContextPopup = { onShowPopup(it) },
                sourceNavigation = { path, start, end ->
                    println("$path, $start, $end")
                }
            )
            loading = false
        }
    }

    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth().wrapContentHeight()
        ) {
            CompositionPanelBar(
                modifier = Modifier.wrapContentHeight().fillMaxWidth(),
                loading = loading,
                onRefresh = {
                    coroutineScope.launch { loadCompositionTree() }
                },
                hideWrapper = hideWrapper,
                hideEmpty = hideEmpty,
                hideLeaf = hideLeaf,
                keepLevel = keepLevel,
                onHideWrapperCheckedChanged = { hideWrapper = it },
                onHideEmptyCheckedChanged = { hideEmpty = it },
                onHideLeafCheckedChanged = { hideLeaf = it },
                onKeepLevelChanged = { keepLevel = it }
            )
        }
        if (!loading) {
            Row(modifier = Modifier.fillMaxWidth()) {
                TreeExpander(
                    onFoldAll = {
                        full.root.setExpandedRecursive(false)
                    },
                    onExpandAll = {
                        full.root.setExpandedRecursive(true)
                    }
                )
                DataExpander(
                    onFoldData = {
                        full.root.addExcludesRecursive(setOf(SlotNode::class))
                    },
                    onExpandData = {
                        full.root.removeExcludesRecursive(setOf(SlotNode::class))
                    }
                )
            }

            Box(
                modifier = Modifier.fillMaxWidth().weight(1.0f)
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
                        val nodes = subtree.flattenNodes
                        items(nodes.size) {
                            nodes[it].TreeNodeIndented(keepLevel)
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

            HorizontalSplitter()

            SubTreeSelector(
                modifier = Modifier.wrapContentSize()
                    .padding(vertical = 12.dp)
                    .align(Alignment.CenterHorizontally),
                selectedTreeKind = selectedTreeKind,
                onSelectedOption = { selectedTreeKind = it }
            )
        } else {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }
    }

    LaunchedEffect(full, hideLeaf) {
        if (hideLeaf) {
            full.root.addExcludesRecursive(setOf(LeafGroup::class))
        } else {
            full.root.removeExcludesRecursive(setOf(LeafGroup::class))
        }
    }

    LaunchedEffect(full, hideEmpty) {
        if (hideEmpty) {
            full.root.addExcludesRecursive(setOf(EmptyGroup::class))
        } else {
            full.root.removeExcludesRecursive(setOf(EmptyGroup::class))
        }
    }

    LaunchedEffect(session) {
        loadCompositionTree()
    }
}

@Composable
fun SubTreeSelector(
    modifier: Modifier,
    selectedTreeKind: TreeKind,
    onSelectedOption: (TreeKind) -> Unit,
) {
    SingleChoiceSegmentedButtonRow(modifier = modifier) {
        val entries = TreeKind.entries
        TreeKind.entries.forEachIndexed { index, kind ->
            val interactionSource = remember { MutableInteractionSource() }
            SegmentedButton(
                modifier = Modifier
                    .hoverable(interactionSource)
                    .pointerHoverIcon(PointerIcon.Hand),
                shape = SegmentedButtonDefaults.itemShape(index = index, count = entries.size),
                onClick = { onSelectedOption(entries[index]) },
                selected = entries[index] == selectedTreeKind,
                colors = SegmentedButtonDefaults.colors().copy(
                    activeContainerColor = Color.Transparent,
                    inactiveContainerColor = Color.Transparent,
                    activeContentColor = LocalContentColor.current
                )
            ) {
                DefaultPanelText(kind.tag)
            }
        }
    }
}

@Composable
fun DataExpander(
    onExpandData: () -> Unit,
    onFoldData: () -> Unit
) {
    Row(
        modifier = Modifier.wrapContentSize()
    ) {
        val interactionSource = remember { MutableInteractionSource() }
        Row(
            Modifier
                .wrapContentSize()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(Res.drawable.expand_data),
                contentDescription = "Expand all data",
                modifier = Modifier.size(32.dp)
                    .hoverable(interactionSource)
                    .pointerHoverIcon(PointerIcon.Hand)
                    .clickable { onExpandData() }
            )
            Image(
                painter = painterResource(Res.drawable.fold_data),
                contentDescription = "Fold all data",
                modifier = Modifier.size(32.dp)
                    .hoverable(interactionSource)
                    .pointerHoverIcon(PointerIcon.Hand)
                    .clickable { onFoldData() }
            )
        }
    }
}

@Composable
fun CompositionPanelBar(
    modifier: Modifier,
    loading: Boolean,
    onRefresh: () -> Unit,
    hideWrapper: Boolean,
    hideEmpty: Boolean,
    hideLeaf: Boolean,
    keepLevel: Boolean,
    onHideWrapperCheckedChanged: (Boolean) -> Unit,
    onHideEmptyCheckedChanged: (Boolean) -> Unit,
    onHideLeafCheckedChanged: (Boolean) -> Unit,
    onKeepLevelChanged: (Boolean) -> Unit
) {
    Row(modifier = modifier) {
        CompositionRefresh(loading, onRefresh)
        ComposeCheckBox(
            text = "Hide wrapper groups",
            checked = hideWrapper,
            onCheckedChanged = { onHideWrapperCheckedChanged(it) }
        )
        ComposeCheckBox(
            text = "Hide empty groups",
            checked = hideEmpty,
            onCheckedChanged = { onHideEmptyCheckedChanged(it) }
        )
        ComposeCheckBox(
            text = "Hide leaf groups",
            checked = hideLeaf,
            onCheckedChanged = { onHideLeafCheckedChanged(it) }
        )
        ComposeCheckBox(
            text = "Keep subtree level",
            checked = keepLevel,
            onCheckedChanged = { onKeepLevelChanged(it) }
        )
    }
}

@Composable
fun CompositionRefresh(loading: Boolean, onRefresh: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    Row(
        modifier = Modifier
            .wrapContentSize()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .run {
                if (!loading) {
                    this.hoverable(interactionSource)
                        .pointerHoverIcon(PointerIcon.Hand)
                        .clickable { onRefresh() }
                } else {
                    this
                }
            },
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
fun ComposeCheckBox(
    text: String,
    checked: Boolean,
    onCheckedChanged: (Boolean) -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    Row(
        modifier = Modifier
            .wrapContentSize()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .hoverable(interactionSource)
            .pointerHoverIcon(PointerIcon.Hand)
            .toggleable(
                value = checked,
                onValueChange = { onCheckedChanged(!checked) },
                role = Role.Checkbox
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = null
        )
        DefaultPanelText(
            text = text,
            modifier = Modifier.padding(start = 4.dp)
        )
    }
}

private fun KClass<*>.withSlots(): List<KClass<*>> {
    return listOf(this, SlotNode::class)
}

enum class TreeKind(val tag: String) {
    FULL("Full"),
    RECOMPOSE_SCOPE("RecomposeScope"),
    COMPOSE_NODE("ComposeNode"),
    COMPOSITION("Composition")
}

sealed interface ComposeTag
data object RecomposeScopeGroup : ComposeTag
data object ComposeNodeGroup : ComposeTag
data object CompositionGroup : ComposeTag
data object WrapperGroup : ComposeTag
data object CoreGroup : ComposeTag
data object EmptyGroup : ComposeTag
data object LeafGroup : ComposeTag
data object SlotNode : ComposeTag
