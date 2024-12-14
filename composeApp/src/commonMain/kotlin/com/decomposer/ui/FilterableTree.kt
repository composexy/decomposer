package com.decomposer.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.reflect.KClass

class FilterableTree(val root: TreeNode) {

    init { setParents(root) }

    private val subtreeCache = mutableMapOf<Filter, FilterableTree>()

    fun subtree(filter: Filter): FilterableTree {
        val cachedTree = subtreeCache[filter]
        if (cachedTree != null) {
            return cachedTree
        }

        val filteredNodes = filterNode(root, filter, 0)

        return when {
            filteredNodes.isEmpty() -> EMPTY_TREE
            filteredNodes.first().level == 0 -> {
                FilterableTree(filteredNodes.first()).also {
                    subtreeCache[filter] = it
                }
            }
            else -> {
                val newRoot = SubtreeNode(
                    wrapped = root,
                    children = filteredNodes,
                    level = 0
                )
                FilterableTree(newRoot).also {
                    subtreeCache[filter] = it
                }
            }
        }
    }

    private fun filterNode(node: TreeNode, filter: Filter, level: Int): List<TreeNode> {
        val matches = matches(filter, node)
        val filteredChildren = if (matches || node === root) {
            node.children.flatMap { filterNode(it, filter, level + 1) }
        } else {
            node.children.flatMap { filterNode(it, filter, level) }
        }

        return if (matches) {
            listOf(
                SubtreeNode(
                    wrapped = node,
                    children = filteredChildren,
                    level = level
                )
            )
        } else filteredChildren
    }

    private fun matches(filter: Filter, node: TreeNode): Boolean {
        return filter.predicate(node)
    }

    private fun setParents(parent: TreeNode) {
        parent.children.forEach { child ->
            child.parent = parent
            setParents(child)
        }
    }

    val flattenNodes: List<TreeNode>
        get() = root.flattenedChildren

    class SubtreeNode(
        private val wrapped: TreeNode,
        override val children: List<TreeNode>,
        override val level: Int
    ): TreeNode by wrapped {
        override val flattenedChildren: List<TreeNode> by derivedStateOf {
            flattenChildren()
        }

        @Composable
        override fun TreeNodeIndented(keepLevel: Boolean) {
            val padding = levelWidth * if (keepLevel) {
                wrapped.level
            } else {
                level
            }
            Box(modifier = Modifier.fillMaxWidth().wrapContentHeight().padding(start = padding)) {
                wrapped.TreeNode()
            }
        }

        override fun compareTo(other: TreeNode): Int {
            return if (other is SubtreeNode) {
                wrapped.compareTo(other.wrapped)
            } else 0
        }
    }

    companion object {
        object EmptyNode : BaseTreeNode() {
            override val name = "Empty"
            override var parent: TreeNode? = null
            override val children: List<TreeNode> = emptyList()
            override var expanded = false
            override val tags = emptySet<Any>()
            override val level = 0

            @Composable
            override fun TreeNode() {
                DefaultPanelText(text = name)
            }

            override fun compareTo(other: TreeNode): Int = 0
        }

        val EMPTY_TREE = FilterableTree(EmptyNode)
    }
}

data class Filter(
    val predicate: (TreeNode) -> Boolean
)

@Stable
interface TreeNode : Comparable<TreeNode> {
    val name: String
    var parent: TreeNode?
    val children: List<TreeNode>
    val flattenedChildren: List<TreeNode>
    val expanded: Boolean
    val tags: Set<Any>
    val expandable: Boolean
    val level: Int
    val excludes: Set<KClass<*>>
    val levelWidth: Dp
    fun hasTag(clazz: KClass<*>): Boolean
    @Composable
    fun TreeNode()
    @Composable
    fun TreeNodeIndented(keepLevel: Boolean = true)
    fun setExpandedRecursive(expanded: Boolean)
    fun addExcludesRecursive(excludes: Set<KClass<*>>)
    fun removeExcludesRecursive(excludes: Set<KClass<*>>)
}

fun TreeNode.flattenChildren(): List<TreeNode> {
    val result = mutableListOf<TreeNode>()
    val excluded = tags.any { excludes.contains(it::class) }
    if (!excluded) {
        result.add(this)
        if (expanded) {
            val sortedChildren = this.children.sortedBy { it }
            sortedChildren.forEach {
                result.addAll(it.flattenedChildren)
            }
        }
    }
    return result
}

abstract class BaseTreeNode : TreeNode {
    override var excludes: Set<KClass<*>> by mutableStateOf(emptySet())
    override val levelWidth: Dp
        get() = AppSetting.fontSize.dp
    override var parent: TreeNode? = null

    override val expandable: Boolean
        get() = children.isNotEmpty()

    override fun hasTag(clazz: KClass<*>): Boolean {
        return tags.any { it::class == clazz }
    }

    override val flattenedChildren: List<TreeNode> by derivedStateOf {
        flattenChildren()
    }

    override var expanded: Boolean by mutableStateOf(false)

    override fun setExpandedRecursive(expanded: Boolean) {
        this.expanded = expanded
        this.children.forEach {
            it.setExpandedRecursive(expanded)
        }
    }

    override fun addExcludesRecursive(excludes: Set<KClass<*>>) {
        this.excludes += excludes
        this.children.forEach {
            it.addExcludesRecursive(excludes)
        }
    }

    override fun removeExcludesRecursive(excludes: Set<KClass<*>>) {
        this.excludes -= excludes
        this.children.forEach {
            it.removeExcludesRecursive(excludes)
        }
    }

    @Composable
    override fun TreeNodeIndented(keepLevel: Boolean) {
        Box(
            modifier = Modifier.fillMaxWidth()
                .wrapContentHeight()
                .padding(start = levelWidth * level)
        ) {
            TreeNode()
        }
    }
 }
