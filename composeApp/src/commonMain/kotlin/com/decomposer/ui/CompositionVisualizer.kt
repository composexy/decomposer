package com.decomposer.ui

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidthIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.LocalContentColor
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.decomposer.runtime.connection.model.ComposableLambdaImpl
import com.decomposer.runtime.connection.model.ComposeState
import com.decomposer.runtime.connection.model.CompositionContextHolder
import com.decomposer.runtime.connection.model.CompositionRoot
import com.decomposer.runtime.connection.model.CompositionRoots
import com.decomposer.runtime.connection.model.Context
import com.decomposer.runtime.connection.model.Coordinator
import com.decomposer.runtime.connection.model.Data
import com.decomposer.runtime.connection.model.Group
import com.decomposer.runtime.connection.model.IntKey
import com.decomposer.runtime.connection.model.LayoutNode
import com.decomposer.runtime.connection.model.ModifierNode
import com.decomposer.runtime.connection.model.ObjectKey
import com.decomposer.runtime.connection.model.RecomposeScope
import com.decomposer.runtime.connection.model.RememberObserverHolder
import com.decomposer.runtime.connection.model.SubcomposeState
import decomposer.composeapp.generated.resources.Res
import decomposer.composeapp.generated.resources.data
import decomposer.composeapp.generated.resources.empty_group
import decomposer.composeapp.generated.resources.expand_data
import decomposer.composeapp.generated.resources.expand_down
import decomposer.composeapp.generated.resources.expand_right
import decomposer.composeapp.generated.resources.fold_data
import decomposer.composeapp.generated.resources.group_attributes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource

@Composable
private fun GroupItem(
    modifier: Modifier = Modifier,
    node: BaseTreeNode,
    clickable: Boolean = false,
    onClick: () -> Unit = { }
) {
    Row(modifier = modifier.wrapContentHeight()) {
        val interactionSource = remember { MutableInteractionSource() }
        val fontSize = AppSetting.fontSize
        GroupIcon(Modifier.align(Alignment.CenterVertically), node)
        Text(
            text = node.name,
            style = if (clickable) {
                TextStyle(
                    color = LocalContentColor.current,
                    textDecoration = TextDecoration.Underline
                )
            } else {
                LocalTextStyle.current
            },
            modifier = Modifier
                .align(Alignment.CenterVertically)
                .clipToBounds()
                .wrapContentSize()
                .run {
                    if (clickable) {
                        this.hoverable(interactionSource)
                            .pointerHoverIcon(PointerIcon.Hand)
                            .clickable { onClick() }
                    } else {
                        this
                    }
                },
            softWrap = true,
            overflow = TextOverflow.Ellipsis,
            maxLines = 1,
            fontFamily = Fonts.jetbrainsMono(),
            fontSize = fontSize.sp,
            fontWeight = FontWeight.Light,
            lineHeight = (fontSize * 1.5).sp
        )
    }
}

@Composable
private fun DataItem(
    modifier: Modifier = Modifier,
    data: Data,
    expanded: Boolean,
    clickable: Boolean = true,
    onClick: () -> Unit = {},
    showIcon: Boolean = true,
    expandedContent: @Composable () -> Unit = {}
) {
    Box(modifier = modifier) {
        Column(modifier = Modifier.wrapContentHeight().fillMaxWidth()) {
            Row(modifier = Modifier.wrapContentHeight().fillMaxWidth()) {
                val interactionSource = remember { MutableInteractionSource() }
                if (showIcon) {
                    DataIcon(Modifier.align(Alignment.CenterVertically), data)
                }
                val fontSize = AppSetting.fontSize
                Text(
                    text = data.toString,
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .wrapContentSize()
                        .run {
                            if (clickable) {
                                this.hoverable(interactionSource)
                                    .pointerHoverIcon(PointerIcon.Hand)
                                    .clickable { onClick() }
                            } else {
                                this
                            }
                        },
                    softWrap = true,
                    maxLines = 1,
                    fontFamily = Fonts.jetbrainsMono(),
                    fontSize = fontSize.sp,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.Thin,
                    lineHeight = (fontSize * 1.5).sp
                )
            }
            if (expanded) {
                expandedContent()
            }
        }
    }
}

@Composable
private fun GroupIcon(modifier: Modifier, node: BaseTreeNode) {
    Box(
        modifier = modifier
            .wrapContentSize()
            .padding(4.dp)
            .run {
                if (node.children.isNotEmpty()) {
                    clickable {
                        node.expanded = !node.expanded
                    }
                } else {
                    this
                }
            }
    ) {
        val isNodeEmpty = when {
            node.children.isEmpty() -> true
            node is GroupNode && node.children.all { it is DataNode } -> true
            else -> false
        }
        when {
            isNodeEmpty -> {
                Image(
                    painter = painterResource(Res.drawable.empty_group),
                    contentDescription = "Empty group",
                    modifier = Modifier.size(32.dp),
                )
            }
            node.expanded -> {
                val interactionSource = remember { MutableInteractionSource() }
                Image(
                    painter = painterResource(Res.drawable.expand_down),
                    contentDescription = "Fold ${node.name}",
                    modifier = Modifier
                        .size(32.dp)
                        .hoverable(interactionSource)
                        .pointerHoverIcon(PointerIcon.Hand),
                )
            }
            else -> {
                val interactionSource = remember { MutableInteractionSource() }
                Image(
                    painter = painterResource(Res.drawable.expand_right),
                    contentDescription = "Unfold ${node.name}",
                    modifier = Modifier
                        .size(32.dp)
                        .hoverable(interactionSource)
                        .pointerHoverIcon(PointerIcon.Hand),
                )
            }
        }

    }
}

@Composable
private fun StateDetail(
    modifier: Modifier,
    contexts: Contexts,
    state: ComposeState,
    onClickDependency: (ComposeState) -> Unit = {}
) {
    with(contexts) {
        Column(modifier = modifier) {
            DefaultPanelText(
                modifier = Modifier.wrapContentHeight().fillMaxWidth().padding(vertical = 2.dp),
                text = "Value: ${state.value.toString}",
                textAlign = TextAlign.Start,
                maxLines = Int.MAX_VALUE
            )
            val readers = mutableListOf<String>()
            if (state.readInComposition == true) readers.add("Composition")
            if (state.readInSnapshotStateObserver == true) readers.add("SnapshotStateObserver")
            if (state.readInSnapshotFlow == true) readers.add("SnapshotFlow")
            DefaultPanelText(
                modifier = Modifier.wrapContentHeight().fillMaxWidth().padding(vertical = 2.dp),
                text = "Readers: ${readers.joinToString(", ")}",
                textAlign = TextAlign.Start
            )
            val hasDependencies = state.dependencyHashes.isNotEmpty()
            if (hasDependencies) {
                HorizontalSplitter()
            }
            DefaultPanelText(
                modifier = Modifier.wrapContentHeight().fillMaxWidth().padding(vertical = 2.dp),
                text = "Dependencies: ${if (!hasDependencies) "None" else ""}",
                textAlign = TextAlign.Start
            )
            state.dependencyHashes.forEach {
                statesByHash[it]?.let { dependency ->
                    StateItem(
                        modifier = Modifier.padding(vertical = 2.dp),
                        state = dependency,
                        expanded = false,
                        clickable = true,
                        contexts = contexts
                    ) {
                        onClickDependency(dependency)
                    }
                }
            }
        }
    }
}

@Composable
private fun ExpandedComposableLambdaImpl(
    modifier: Modifier,
    contexts: Contexts,
    composableLambdaImpl: ComposableLambdaImpl
) {
    with(contexts) {
        val verticalScrollState = rememberScrollState()

        Box(modifier = modifier.fillMaxSize()) {
            Column(modifier = Modifier.verticalScroll(verticalScrollState)) {
                DefaultPanelText(
                    text = "Key: ${composableLambdaImpl.key}",
                    textAlign = TextAlign.Start
                )
                HorizontalSplitter()
                composableLambdaImpl.block?.let {
                    DefaultPanelText(
                        text = "Block:",
                        textAlign = TextAlign.Start
                    )
                    ExpandedDataDefault(
                        modifier = Modifier.fillMaxWidth().wrapContentHeight(),
                        contexts = contexts,
                        data = it
                    )
                }
                HorizontalSplitter()
                DefaultPanelText(
                    text = "Tracked: ${composableLambdaImpl.tracked}",
                    textAlign = TextAlign.Start
                )
                val recomposeScope = composableLambdaImpl.scopeHash?.let {
                    recomposeScopesByHash[it]
                }
                DefaultPanelText(
                    text = "RecomposeScope: ${ if(recomposeScope == null) "Empty" else "" }",
                    textAlign = TextAlign.Start
                )
                recomposeScope?.let { scope ->
                    DataItem(data = scope, expanded = false, showIcon = false, onClick = {
                        window(
                            "RecomposeScope@${scope.hashCode}" to @Composable {
                                ExpandedRecomposeScope(
                                    modifier = Modifier.fillMaxSize(),
                                    contexts = contexts,
                                    recomposeScope = scope
                                )
                            }
                        )
                    })
                }
                val scopes = composableLambdaImpl.scopeHashes.mapNotNull {
                    recomposeScopesByHash[it]
                }
                DefaultPanelText(
                    text = "RecomposeScopes: ${ if(scopes.isEmpty()) "Empty" else "" }",
                    textAlign = TextAlign.Start
                )
                if (scopes.isNotEmpty()) {
                    scopes.forEach { scope ->
                        DataItem(data = scope, expanded = false, showIcon = false, onClick = {
                            window(
                                "RecomposeScope@${scope.hashCode}" to @Composable {
                                    ExpandedRecomposeScope(
                                        modifier = Modifier.fillMaxSize(),
                                        contexts = contexts,
                                        recomposeScope = scope
                                    )
                                }
                            )
                        })
                    }
                }
            }

            VerticalScrollbar(
                modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
                adapter = rememberScrollbarAdapter(verticalScrollState)
            )
        }
    }
}

@Composable
private fun ExpandedCompositionContextHolder(
    modifier: Modifier,
    contexts: Contexts,
    compositionContextHolder: CompositionContextHolder
) {
    with(contexts) {
        Column(modifier = modifier) {
            DefaultPanelText(text = "Reference:", textAlign = TextAlign.Start)
            ExpandedContext(
                modifier = Modifier.fillMaxWidth().wrapContentHeight(),
                contexts = contexts,
                context = compositionContextHolder.ref
            )
        }
    }
}

@Composable
private fun ExpandedContext(
    modifier: Modifier,
    contexts: Contexts,
    context: Context
) {
    DefaultPanelText(
        modifier = modifier,
        text = "CompoundHashKey: ${context.compoundHashKey}",
        textAlign = TextAlign.Start
    )
}

@Composable
private fun ExpandedLayoutNode(
    modifier: Modifier,
    contexts: Contexts,
    layoutNode: LayoutNode
) {
    with(contexts) {
        val verticalScrollState = rememberScrollState()
        Box(modifier = modifier) {
            Column(modifier = modifier) {
                DefaultPanelText(
                    text = "LayoutNode: ${layoutNode.toString}",
                    maxLines = Int.MAX_VALUE,
                    textAlign = TextAlign.Start,
                    clickable = false
                )

                val lookahead = layoutNode.lookaheadRootHash?.let {
                    layoutNodesByHash[it]
                }
                if (lookahead != null) {
                    DefaultPanelText(
                        text = "Lookahead root: ${lookahead.toString}",
                        maxLines = Int.MAX_VALUE,
                        textAlign = TextAlign.Start,
                        clickable = true
                    ) {
                        window(
                            "LayoutNode@${lookahead.hashCode}" to @Composable {
                                ExpandedLayoutNode(
                                    modifier = Modifier.fillMaxSize(),
                                    contexts = contexts,
                                    layoutNode = lookahead
                                )
                            }
                        )
                    }
                }

                val parent = layoutNode.parentHash?.let {
                    layoutNodesByHash[it]
                }
                if (parent != null) {
                    DefaultPanelText(
                        text = "Parent: ${parent.toString}",
                        maxLines = Int.MAX_VALUE,
                        textAlign = TextAlign.Start,
                        clickable = true
                    ) {
                        window(
                            "LayoutNode@${parent.hashCode}" to @Composable {
                                ExpandedLayoutNode(
                                    modifier = Modifier.fillMaxSize(),
                                    contexts = contexts,
                                    layoutNode = parent
                                )
                            }
                        )
                    }
                }

                val children = layoutNode.childrenHashes.mapNotNull {
                    layoutNodesByHash[it]
                }
                if (children.isNotEmpty()) {
                    DefaultPanelText(text = "Children:")
                    children.forEachIndexed { index, child ->
                        DefaultPanelText(
                            text = "Child $index: ${child.toString}",
                            maxLines = Int.MAX_VALUE,
                            textAlign = TextAlign.Start,
                            clickable = true
                        ) {
                            window(
                                "LayoutNode@${child.hashCode}" to @Composable {
                                    ExpandedLayoutNode(
                                        modifier = Modifier.fillMaxSize(),
                                        contexts = contexts,
                                        layoutNode = child
                                    )
                                }
                            )
                        }
                    }
                }

                DefaultPanelText(text = "Coordinators and contained nodes:")
                var currentNodeIndex = -1
                layoutNode.coordinators.forEachIndexed { index, coordinator ->
                    val tail = layoutNode.nodes.firstOrNull {
                        it.hashCode == coordinator.tailNodeHash
                    }
                    RowWithLineNumber(index + 1, layoutNode.coordinators.size) {
                        DefaultPanelText(
                            text = "Coordinator: ${coordinator.toString}",
                            textAlign = TextAlign.Start,
                            clickable = true
                        ) {
                            window(
                                "Coordinator@${coordinator.hashCode}" to @Composable {
                                    ExpandedCoordinator(
                                        modifier = modifier.fillMaxSize(),
                                        contexts = contexts,
                                        coordinator = coordinator,
                                        tail = tail
                                    )
                                }
                            )
                        }
                    }

                    do {
                        val node = layoutNode.nodes[++currentNodeIndex]
                        Box(modifier = Modifier.padding(start = 24.dp)) {
                            RowWithLineNumber(currentNodeIndex + 1, layoutNode.nodes.size) {
                                DefaultPanelText(
                                    text = "ModifierNode: ${node.toString}",
                                    textAlign = TextAlign.Start,
                                    clickable = true
                                ) {
                                    window(
                                        "ModifierNode@${coordinator.hashCode}" to @Composable {
                                            ExpandedModifierNode(
                                                modifier = modifier.fillMaxSize(),
                                                contexts = contexts,
                                                node = node
                                            )
                                        }
                                    )
                                }
                            }
                        }
                    } while (tail != node)
                }
            }

            VerticalScrollbar(
                modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
                adapter = rememberScrollbarAdapter(verticalScrollState)
            )
        }
    }
}

@Composable
private fun ExpandedCoordinator(
    modifier: Modifier,
    contexts: Contexts,
    coordinator: Coordinator,
    tail: ModifierNode?
) {
    with(contexts) {
        Column(modifier = modifier) {
            DefaultPanelText(
                text = "Coordinator:",
                textAlign = TextAlign.Start
            )
            DefaultPanelText(
                text = coordinator.toString,
                maxLines = Int.MAX_VALUE,
                textAlign = TextAlign.Start
            )
            HorizontalSplitter()
            DefaultPanelText(
                text = "Tail node: ${ if (tail == null) "None" else "" }",
                textAlign = TextAlign.Start
            )
            tail?.let {
                ExpandedModifierNode(
                    modifier = Modifier.fillMaxWidth().wrapContentHeight(),
                    contexts = contexts,
                    node = tail
                )
            }
        }
    }
}

@Composable
private fun ExpandedModifierNode(
    modifier: Modifier,
    contexts: Contexts,
    node: ModifierNode
) {
    with(contexts) {
        Column(modifier = modifier) {
            DefaultPanelText(
                text = "ModifierNode:",
                textAlign = TextAlign.Start
            )
            DefaultPanelText(
                text = node.toString,
                maxLines = Int.MAX_VALUE,
                textAlign = TextAlign.Start
            )
        }

    }
}

@Composable
private fun ExpandedRecomposeScope(
    modifier: Modifier,
    contexts: Contexts,
    recomposeScope: RecomposeScope
) {
    with(contexts) {
        Column(
            modifier = modifier
        ) {
            val states = recomposeScope.composeStateHashes.mapNotNull {
                statesByHash[it]
            }
            DefaultPanelText(
                text = "States in scope: ${if (states.isEmpty()) "None" else ""}",
                modifier = Modifier.fillMaxWidth().wrapContentHeight(),
                textAlign = TextAlign.Start
            )
            ExpandedStatesTable(states, contexts)
        }
    }
}

@Composable
private fun ExpandedStatesTable(
    states: List<ComposeState>,
    contexts: Contexts
) {
    val verticalScrollState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val expandedMap: SnapshotStateMap<Int, Boolean> = remember {
        mutableStateMapOf()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            state = verticalScrollState
        ) {
            items(states.size) {
                RowWithLineNumber(lineNumber = it + 1, lines = states.size) {
                    StateItem(
                        modifier = Modifier.padding(4.dp),
                        state = states[it],
                        expanded = expandedMap[it] ?: false,
                        contexts = contexts,
                        onClickDependency = { dependency ->
                            val index = states.indexOf(dependency)
                            if (index != -1) {
                                coroutineScope.launch {
                                    verticalScrollState.animateScrollToItem(index)
                                    expandedMap[index] = true
                                }
                            }
                        }
                    ) {
                        expandedMap[it] = !(expandedMap[it] ?: false)
                    }
                }
            }
        }

        VerticalScrollbar(
            modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
            adapter = rememberScrollbarAdapter(verticalScrollState)
        )
    }
}

@Composable
private fun StateItem(
    modifier: Modifier = Modifier,
    state: ComposeState,
    expanded: Boolean,
    contexts: Contexts,
    clickable: Boolean = true,
    onClickDependency: (ComposeState) -> Unit = {},
    onClick: () -> Unit = {},
) {
    Box(modifier = modifier) {
        Column(modifier = Modifier.wrapContentHeight().fillMaxWidth().animateContentSize()) {
            val interactionSource = remember { MutableInteractionSource() }
            val fontSize = AppSetting.fontSize
            Text(
                text = state.toString,
                modifier = Modifier
                    .clipToBounds()
                    .run {
                        if (clickable) {
                            this.hoverable(interactionSource)
                                .pointerHoverIcon(PointerIcon.Hand)
                                .clickable { onClick() }
                        } else {
                            this
                        }
                    }
                    .basicMarquee(velocity = 160.dp),
                softWrap = true,
                maxLines = 1,
                fontFamily = Fonts.jetbrainsMono(),
                fontSize = fontSize.sp,
                overflow = TextOverflow.Ellipsis,
                fontWeight = FontWeight.Thin,
                lineHeight = (fontSize * 1.5).sp
            )
            if (expanded) {
                StateDetail(
                    modifier = Modifier.fillMaxWidth()
                        .wrapContentHeight()
                        .background(Color(0x08FFFFFF))
                        .padding(horizontal = 48.dp),
                    contexts = contexts,
                    state = state,
                    onClickDependency = onClickDependency
                )
            }
        }
    }
}

@Composable
private fun ExpandedRoots(
    rootsNode: CompositionRoots,
    contexts: Contexts
) {
    ExpandedStatesTable(rootsNode.stateTable.toList(), contexts)
}

@Composable
private fun ExpandedDataDefault(
    modifier: Modifier,
    contexts: Contexts,
    data: Data
) {
    with(contexts) {
        Column(modifier) {
            DefaultPanelText(
                text = "${data.typeName ?: "<anonymous>"}@${data.hashCode}",
                textAlign = TextAlign.Start
            )
            DefaultPanelText(
                text = "toString: ${data.toString}",
                maxLines = Int.MAX_VALUE,
                textAlign = TextAlign.Start
            )
        }
    }
}

@Composable
private fun GroupAttributes(
    modifier: Modifier,
    contexts: Contexts,
    group: Group
) {
    val keyInfo = when(val key = group.attributes.key) {
        is IntKey -> key.value
        is ObjectKey -> key.value
    }
    with(contexts) {
        Column(modifier) {
            DefaultPanelText(text = "Key: $keyInfo", textAlign = TextAlign.Start)
            DefaultPanelText(
                text = "Source info: ${group.attributes.sourceInformation}",
                maxLines = Int.MAX_VALUE,
                textAlign = TextAlign.Start
            )
        }
    }
}

@Composable
private fun ExpandedRememberObserverHolder(
    modifier: Modifier,
    contexts: Contexts,
    rememberObserverHolder: RememberObserverHolder
) {
    with(contexts) {
        Column(modifier = modifier) {
            DefaultPanelText(text = "RememberObserverHolder:", textAlign = TextAlign.Start)
            DataItem(
                data = rememberObserverHolder.wrapped,
                expanded = false,
                showIcon = false,
                clickable = false
            )
        }
    }
}

@Composable
private fun DataIcon(modifier: Modifier, data: Data) {
    Box(
        modifier = modifier
            .wrapContentSize()
            .padding(4.dp)
    ) {
        Image(
            painter = painterResource(Res.drawable.data),
            contentDescription = data::class.simpleName,
            modifier = Modifier.size(32.dp),
        )
    }
}

@Composable
private fun ShowDataIcon(
    modifier: Modifier,
    expanded: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    Box(
        modifier = modifier
            .wrapContentSize()
            .padding(4.dp)
            .hoverable(interactionSource)
            .pointerHoverIcon(PointerIcon.Hand)
            .clickable { onClick() }
    ) {
        if (expanded) {
            Image(
                painter = painterResource(Res.drawable.fold_data),
                contentDescription = "Fold data",
                modifier = Modifier.size(32.dp),
            )
        } else {
            Image(
                painter = painterResource(Res.drawable.expand_data),
                contentDescription = "Expand data",
                modifier = Modifier.size(32.dp),
            )
        }
    }
}

@Composable
private fun GroupAttributesIcon(
    modifier: Modifier,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    Box(
        modifier = modifier
            .wrapContentSize()
            .padding(4.dp)
            .hoverable(interactionSource)
            .pointerHoverIcon(PointerIcon.Hand)
            .clickable { onClick() }
    ) {
        Image(
            painter = painterResource(Res.drawable.group_attributes),
            contentDescription = "Group attributes",
            modifier = Modifier.size(32.dp),
        )
    }
}

fun CompositionRoots.buildCompositionTree(
    navigationContext: NavigationContext?,
    showContextPopup: (@Composable () -> Unit) -> Unit,
    showContextWindow: (Pair<String, @Composable () -> Unit>) -> Unit,
    sourceNavigation: (String, Int, Int) -> Unit
): FilterableTree {
    val contexts = this.buildContexts(
        navigationContext = navigationContext,
        popup = showContextPopup,
        window = showContextWindow,
        navigate = sourceNavigation
    )
    return FilterableTree(
        root = RootsNode(this, contexts)
    )
}

private sealed class BaseComposeTreeNode : BaseTreeNode() {
    override fun compareTo(other: TreeNode): Int {
        if (other !is BaseComposeTreeNode){
            return 0
        }
        val selfOrder = sortOrderOf(this)
        val otherOrder = sortOrderOf(other)
        if (selfOrder != otherOrder) {
            return selfOrder - otherOrder
        }
        return 0
    }

    private fun sortOrderOf(node: BaseComposeTreeNode): Int {
        return when(node) {
            is CompositionNode -> 3
            is DataNode -> 1
            is GroupNode -> 2
            is RootsNode -> 0
            is SubcompositionsNode -> 4
        }
    }
}

private class RootsNode(
    private val compositionRoots: CompositionRoots,
    private val contexts: Contexts
) : BaseComposeTreeNode() {
    override val level = 0
    override val name = "Application"
    override val children: List<TreeNode> = compositionRoots.compositionData.map {
        CompositionNode(
            compositionRoot = it,
            level = level + 1,
            contexts = contexts
        )
    }
    override val tags: Set<Any> = emptySet()

    @Composable
    override fun TreeNode() = with(contexts) {
        GroupItem(node = this@RootsNode, clickable = true) {
            window("App States" to @Composable { ExpandedRoots(compositionRoots, contexts) })
        }
    }
}

private class CompositionNode(
    private val compositionRoot: CompositionRoot,
    private val contexts: Contexts,
    override val level: Int
) : BaseComposeTreeNode() {
    override val name = "Composition(${compositionRoot.context?.compoundHashKey ?: "Recomposer"})"
    override val children: List<TreeNode> = compositionRoot.groups.map {
        GroupNode(
            group = it,
            level = level + 1,
            contexts = contexts
        )
    }
    override val tags: Set<Any> = setOf(CompositionGroup)

    @Composable
    override fun TreeNode()  {
        GroupItem(node = this@CompositionNode)
    }
}

private class GroupNode(
    private val group: Group,
    private val contexts: Contexts,
    override val level: Int
) : BaseComposeTreeNode() {
    override val name = group.name
    private val _tags = mutableSetOf<Any>()
    private val _children = mutableListOf<TreeNode>()
    override val children: List<TreeNode>
        get() = _children
    override val tags: Set<Any>
        get() = _tags

    var canNavigate: Boolean = false
    var sourceInformation: SourceInformation? = null

    init {
        with(contexts) {
            call(group) {
                this@GroupNode.sourceInformation = sourceInformation
                val packageHash = sourceInformation?.packageHash
                val fileName = sourceInformation?.fileName
                canNavigate = if (packageHash == null || navigationContext == null || fileName == null) {
                    false
                } else {
                    navigationContext.canNavigate(PackageHashWithFileName(packageHash, fileName))
                }
                _children.addAll(
                    group.data.map {
                        DataNode(
                            data = it,
                            level = level + 1,
                            contexts = contexts
                        )
                    }
                )
                _children.addAll(
                    group.children.map {
                        GroupNode(
                            group = it,
                            level = level + 1,
                            contexts = contexts
                        )
                    }
                )
                val subcomposeStates = group.data.filterIsInstance<SubcomposeState>()
                if (group.sourceKey == "SubcomposeLayout") {
                    _children.addAll(
                        subcomposeStates.map {
                            SubcompositionsNode(
                                subcomposeState = it,
                                level = level + 1,
                                contexts = contexts
                            )
                        }
                    )
                }

                when {
                    isWrapperGroup -> _tags.add(WrapperGroup)
                    isContentGroup -> _tags.add(ContentGroup)
                    isUserGroup -> _tags.add(UserGroup)
                }

                group.data.forEach {
                    when(it) {
                        is RecomposeScope -> _tags.add(RecomposeScopeGroup)
                        is LayoutNode -> _tags.add(ComposeNodeGroup)
                        else -> { }
                    }
                }
                if (group.children.isEmpty()) {
                    _tags.add(LeafGroup)
                    if (group.data.isEmpty()) {
                        _tags.add(EmptyGroup)
                    }
                }
                if (group.wellKnownKey == WellKnownKey.NODE) {
                    _tags.add(ComposeNodeGroup)
                }
                when(group.sourceKey) {
                    "SubcomposeLayout" -> _tags.add(CompositionGroup)
                    "ReusableComposeNode", "ComposeNode", "Layout" -> _tags.add(ComposeNodeGroup)
                    else -> { }
                }
            }
        }
    }

    @Composable
    override fun TreeNode() = with(contexts) {
        val coroutineScope = rememberCoroutineScope { Dispatchers.Default }
        Column(
            modifier = Modifier.wrapContentHeight().fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                GroupItem(node = this@GroupNode, clickable = canNavigate) {
                    coroutineScope.launch {
                        val navigationContext = navigationContext
                        val sourceInfo = sourceInformation
                        val packageHash = sourceInfo?.packageHash
                        val fileName = sourceInfo?.fileName
                        if (canNavigate && navigationContext != null && packageHash != null && fileName != null) {
                            val packageHashWithFileName = PackageHashWithFileName(
                                packageHash = packageHash,
                                fileName = fileName
                            )
                            val filePath = navigationContext.filePath(packageHashWithFileName)
                            val coordinates = navigationContext.getCoordinates(
                                packageHashWithFileName = packageHashWithFileName,
                                invocationLocations = sourceInfo.invocations.flatMap {
                                    listOf(it.startOffset, it.endOffset)
                                }
                            )
                            if (filePath != null && coordinates != null) {
                                navigate(filePath, coordinates.first, coordinates.second)
                            }
                        }
                    }
                }
                if (group.data.isNotEmpty()) {
                    val dataExpanded: Boolean by remember {
                        derivedStateOf { !excludes.contains(SlotNode::class) }
                    }
                    ShowDataIcon(
                        modifier = Modifier.align(Alignment.CenterVertically),
                        expanded = dataExpanded
                    ) {
                        val wasExpanded = dataExpanded
                        excludes = if (wasExpanded) {
                            excludes + SlotNode::class
                        } else {
                            excludes - SlotNode::class
                        }
                        this@GroupNode.children.forEach {
                            if (it is DataNode) {
                                it.excludes = if (wasExpanded) {
                                    it.excludes + SlotNode::class
                                } else {
                                    it.excludes - SlotNode::class
                                }
                            }
                        }
                    }
                }
                GroupAttributesIcon(
                    modifier = Modifier.align(Alignment.CenterVertically)
                ) {
                    popupGroup(group)
                }
            }
        }
    }

    private fun popupGroup(group: Group) = with(contexts) {
        popup @Composable {
            Box(
                modifier = Modifier.wrapContentWidth().wrapContentHeight()
            ) {
                GroupAttributes(
                    modifier = Modifier.wrapContentWidth().wrapContentHeight(),
                    contexts = contexts,
                    group = group
                )
            }
        }
    }
}

private class SubcompositionsNode(
    private val subcomposeState: SubcomposeState,
    private val contexts: Contexts,
    override val level: Int
) : BaseComposeTreeNode() {
    override val name: String = "Subcompositions"
    override val children: List<TreeNode> = subcomposeState.compositions.map {
        CompositionNode(
            compositionRoot = it,
            level = level + 1,
            contexts = contexts
        )
    }
    override val tags: Set<Any> = setOf(CompositionGroup)

    @Composable
    override fun TreeNode() {
        GroupItem(node = this@SubcompositionsNode)
    }
}

private class DataNode(
    private val data: Data,
    private val contexts: Contexts,
    override val level: Int
) : BaseComposeTreeNode() {
    override val name: String = data.toString
    override val children: List<TreeNode> = emptyList()
    override val tags: Set<Any> = setOf(SlotNode)

    @Composable
    override fun TreeNode() {
        DataItem(
            data = data,
            expanded = false,
            onClick = { showDetail(data) }
        )
    }

    private fun showDetail(data: Data) = with(contexts) {
        val composable = @Composable {
            Box(modifier = Modifier.fillMaxSize()) {
                when (data) {
                    is ComposableLambdaImpl -> ExpandedComposableLambdaImpl(
                        modifier = Modifier.fillMaxSize(),
                        contexts = contexts,
                        composableLambdaImpl = data
                    )
                    is ComposeState -> StateDetail(
                        modifier = Modifier.fillMaxSize(),
                        contexts = contexts,
                        state = data
                    )
                    is CompositionContextHolder -> ExpandedCompositionContextHolder(
                        modifier = Modifier.fillMaxSize(),
                        contexts = contexts,
                        compositionContextHolder = data
                    )
                    is Context -> ExpandedContext(
                        modifier = Modifier.fillMaxSize(),
                        contexts = contexts,
                        context = data
                    )
                    is LayoutNode -> ExpandedLayoutNode(
                        modifier = Modifier.fillMaxSize(),
                        contexts = contexts,
                        layoutNode = data
                    )
                    is RecomposeScope -> ExpandedRecomposeScope(
                        modifier = Modifier.fillMaxSize(),
                        contexts = contexts,
                        recomposeScope = data
                    )
                    is RememberObserverHolder -> ExpandedRememberObserverHolder(
                        modifier = Modifier.fillMaxSize(),
                        contexts = contexts,
                        rememberObserverHolder = data
                    )
                    else -> {
                        ExpandedDataDefault(
                            modifier = Modifier.fillMaxSize(),
                            contexts = contexts,
                            data = data
                        )
                    }
                }
            }
        }

        when (data) {
            is LayoutNode -> window("LayoutNode" to composable)
            is RecomposeScope -> window("RecomposeScope" to composable)
            is ComposeState -> window("State" to composable)
            is ComposableLambdaImpl -> window("ComposableLambda" to composable)
            else -> popup(composable)
        }
    }
}

private fun CompositionRoots.buildContexts(
    navigationContext: NavigationContext?,
    popup: (@Composable () -> Unit) -> Unit,
    window: (Pair<String, @Composable () -> Unit>) -> Unit,
    navigate: (String, Int, Int) -> Unit
): Contexts {
    val statesByHash = this.stateTable.associateBy { it.hashCode }
    val layoutNodesByHash = mutableMapOf<Int, LayoutNode>()
    val recomposeScopesByHash = mutableMapOf<Int, RecomposeScope>()

    fun traverse(group: Group) {
        group.data.forEach {
            when (it) {
                is LayoutNode -> layoutNodesByHash[it.hashCode] = it
                is RecomposeScope -> recomposeScopesByHash[it.hashCode] = it
                else -> { }
            }
        }
        group.children.forEach {
            traverse(it)
        }
    }

    this.compositionData.forEach {
        it.groups.forEach { group ->
            traverse(group)
        }
    }

    return Contexts(
        statesByHash = statesByHash,
        layoutNodesByHash = layoutNodesByHash,
        recomposeScopesByHash = recomposeScopesByHash,
        navigationContext = navigationContext,
        popup = popup,
        window = window,
        navigate = navigate
    )
}

private class Contexts(
    val statesByHash: Map<Int, ComposeState>,
    val layoutNodesByHash: Map<Int, LayoutNode>,
    val recomposeScopesByHash: Map<Int, RecomposeScope>,
    val navigationContext: NavigationContext?,
    val popup: (@Composable () -> Unit) -> Unit,
    val window: (Pair<String, @Composable () -> Unit>) -> Unit,
    val navigate: (String, Int, Int) -> Unit,
    val callStack: CallStack = CallStack(navigationContext)
) {
    fun <R> call(group: Group, block: CallScope.(Group) -> R): R {
        callStack.push(group)
        val result = callStack.block(group)
        callStack.pop()
        return result
    }
}

private class CallStack(private val navigationContext: NavigationContext?) : CallScope {
    private val stack = mutableListOf<Group>()
    private val sourceInformationStack = mutableListOf<SourceInformation?>()

    override val caller: Group?
        get() {
            return if (stack.size > 1) {
                stack[stack.size - 2]
            } else null
        }

    override val sourceInformation: SourceInformation?
        get() = sourceInformationStack.last()

    override val parentSourceInformation: SourceInformation?
        get() {
            return if (sourceInformationStack.size > 1) {
                sourceInformationStack[sourceInformationStack.size - 2]
            } else null
        }

    private var _isWrapperGroup: Boolean = true
    override val isWrapperGroup: Boolean
        get() = _isWrapperGroup
    private var _isContentGroup: Boolean = false
    override val isContentGroup: Boolean
        get() = _isContentGroup
    private var _isUserGroup: Boolean = false
    override val isUserGroup: Boolean
        get() = _isUserGroup

    private var topUserGroup: Group?  = null

    fun push(group: Group) {
        stack.add(group).also {
            if (isWrapperGroup) {
                if (group.isUiContent) {
                    _isWrapperGroup = false
                    _isContentGroup = true
                }
            }

            if (isContentGroup) {
                if (group.isUserGroup) {
                    _isContentGroup = false
                    _isUserGroup = true
                    topUserGroup = group
                }
            }

            val sourceInformation = try {
                group.parseSourceInformation
            } catch (ex: Exception) {
                println("Unexpected format: ${group.attributes.sourceInformation}")
                null
            }

            sourceInformationStack.add(sourceInformation)
        }
    }

    fun pop(): Group {
        return stack.removeLast().also {
            if (it === topUserGroup) {
                topUserGroup = null
                _isUserGroup = false
                _isContentGroup = true
            }
            if (!isWrapperGroup) {
                if (it.isUiContent) {
                    _isContentGroup = false
                    _isWrapperGroup = true
                }
            }
            sourceInformationStack.removeLast()
        }
    }

    private val Group.isUserGroup: Boolean
        get() {
            val packageHash = this.packageHash
            val fileName = this.sourceFile
            return when {
                packageHash == null || fileName == null || navigationContext == null -> false
                else -> navigationContext.canNavigate(
                    PackageHashWithFileName(packageHash, fileName)
                )
            }
        }

    private val Group.isUiContent: Boolean
        get() {
            return isComposeViewContent || isPopupContent || isDialogContent
        }

    private val Group.isComposeViewContent: Boolean
        get() {
            val sourceKey = this.sourceKey
            val sourceFile = this.sourceFile
            return sourceKey == "Content" && sourceFile == "ComposeView.android.kt"
        }

    private val Group.isPopupContent: Boolean
        get() {
            val sourceKey = this.sourceKey
            val sourceFile = this.sourceFile
            return sourceKey == "Content" && sourceFile == "AndroidPopup.android.kt"
        }

    private val Group.isDialogContent: Boolean
        get() {
            val sourceKey = this.sourceKey
            val sourceFile = this.sourceFile
            return sourceKey == "Content" && sourceFile == "AndroidDialog.android.kt"
        }
}

private interface CallScope {
    val caller: Group?
    val isWrapperGroup: Boolean
    val isContentGroup: Boolean
    val isUserGroup: Boolean
    val sourceInformation: SourceInformation?
    val parentSourceInformation: SourceInformation?
}

private class SourceInformation(
    val fileName: String?,
    val packageHash: String?,
    val isLambda: Boolean,
    val isCall: Boolean,
    val isInline: Boolean,
    val sourceName: String?,
    val invocations: List<Location>,
)

private class Location(
    val lineNumber: Int,
    val startOffset: Int,
    val endOffset: Int
)

private val Group.parseSourceInformation: SourceInformation?
    get() {
        val sourceInfo = this.attributes.sourceInformation ?: return null
        val sourceKey = this.sourceKey
        val isCall = sourceInfo.startsWith("C")
        val isInline = sourceInfo.startsWith("CC")
        val isLambda = sourceKey == null
        val fileName = this.sourceFile
        val packageHash = this.packageHash
        val indexOfLastParentheses = sourceInfo.indexOfLast { it == ')' }
        val locationStart = when {
            indexOfLastParentheses != -1 -> indexOfLastParentheses + 1
            isInline -> 2
            else -> 1
        }
        val indexOfFirstColon = sourceInfo.indexOf(':')
        val locationEnd = when {
            indexOfFirstColon == -1 -> sourceInfo.length
            else -> indexOfFirstColon
        }
        val locationParts = if (locationStart < locationEnd) {
            sourceInfo.substring(locationStart, locationEnd).split(",")
        } else {
            emptyList()
        }
        val invocations = locationParts.map {
            if (it.startsWith('*')) it.substring(1)
            else it
        }.mapNotNull {
            val startOffsetStart = it.indexOf('@') + 1
            val lineNumberEnd = startOffsetStart - 1
            val lengthStart = it.indexOf('L') + 1
            val startOffsetEnd = if (lengthStart == 0) {
                it.length
            } else {
                lengthStart - 1
            }
            val lineNumber = it.substring(0, lineNumberEnd).toIntOrNull()
            val startOffset = it.substring(startOffsetStart, startOffsetEnd).toIntOrNull()
            val length = if (lengthStart > 0) {
                it.substring(lengthStart).toIntOrNull()
            } else null
            if (lineNumber != null && startOffset != null && length != null) {
                Location(
                    lineNumber = lineNumber,
                    startOffset = startOffset,
                    endOffset = startOffset + length
                )
            } else {
                null
            }
        }
        return SourceInformation(
            fileName = fileName,
            packageHash = packageHash,
            isLambda = isLambda,
            isCall = isCall,
            isInline = isInline,
            sourceName = sourceKey,
            invocations = invocations
        )
    }

private val Group.name: String
    get() {
        val sourceInfo = this.attributes.sourceInformation
        val key = this.attributes.key
        val intKeyValue = if (key is IntKey) {
            key.value
        } else null
        val sourceKey = this.sourceKey
        val isLambda = sourceInfo != null && sourceKey == null
        val lambdaPrefix = if (isLambda) "\u03BB " else ""
        val wellKnownKey = this.wellKnownKey
        val intKey = if (key is IntKey) {
            "${lambdaPrefix}Group(${key.value})"
        } else null
        val objectKey = if (key is ObjectKey) {
            "${lambdaPrefix}Group(${key.value})"
        } else null
        return when {
            sourceKey != null -> "$sourceKey(${intKeyValue ?: ""})"
            wellKnownKey != null -> "Group(${wellKnownKey.displayName})"
            intKey != null -> intKey
            objectKey != null -> objectKey
            else -> "Group()"
        }
    }

private val Group.wellKnownKey: WellKnownKey?
    get() {
        val key = this.attributes.key
        return if (key is IntKey) {
            WellKnownKey.entries.firstOrNull {
                it.intKey == key.value
            }
        } else null
    }

private val Group.sourceKey: String?
    get() {
        val sourceInfo = this.attributes.sourceInformation
        return if (sourceInfo != null) {
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
    }

private val Group.sourceFile: String?
    get() {
        val sourceInfo = this.attributes.sourceInformation
        return if (sourceInfo != null) {
            val startIndex = sourceInfo.indexOf(':') + 1
            val endIndex = sourceInfo.indexOf('#')
            if (startIndex in 0 until endIndex)
                sourceInfo.substring(startIndex, endIndex)
            else null
        } else null
    }

private val Group.packageHash: String?
    get() {
        val sourceInfo = this.attributes.sourceInformation
        return if (sourceInfo != null) {
            val startIndex = sourceInfo.indexOfLast { it == '#' } + 1
            if (startIndex in 1 until sourceInfo.length)
                sourceInfo.substring(startIndex)
            else null
        } else null
    }

private enum class WellKnownKey(val intKey: Int, val displayName: String) {
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
