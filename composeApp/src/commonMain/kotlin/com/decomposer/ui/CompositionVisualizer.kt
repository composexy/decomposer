package com.decomposer.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowColumn
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeightIn
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.requiredWidthIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.font.FontWeight
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
import org.jetbrains.compose.resources.painterResource

@Composable
private fun GroupItem(
    level: Int,
    node: BaseTreeNode,
    onClick: () -> Unit = { }
) {
    Row(
        modifier = Modifier
            .wrapContentHeight()
            .fillMaxWidth()
            .padding(start = 24.dp * level)
    ) {
        val interactionSource = remember { MutableInteractionSource() }
        GroupIcon(Modifier.align(Alignment.CenterVertically), node)
        Text(
            text = node.name,
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
            fontSize = 24.sp,
            fontWeight = FontWeight.Light,
            lineHeight = 36.sp
        )
    }
}

@Composable
private fun DataItem(
    modifier: Modifier = Modifier,
    level: Int,
    data: Data,
    expanded: Boolean,
    onClick: () -> Unit,
    expandedContent: @Composable () -> Unit = {}
) {
    Box(
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .wrapContentHeight()
                .fillMaxWidth()
                .padding(start = 24.dp * level)
        ) {
            if (expanded) {
                expandedContent()
            } else {
                val interactionSource = remember { MutableInteractionSource() }
                DataIcon(Modifier.align(Alignment.CenterVertically), data)
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
        when {
            node.children.isEmpty() -> {
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
private fun ExpandedState(modifier: Modifier, contexts: Contexts, state: ComposeState) {
    with(contexts) {
        Column(modifier = modifier) {
            DefaultPanelText(text = "Value:")
            DataItem(
                modifier = Modifier.padding(vertical = 4.dp),
                level = 0,
                data = state.value,
                expanded = false,
                onClick = {}
            )
            HorizontalSplitter()
            val readers = mutableListOf<String>()
            if (state.readInComposition == true) readers.add("Composition")
            if (state.readInSnapshotStateObserver == true) readers.add("SnapshotStateObserver")
            if (state.readInSnapshotFlow == true) readers.add("SnapshotFlow")
            DefaultPanelText(text = "Readers: ${readers.joinToString(", ")}")
            HorizontalSplitter()
            DefaultPanelText("Dependencies:")
            state.dependencyHashes.forEach {
                statesByHash[it]?.let { dependency ->
                    DataItem(
                        modifier = Modifier.padding(vertical = 4.dp),
                        level = 0,
                        data = dependency,
                        expanded = false,
                        onClick = {}
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
                    level = 0,
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
                        level = 0,
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
                        level = 0,
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
                        level = 0,
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
                        level = 0,
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
                        level = 0,
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
                    level = 0,
                    data = coordinator,
                    expanded = false,
                    onClick = {}
                )
                while (coordinator.tailNodeHash != layoutNode.nodes[currentNodeIndex].hashCode) {
                    DataItem(
                        modifier = Modifier.padding(vertical = 4.dp),
                        level = 0,
                        data = layoutNode.nodes[currentNodeIndex++],
                        expanded = false,
                        onClick = {}
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
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
            FlowColumn(
                modifier = Modifier.fillMaxWidth()
            ) {
                val states = recomposeScope.composeStateHashes.mapNotNull {
                    statesByHash[it]
                }
                states.forEach {
                    var expanded: Boolean by remember {
                        mutableStateOf(false)
                    }
                    DataItem(
                        modifier = Modifier.padding(4.dp),
                        level = 0,
                        data = it,
                        expanded = expanded,
                        onClick = { expanded = !expanded }
                    ) {
                        ExpandedState(
                            modifier = Modifier.fillMaxColumnWidth().wrapContentHeight(),
                            contexts = contexts,
                            state = it
                        )
                    }
                }
            }
        }
    }
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
    with(contexts) {
        Column(modifier) {
            DefaultPanelText("Key: ${group.attributes.key}")
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
                level = 0,
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
    showContextPopup: (@Composable () -> Unit) -> Unit,
    sourceNavigation: (String, Int, Int) -> Unit
): FilterableTree {
    val contexts = this.buildContexts(
        popup = showContextPopup,
        navigate = sourceNavigation
    )
    return FilterableTree(
        root = RootsNode(this, contexts)
    )
}

private class RootsNode(
    private val compositionRoots: CompositionRoots,
    private val contexts: Contexts
) : BaseTreeNode() {
    override val level = 0
    override val name = "Application"
    override val children: List<TreeNode> = compositionRoots.compositionData.map {
        RootNode(
            compositionRoot = it,
            level = level + 1,
            contexts = contexts
        )
    }
    override val tags: List<Any> = emptyList()

    @OptIn(ExperimentalLayoutApi::class)
    @Composable
    override fun TreeNodeRow() = with(contexts) {
        GroupItem(level, this@RootsNode) {
            popup @Composable {
                val verticalScrollState = rememberScrollState()
                Box(
                    modifier = Modifier
                        .requiredWidth(800.dp)
                        .requiredHeightIn(400.dp, 1000.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                            .verticalScroll(verticalScrollState)
                    ) {
                        DefaultPanelText(
                            text = "States:",
                            modifier = Modifier.fillMaxWidth().wrapContentHeight()
                        )
                        FlowColumn(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            statesByHash.values.forEach {
                                var expanded: Boolean by remember {
                                    mutableStateOf(false)
                                }
                                DataItem(
                                    modifier = Modifier.padding(4.dp),
                                    level = 0,
                                    data = it,
                                    expanded = expanded,
                                    onClick = { expanded = !expanded }
                                ) {
                                    ExpandedState(
                                        modifier = Modifier.fillMaxColumnWidth().wrapContentHeight(),
                                        contexts = contexts,
                                        state = it
                                    )
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
        }
    }
}

private class RootNode(
    private val compositionRoot: CompositionRoot,
    private val contexts: Contexts,
    override val level: Int
) : BaseTreeNode() {
    override val name = "Composition(${compositionRoot.context?.compoundHashKey ?: "Recomposer"})"
    override val children: List<TreeNode> = compositionRoot.groups.map {
        GroupNode(
            group = it,
            level = level + 1,
            contexts = contexts
        )
    }
    override val tags: List<Any> = emptyList()

    @Composable
    override fun TreeNodeRow()  {
        GroupItem(
            level = level,
            node = this@RootNode
        )
    }
}

private class GroupNode(
    private val group: Group,
    private val contexts: Contexts,
    override val level: Int
) : BaseTreeNode() {
    override val name = group.name
    override val children: List<TreeNode> = run {
        val children = mutableListOf<TreeNode>()
        children.addAll(
            group.children.map {
                GroupNode(
                    group = it,
                    level = level + 1,
                    contexts = contexts
                )
            }
        )
        val subcomposeStates = group.data.filterIsInstance<SubcomposeState>()
        children.addAll(
            subcomposeStates.map {
                SubcompositionsNode(
                    subcomposeState = it,
                    level = level + 1,
                    contexts = contexts
                )
            }
        )
        children
    }

    override val tags: List<Any> = group.data

    @Composable
    override fun TreeNodeRow() = with(contexts) {
        Column(
            modifier = Modifier.wrapContentHeight().fillMaxWidth()
        ) {
            var showData: Boolean by remember {
                mutableStateOf(true)
            }
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                GroupItem(
                    level = level,
                    node = this@GroupNode
                ) {
                    navigate("", 0, 0)
                }
                if (group.data.isNotEmpty()) {
                    ShowDataIcon(
                        modifier = Modifier.align(Alignment.CenterVertically),
                        expanded = showData
                    ) {
                        showData = !showData
                    }
                }
                GroupAttributesIcon(
                    modifier = Modifier.align(Alignment.CenterVertically)
                ) {
                    popupGroup(group)
                }
            }
            if (showData) {
                group.data.forEach {
                    DataItem(
                        modifier = Modifier.padding(vertical = 4.dp),
                        level = level + 1,
                        data = it,
                        expanded = false,
                        onClick = { popupData(it) }
                    )
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
                        is ComposeState -> ExpandedState(
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

private class SubcompositionsNode(
    private val subcomposeState: SubcomposeState,
    private val contexts: Contexts,
    override val level: Int
) : BaseTreeNode() {
    override val name: String = "Subcompositions"
    override val children: List<TreeNode> = subcomposeState.compositions.map {
        RootNode(
            compositionRoot = it,
            level = level + 1,
            contexts = contexts
        )
    }
    override val tags: List<Any> = emptyList()

    @Composable
    override fun TreeNodeRow() {
        GroupItem(
            level = level,
            node = this@SubcompositionsNode
        )
    }
}


private fun CompositionRoots.buildContexts(
    popup: (@Composable () -> Unit) -> Unit,
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
        popup = popup,
        navigate = navigate
    )
}

private class Contexts(
    val statesByHash: Map<Int, ComposeState>,
    val layoutNodesByHash: Map<Int, LayoutNode>,
    val modifiersByBash: Map<Int, ModifierNode>,
    val recomposeScopesByHash: Map<Int, RecomposeScope>,
    val popup: (@Composable () -> Unit) -> Unit,
    val navigate: (String, Int, Int) -> Unit
)

private val Group.name: String
    get() {
        val sourceInfo = this.attributes.sourceInformation
        val key = this.attributes.key
        val intKeyValue = if (key is IntKey) {
            key.value
        } else null
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
        val wellKnownKey = if (key is IntKey) {
            WellKnownKeys.entries.firstOrNull {
                it.intKey == key.value
            }?.displayName
        } else null
        val intKey = if (key is IntKey) {
            "Group(${key.value})"
        } else null
        val objectKey = if (key is ObjectKey) {
            "Group(${key.value})"
        } else null
        return when {
            sourceKey != null -> "$sourceKey(${intKeyValue ?: ""})"
            wellKnownKey != null -> "Group($wellKnownKey)"
            intKey != null -> intKey
            objectKey != null -> objectKey
            else -> "Group()"
        }
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
