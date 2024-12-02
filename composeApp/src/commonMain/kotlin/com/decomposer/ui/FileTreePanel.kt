package com.decomposer.ui

import androidx.compose.foundation.HorizontalScrollbar
import androidx.compose.foundation.Image
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.Icon
import androidx.compose.material.LocalContentColor
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.decomposer.runtime.connection.model.ProjectSnapshot
import com.decomposer.server.Session
import decomposer.composeapp.generated.resources.Res
import decomposer.composeapp.generated.resources.document
import decomposer.composeapp.generated.resources.folder_close
import decomposer.composeapp.generated.resources.folder_open
import org.jetbrains.compose.resources.painterResource
import java.nio.file.Path
import java.nio.file.Paths

@Composable
fun FileTreePanel(
    modifier: Modifier = Modifier,
    session: Session
) {
    var fileTree: FilterableTree by remember {
        mutableStateOf(FilterableTree.EMPTY_TREE)
    }

    Box(
        modifier = modifier
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
                val nodes = fileTree.flattenNodes
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

    LaunchedEffect(session.sessionId) {
        val projectSnapshot = session.getProjectSnapshot()
        fileTree = projectSnapshot.asFileTree
    }
}

class FileTreeNode(
    override val name: String,
    override val children: List<TreeNode>,
    override val level: Int,
    override val tags: List<Any> = emptyList(),
) : BaseTreeNode() {

    @Composable
    override fun TreeNodeRow() {
        Row(
            modifier = Modifier
                .wrapContentHeight()
                .fillMaxWidth()
                .padding(start = 24.dp * level)
        ) {
            val interactionSource = remember { MutableInteractionSource() }
            val active by interactionSource.collectIsHoveredAsState()

            FileIcon(Modifier.align(Alignment.CenterVertically))
            Text(
                text = name,
                color = if (active) {
                    LocalContentColor.current.copy(alpha = 0.60f)
                } else {
                    LocalContentColor.current
                },
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .clipToBounds()
                    .hoverable(interactionSource),
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
    private fun FileIcon(modifier: Modifier) {
        Box(
            modifier = modifier
                .wrapContentSize()
                .padding(4.dp)
                .clickable {
                    if (expandable) {
                        expanded = !expanded
                    }
                }
        ) {
            when {
                expandable && expanded -> {
                    Image(
                        painter = painterResource(Res.drawable.folder_open),
                        contentDescription = "Unfold $name",
                        modifier = Modifier.size(32.dp),
                    )
                }
                expandable && !expanded -> {
                    Image(
                        painter = painterResource(Res.drawable.folder_close),
                        contentDescription = "Unfold $name",
                        modifier = Modifier.size(32.dp),
                    )
                }
                else -> {
                    Image(
                        painter = painterResource(Res.drawable.document),
                        contentDescription = name,
                        modifier = Modifier.size(32.dp),
                    )
                }
            }
        }
    }
}

@Suppress("UNCHECKED_CAST")
internal val ProjectSnapshot.asFileTree: FilterableTree
    get() {
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

        fun createNode(name: String, map: Map<String, Any>, level: Int): FileTreeNode {
            val children = map.map { (childName, childMap) ->
                createNode(childName, childMap as Map<String, Any>, level + 1)
            }
            return FileTreeNode(name = name, children = children, level = level)
        }

        val rootChildren = rootMap.map { (name, map) ->
            createNode(name, map as Map<String, Any>, level = 1)
        }
        val root = FileTreeNode(name = commonPrefix.toString(), children = rootChildren, level = 0)
        return FilterableTree(root)
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
