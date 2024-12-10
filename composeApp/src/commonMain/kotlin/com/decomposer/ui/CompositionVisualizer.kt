package com.decomposer.ui

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
import androidx.compose.foundation.layout.requiredHeightIn
import androidx.compose.foundation.layout.requiredWidth
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
    node: BaseTreeNode,
    clickable: Boolean = false,
    onClick: () -> Unit = { }
) {
    Row(
        modifier = Modifier.wrapContentHeight().fillMaxWidth()
    ) {
        val interactionSource = remember { MutableInteractionSource() }
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
            fontSize = 24.sp,
            fontWeight = FontWeight.Light,
            lineHeight = 36.sp
        )
    }
}

@Composable
private fun DataItem(
    modifier: Modifier = Modifier,
    data: Data,
    expanded: Boolean,
    onClick: () -> Unit,
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
                Text(
                    text = data.toString,
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .clipToBounds()
                        .hoverable(interactionSource)
                        .pointerHoverIcon(PointerIcon.Hand)
                        .clickable { onClick() }
                        .requiredWidthIn(80.dp, 5000.dp),
                    softWrap = true,
                    maxLines = 1,
                    fontFamily = Fonts.jetbrainsMono(),
                    fontSize = 24.sp,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.Thin,
                    lineHeight = 36.sp
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
private fun StateDetail(modifier: Modifier, contexts: Contexts, state: ComposeState) {
    with(contexts) {
        Column(modifier = modifier) {
            DefaultPanelText(
                modifier = Modifier.wrapContentHeight().fillMaxWidth().padding(vertical = 2.dp),
                text = "Value: ${state.value.toString}",
                textAlign = TextAlign.Start
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
                        clickable = false,
                        onClick = {},
                        contexts = contexts
                    )
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
        Column(modifier = modifier) {
            DefaultPanelText(text = "Key: ${composableLambdaImpl.key}")
            composableLambdaImpl.block?.let {
                HorizontalSplitter()
                DataItem(
                    modifier = Modifier.padding(vertical = 4.dp),
                    data = it,
                    expanded = false,
                    onClick = {}
                )
            }
            HorizontalSplitter()
            DefaultPanelText(text = "Tracked: ${composableLambdaImpl.tracked}")
            composableLambdaImpl.scopeHash?.let {
                recomposeScopesByHash[it]?.let { scope ->
                    HorizontalSplitter()
                    DefaultPanelText(text = "Scope:")
                    DataItem(
                        modifier = Modifier.padding(vertical = 4.dp),
                        data = scope,
                        expanded = false,
                        onClick = {}
                    )
                }
            }
            val scopes = composableLambdaImpl.scopeHashes.mapNotNull {
                recomposeScopesByHash[it]
            }
            if (scopes.isNotEmpty()) {
                HorizontalSplitter()
                DefaultPanelText(text = "Scopes:")
                scopes.forEach {
                    DataItem(
                        modifier = Modifier.padding(vertical = 4.dp),
                        data = it,
                        expanded = false,
                        onClick = {}
                    )
                }
            }
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
            DefaultPanelText(text = "Reference:")
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
        text = "CompoundHashKey: ${context.compoundHashKey}"
    )
}

@Composable
private fun ExpandedLayoutNode(
    modifier: Modifier,
    contexts: Contexts,
    layoutNode: LayoutNode
) {
    with(contexts) {
        Column(modifier = modifier) {
            layoutNode.lookaheadRootHash?.let {
                layoutNodesByHash[it]?.let { node ->
                    DefaultPanelText(text = "Lookahead root:")
                    DataItem(
                        modifier = Modifier.padding(vertical = 4.dp),
                        data = node,
                        expanded = false,
                        onClick = {}
                    )
                }
            }

            layoutNode.parentHash?.let {
                layoutNodesByHash[it]?.let { node ->
                    HorizontalSplitter()
                    DefaultPanelText(text = "Parent:")
                    DataItem(
                        modifier = Modifier.padding(vertical = 4.dp),
                        data = node,
                        expanded = false,
                        onClick = {}
                    )
                }
            }

            val children = layoutNode.childrenHashes.mapNotNull {
                layoutNodesByHash[it]
            }
            if (children.isNotEmpty()) {
                HorizontalSplitter()
                DefaultPanelText(text = "Children:")
                children.forEach { child ->
                    DataItem(
                        modifier = Modifier.padding(vertical = 4.dp),
                        data = child,
                        expanded = false,
                        onClick = {}
                    )
                }
            }

            HorizontalSplitter()
            DefaultPanelText(text = "Coordinators:")
            var currentNodeIndex = 0
            layoutNode.coordinators.forEach { coordinator ->
                DataItem(
                    modifier = Modifier.padding(vertical = 4.dp),
                    data = coordinator,
                    expanded = false,
                    onClick = {}
                )
                while (coordinator.tailNodeHash != layoutNode.nodes[currentNodeIndex].hashCode) {
                    DataItem(
                        modifier = Modifier.padding(vertical = 4.dp),
                        data = layoutNode.nodes[currentNodeIndex++],
                        expanded = false,
                        onClick = {}
                    )
                }
            }
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
            DefaultPanelText(
                text = "Observing states:",
                modifier = Modifier.fillMaxWidth().wrapContentHeight()
            )
            val states = recomposeScope.composeStateHashes.mapNotNull {
                statesByHash[it]
            }
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
    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            state = verticalScrollState
        ) {
            items(states.size) {
                var expanded: Boolean by remember {
                    mutableStateOf(false)
                }
                RowWithLineNumber(it, states.size) {
                    StateItem(
                        modifier = Modifier.padding(4.dp),
                        state = states[it],
                        expanded = expanded,
                        contexts = contexts,
                        onClick = { expanded = !expanded }
                    )
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
    onClick: () -> Unit = {},
) {
    Box(modifier = modifier) {
        Column(modifier = Modifier.wrapContentHeight().fillMaxWidth()) {
            val interactionSource = remember { MutableInteractionSource() }
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
                fontSize = 24.sp,
                overflow = TextOverflow.Ellipsis,
                fontWeight = FontWeight.Thin,
                lineHeight = 36.sp
            )
            if (expanded) {
                StateDetail(
                    modifier = Modifier.fillMaxWidth()
                        .wrapContentHeight()
                        .background(Color(0x08FFFFFF))
                        .padding(horizontal = 48.dp),
                    contexts = contexts,
                    state = state
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
            DefaultPanelText("${data.typeName ?: ""}@${data.hashCode}")
            DefaultPanelText("toString: ${data.toString}")
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
            DefaultPanelText("Key: $keyInfo")
            DefaultPanelText("Source info: ${group.attributes.sourceInformation}")
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
            DefaultPanelText(text = "Wrapped:")
            DataItem(
                modifier = Modifier.padding(4.dp),
                data = rememberObserverHolder.wrapped,
                expanded = false,
                onClick = { }
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
            return -1
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
        GroupItem(this@RootsNode, clickable = true) {
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
            modifier = Modifier.padding(vertical = 4.dp),
            data = data,
            expanded = false,
            onClick = { popupData(data) }
        )
    }

    private fun popupData(data: Data) = with(contexts) {
        popup @Composable {
            val verticalScrollState = rememberScrollState()

            Box(
                modifier = Modifier
                    .requiredWidth(800.dp)
                    .requiredHeightIn(400.dp, 1000.dp)
            ) {
                Box(
                    modifier = Modifier.wrapContentHeight().verticalScroll(verticalScrollState)
                ) {
                    when (data) {
                        is ComposableLambdaImpl -> ExpandedComposableLambdaImpl(
                            modifier = Modifier.fillMaxWidth().wrapContentHeight(),
                            contexts = contexts,
                            composableLambdaImpl = data
                        )
                        is ComposeState -> StateDetail(
                            modifier = Modifier.fillMaxWidth().wrapContentHeight(),
                            contexts = contexts,
                            state = data
                        )
                        is CompositionContextHolder -> ExpandedCompositionContextHolder(
                            modifier = Modifier.fillMaxWidth().wrapContentHeight(),
                            contexts = contexts,
                            compositionContextHolder = data
                        )
                        is Context -> ExpandedContext(
                            modifier = Modifier.fillMaxWidth().wrapContentHeight(),
                            contexts = contexts,
                            context = data
                        )
                        is LayoutNode -> ExpandedLayoutNode(
                            modifier = Modifier.fillMaxWidth().wrapContentHeight(),
                            contexts = contexts,
                            layoutNode = data
                        )
                        is RecomposeScope -> ExpandedRecomposeScope(
                            modifier = Modifier.fillMaxWidth().wrapContentHeight(),
                            contexts = contexts,
                            recomposeScope = data
                        )
                        is RememberObserverHolder -> ExpandedRememberObserverHolder(
                            modifier = Modifier.fillMaxWidth().wrapContentHeight(),
                            contexts = contexts,
                            rememberObserverHolder = data
                        )
                        else -> {
                            ExpandedDataDefault(
                                modifier = Modifier.fillMaxWidth().wrapContentHeight(),
                                contexts = contexts,
                                data = data
                            )
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
}

private fun CompositionRoots.buildContexts(
    navigationContext: NavigationContext?,
    popup: (@Composable () -> Unit) -> Unit,
    window: (Pair<String, @Composable () -> Unit>) -> Unit,
    navigate: (String, Int, Int) -> Unit
): Contexts {
    val statesByHash = this.stateTable.associateBy { it.hashCode }
    val layoutNodesByHash = mutableMapOf<Int, LayoutNode>()
    val modifiersByHash = mutableMapOf<Int, ModifierNode>()
    val recomposeScopesByHash = mutableMapOf<Int, RecomposeScope>()

    fun traverse(group: Group) {
        group.data.forEach {
            when (it) {
                is LayoutNode -> layoutNodesByHash[it.hashCode] = it
                is ModifierNode -> modifiersByHash[it.hashCode] = it
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
        modifiersByBash = modifiersByHash,
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
    val modifiersByBash: Map<Int, ModifierNode>,
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
