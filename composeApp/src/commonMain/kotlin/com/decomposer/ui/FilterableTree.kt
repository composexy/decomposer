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

class FilterableTree(
    val root: TreeNode
) {
    var keepLevel: Boolean by mutableStateOf(true)
    private val subtreeCache = mutableMapOf<KClass<*>, FilterableTree>()

    fun subtree(clazz: KClass<*>): FilterableTree {
        val cachedTree = subtreeCache[clazz]
        if (cachedTree != null) {
            return cachedTree
        }

        fun filterNode(node: TreeNode, level: Int): List<TreeNode> {
            val filteredChildren = if (node.hasTag(clazz) || node === root) {
                node.children.flatMap { filterNode(it, level + 1) }
            } else {
                node.children.flatMap { filterNode(it, level) }
            }

            if (node.hasTag(clazz)) {
                return listOf(
                    FilteredNode(
                        wrapped = node,
                        children = filteredChildren,
                        level = level
                    )
                )
            }

            return filteredChildren
        }

        val filteredNodes = filterNode(root, 0)

        return when {
            filteredNodes.isEmpty() -> EMPTY_TREE
            filteredNodes.first().level == 0 -> {
                FilterableTree(filteredNodes.first()).also {
                    subtreeCache[clazz] = it
                }
            }
            else -> {
                val newRoot = FilteredNode(
                    wrapped = root,
                    children = filteredNodes,
                    level = 0
                )
                FilterableTree(newRoot).also {
                    subtreeCache[clazz] = it
                }
            }
        }
    }

    val flattenNodes: List<TreeNode>
        get() = root.flattenedChildren

    inner class FilteredNode(
        private val wrapped: TreeNode,
        override val children: List<TreeNode>,
        override val level: Int
    ): BaseTreeNode() {
        override val name = wrapped.name
        override val tags = wrapped.tags
        override var expanded by mutableStateOf(wrapped.expanded)

        @Composable
        override fun TreeNode() {
            wrapped.TreeNode()
        }

        @Composable
        override fun TreeNodeLeveled() {
            val padding = levelWidth * if (keepLevel) {
                wrapped.level
            } else {
                level
            }
            Box(
                modifier = Modifier.fillMaxWidth()
                    .wrapContentHeight()
                    .padding(start = padding)
            ) {
                wrapped.TreeNode()
            }
        }

        override fun compareTo(other: TreeNode): Int = wrapped.compareTo(other)
    }

    companion object {
        object EmptyNode : BaseTreeNode() {
            override val name = "Empty"
            override val children = emptyList<EmptyNode>()
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

@Stable
interface TreeNode : Comparable<TreeNode> {
    val name: String
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
    fun TreeNodeLeveled()
    fun setExpandedRecursive(expanded: Boolean)
    fun addExcludesRecursive(excludes: Set<KClass<*>>)
    fun removeExcludesRecursive(excludes: Set<KClass<*>>)
}

abstract class BaseTreeNode : TreeNode {

    override var excludes: Set<KClass<*>> by mutableStateOf(emptySet())
    override val levelWidth: Dp = 24.dp

    override val expandable: Boolean
        get() = children.isNotEmpty()

    override fun hasTag(clazz: KClass<*>): Boolean {
        return tags.any { it::class == clazz }
    }

    override val flattenedChildren: List<TreeNode> by derivedStateOf {
        val result = mutableListOf<TreeNode>()
        val excluded = tags.any { excludes.contains(it::class) }
        if (!excluded) {
            result.add(this)
            if (expanded) {
                val sortedChildren = this.children.sortedBy { this }
                sortedChildren.forEach {
                    result.addAll(it.flattenedChildren)
                }
            }
        }
        result
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
    override fun TreeNodeLeveled() {
        Box(
            modifier = Modifier.fillMaxWidth()
                .wrapContentHeight()
                .padding(start = levelWidth * level)
        ) {
            TreeNode()
        }
    }
 }
