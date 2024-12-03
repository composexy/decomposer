package com.decomposer.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlin.reflect.KClass

class FilterableTree(
    private val root: TreeNode
) {
    private val filterCache = mutableMapOf<KClass<*>, FilterableTree>()

    fun filterSubTree(clazz: KClass<*>): FilterableTree {
        val cachedTree = filterCache[clazz]
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
                    filterCache[clazz] = it
                }
            }
            else -> {
                val newRoot = FilteredNode(
                    wrapped = root,
                    children = filteredNodes,
                    level = 0
                )
                FilterableTree(newRoot).also {
                    filterCache[clazz] = it
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
    }

    companion object {
        object EmptyNode : BaseTreeNode() {
            override val name = "Empty"
            override val children = emptyList<EmptyNode>()
            override var expanded = false
            override val tags = emptyList<Any>()
            override val level = 0

            @Composable
            override fun TreeNodeRow() {
                DefaultPanelText(text = name)
            }
        }

        val EMPTY_TREE = FilterableTree(EmptyNode)
    }
}

@Stable
interface TreeNode {
    val name: String
    val children: List<TreeNode>
    val flattenedChildren: List<TreeNode>
    val expanded: Boolean
    val tags: List<Any>
    val expandable: Boolean
    val level: Int
    fun hasTag(clazz: KClass<*>): Boolean

    @Composable
    fun TreeNodeRow()
}

abstract class BaseTreeNode : TreeNode {
    override val expandable: Boolean
        get() {
            return children.isNotEmpty()
        }

    override fun hasTag(clazz: KClass<*>): Boolean {
        return tags.any { it::class == clazz }
    }

    override val flattenedChildren: List<TreeNode> by derivedStateOf {
        val result = mutableListOf<TreeNode>()
        result.add(this)
        if (expanded) {
            val sortedChildren = this.children.sortedWith(
                compareBy({ it.expandable }, { it.name })
            )
            sortedChildren.forEach {
                result.addAll(it.flattenedChildren)
            }
        }
        result
    }

    override var expanded: Boolean by mutableStateOf(false)
}
