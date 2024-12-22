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
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.decomposer.runtime.connection.model.ProjectSnapshot
import decomposer.composeapp.generated.resources.Res
import decomposer.composeapp.generated.resources.file
import decomposer.composeapp.generated.resources.folder_close
import decomposer.composeapp.generated.resources.folder_open
import org.jetbrains.compose.resources.painterResource
import java.nio.file.Path
import java.nio.file.Paths

@Composable
fun FileTreePanel(
    modifier: Modifier = Modifier,
    projectSnapshot: ProjectSnapshot,
    loading: Boolean,
    onClickFileEntry: (String) -> Unit
) {
    var fileTree: FilterableTree by remember { mutableStateOf(FilterableTree.EMPTY_TREE) }

    Box(modifier = modifier) {
        val verticalScrollState = rememberLazyListState()
        val horizontalScrollState = rememberScrollState()

        Column(modifier = Modifier.fillMaxSize()) {
            if (loading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            } else {
                TreeExpander(
                    onFoldAll = { fileTree.root.setExpandedRecursive(false) },
                    onExpandAll = { fileTree.root.setExpandedRecursive(true) }
                )
                Box(modifier = Modifier.fillMaxSize().horizontalScroll(horizontalScrollState)) {
                    LazyColumn(
                        modifier = Modifier.matchParentSize(),
                        state = verticalScrollState,
                        contentPadding = PaddingValues(vertical = 3.dp, horizontal = 8.dp)
                    ) {
                        val nodes = fileTree.flattenNodes
                        items(nodes.size) {
                            Box(modifier = Modifier.animateItem()) {
                                nodes[it].TreeNodeIndented()
                            }
                        }
                    }
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

    LaunchedEffect(projectSnapshot) {
        fileTree = projectSnapshot.buildFileTree {
            projectSnapshot.findMatching(it)?.let(onClickFileEntry)
        }
    }
}

class FileTreeNode(
    override val name: String,
    override val children: List<FileTreeNode>,
    override val level: Int,
    override val tags: Set<Any> = emptySet(),
    val prefix: String,
    val onClickFileEntry: (String) -> Unit
) : BaseTreeNode() {
    private val isFile = !expandable
    private val isFolder = expandable

    @Composable
    override fun TreeNode() {
        Row(modifier = Modifier.wrapContentHeight().fillMaxWidth()) {
            val interactionSource = remember { MutableInteractionSource() }
            val fontSize = AppSetting.fontSize
            FileIcon(Modifier.align(Alignment.CenterVertically))
            Text(
                text = name,
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .clipToBounds()
                    .run {
                        if (isFile) {
                            this.hoverable(interactionSource)
                                .pointerHoverIcon(PointerIcon.Hand)
                                .clickable {
                                    onClickFileEntry(Paths.get(prefix, name).toString())
                                }
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

    override fun compareTo(other: TreeNode): Int {
        return when {
            other !is FileTreeNode -> -1
            isFolder.compareTo(other.isFolder) != 0 -> {
                isFolder.compareTo(other.isFolder)
            }
            else -> name.compareTo(other.name)
        }
    }

    @Composable
    private fun FileIcon(modifier: Modifier) {
        val size = with(LocalDensity.current) {
            (LocalFontSize.current * 1.25).sp.toDp()
        }
        Box(
            modifier = modifier
                .wrapContentSize()
                .padding(horizontal = 4.dp)
                .run {
                    if (isFolder) {
                        this.clickable { expanded = !expanded }
                    } else {
                        this
                    }
                }
        ) {
            when {
                isFolder && expanded -> {
                    val interactionSource = remember { MutableInteractionSource() }
                    Image(
                        painter = painterResource(Res.drawable.folder_open),
                        contentDescription = "Fold $name",
                        modifier = Modifier
                            .size(size)
                            .hoverable(interactionSource)
                            .pointerHoverIcon(PointerIcon.Hand),
                    )
                }
                isFolder && !expanded -> {
                    val interactionSource = remember { MutableInteractionSource() }
                    Image(
                        painter = painterResource(Res.drawable.folder_close),
                        contentDescription = "Unfold $name",
                        modifier = Modifier
                            .size(size)
                            .hoverable(interactionSource)
                            .pointerHoverIcon(PointerIcon.Hand),
                    )
                }
                else -> {
                    Image(
                        painter = painterResource(Res.drawable.file),
                        contentDescription = name,
                        modifier = Modifier.size(size),
                    )
                }
            }
        }
    }
}

@Suppress("UNCHECKED_CAST")
internal fun ProjectSnapshot.buildFileTree(
    onClickFileEntry: (String) -> Unit
): FilterableTree {
    val paths = this.fileTree
    if (paths.isEmpty()) {
        return FilterableTree.EMPTY_TREE
    }

    val normalizedPaths = paths.map { Paths.get(it).normalize() }
    val commonPrefix = findCommonPrefix(normalizedPaths)
    val trimmedPaths = normalizedPaths.map { commonPrefix.relativize(it) }

    val rootMap = mutableMapOf<String, Any>()
    trimmedPaths.forEach { trimmedPath ->
        val parts = trimmedPath.iterator().asSequence().map { it.toString() }.toList()
        var currentMap = rootMap
        for (part in parts) {
            currentMap = currentMap.computeIfAbsent(part) {
                mutableMapOf<String, Any>()
            } as MutableMap<String, Any>
        }
    }

    fun createNode(
        prefix: String,
        name: String,
        map: Map<String, Any>,
        level: Int
    ): FileTreeNode {
        val children = map.map { (childName, childMap) ->
            createNode(
                prefix = Paths.get(prefix, name).toString(),
                name = childName,
                map = childMap as Map<String, Any>,
                level = level + 1
            )
        }
        return FileTreeNode(
            name = name,
            children = children,
            level = level,
            prefix = prefix,
            onClickFileEntry = onClickFileEntry
        )
    }

    val rootChildren = rootMap.map { (name, map) ->
        createNode(commonPrefix.toString(), name, map as Map<String, Any>, level = 1)
    }
    val root = FileTreeNode(
        name = commonPrefix.toString(),
        children = rootChildren,
        level = 0,
        prefix = "",
        onClickFileEntry = onClickFileEntry
    )
    return FilterableTree(root)
}

private fun ProjectSnapshot.findMatching(other: String): String? {
    return this.fileTree.firstOrNull {
        val normalized = Paths.get(it).normalize().toString()
        normalized == other
    }
}

private fun findCommonPrefix(paths: List<Path>): Path {
    if (paths.isEmpty()) return Paths.get("")
    var prefix = paths.first()
    for (path in paths.drop(1)) {
        while (!path.startsWith(prefix)) {
            prefix = prefix.parent ?: Paths.get("")
        }
    }
    return prefix
}
