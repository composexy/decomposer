package com.decomposer.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlin.reflect.KClass

class FilterableTree(
    val root: TreeNode
) {
    private val filterCache = mutableMapOf<Set<KClass<*>>, FilterableTree>()

    fun subtree(classes: Set<KClass<*>>): FilterableTree {
        val cachedTree = filterCache[classes]
        if (cachedTree != null) {
            return cachedTree
        }

        fun filterNode(node: TreeNode, level: Int): List<TreeNode> {
            val filteredChildren = if (classes.all { node.hasTag(it) } || node === root) {
                node.children.flatMap { filterNode(it, level + 1) }
            } else {
                node.children.flatMap { filterNode(it, level) }
            }

            if (classes.all { node.hasTag(it) }) {
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
                    filterCache[classes] = it
                }
            }
            else -> {
                val newRoot = FilteredNode(
                    wrapped = root,
                    children = filteredNodes,
                    level = 0
                )
                FilterableTree(newRoot).also {
                    filterCache[classes] = it
                }
            }
        }
    }

    val flattenNodes: List<TreeNode>
        get() = root.flattenedChildren

    class FilteredNode(
        private val wrapped: TreeNode,
        override val children: List<TreeNode>,
        override val level: Int
    ): BaseTreeNode() {
        override val name = wrapped.name
        override var expanded by mutableStateOf(wrapped.expanded)

        override val tags = wrapped.tags

        @Composable
        override fun TreeNodeRow() {
            wrapped.TreeNodeRow()
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
            override fun TreeNodeRow() {
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
    fun hasTag(clazz: KClass<*>): Boolean
    @Composable
    fun TreeNodeRow()
    fun setExpandedRecursive(expanded: Boolean)
    fun addExcludesRecursive(excludes: Set<KClass<*>>)
    fun removeExcludesRecursive(excludes: Set<KClass<*>>)
}

abstract class BaseTreeNode : TreeNode {

    override var excludes: Set<KClass<*>> by mutableStateOf(emptySet())

    override val expandable: Boolean
        get() {
            return children.isNotEmpty()
        }

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
 }
