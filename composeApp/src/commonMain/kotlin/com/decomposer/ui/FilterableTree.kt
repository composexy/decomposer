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
    private val filterCache = mutableMapOf<KClass<*>, FilterableTree>()

    fun filterSubTree(clazz: KClass<*>): FilterableTree {
        val cachedTree = filterCache[clazz]
        if (cachedTree != null) {
            return cachedTree
        }

        fun filterNode(node: TreeNode): List<TreeNode> {
            val filteredChildren = node.children.flatMap { filterNode(it) }

            if (node.hasTag(clazz)) {
                return listOf(
                    FilteredNode(
                        wrapped = node,
                        children = filteredChildren
                    )
                )
            }

            return filteredChildren
        }

        val filteredNodes = filterNode(root)

        val newRoot = when {
            filteredNodes.isEmpty() -> return EMPTY_TREE
            filteredNodes.size == 1 -> filteredNodes.first()
            else -> {
                FilteredNode(
                    wrapped = root,
                    children = filteredNodes
                )
            }
        }

        return FilterableTree(newRoot).also {
            filterCache[clazz] = it
        }
    }

    val flattenNodes = root.flattenedChildren

    class FilteredNode(
        private val wrapped: TreeNode,
        override val children: List<TreeNode>
    ): BaseTreeNode() {
        override val name = wrapped.name
        override var expanded by mutableStateOf(wrapped.expanded)
        override val tags = wrapped.tags

        @Composable
        override fun node() {
            wrapped.node()
        }
    }

    companion object {
        object EmptyNode : BaseTreeNode() {
            override val name = "Empty"
            override val children = emptyList<EmptyNode>()
            override var expanded = false
            override val tags = emptyList<Any>()

            @Composable
            override fun node() {
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
    fun hasTag(clazz: KClass<*>): Boolean

    @Composable
    fun node()
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
