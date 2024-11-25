package com.decomposer.runtime.compose

import androidx.compose.runtime.tooling.CompositionData
import com.decomposer.runtime.connection.model.CompositionTree

internal abstract class CompositionExtractor {

    fun map(compositionData: CompositionData): CompositionTree {
        dumpCompositionData(compositionData)
        return CompositionTree()
    }

    abstract suspend fun extractCompositionTree(): CompositionTree
    abstract fun dumpCompositionData(data: CompositionData)
}

internal class CompositionNode(
    val data: CompositionData,
    val children: List<CompositionNode>
)
