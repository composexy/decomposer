package com.decomposer.runtime.compose

import androidx.compose.runtime.CompositionContext
import androidx.compose.runtime.tooling.CompositionData
import androidx.compose.runtime.tooling.CompositionGroup
import com.decomposer.runtime.Logger
import com.decomposer.runtime.connection.model.CompositionRoots
import com.decomposer.runtime.connection.model.Context
import com.decomposer.runtime.connection.model.Group
import com.decomposer.runtime.connection.model.Root
import kotlin.reflect.KProperty1

@Suppress("UNCHECKED_CAST")
internal abstract class CompositionExtractor(
    private val logger: Logger
) {

    fun map(compositionData: CompositionData): Root {
        return Root(
            context = compositionData.parent?.let {
                mapCompositionContext(it)
            },
            groups = compositionData.compositionGroups.map {
                mapCompositionGroup(it)
            }
        )
    }

    private fun mapCompositionContext(compositionContext: CompositionContext): Context? {
        return when {
            compositionContext::class.qualifiedName?.contains(COMPOSITION_CONTEXT_IMPL) == true ->
                Context(compositionContext.compoundHashKey)
            compositionContext::class.qualifiedName == RECOMPOSER -> null
            else -> {
                logger.log(
                    Logger.Level.WARNING, TAG,
                    "Unknown CompositionContext type: $compositionContext"
                )
                null
            }
        }
    }

    private fun mapCompositionGroup(group: CompositionGroup): Group {
        TODO()
    }

    abstract suspend fun extractCompositionRoots(): CompositionRoots
    abstract fun dumpCompositionData(data: CompositionData)

    private val CompositionData.parent: CompositionContext?
        get() {
            return if (this::class.qualifiedName == COMPOSITION_IMPL) {
                val kClass = this::class
                val parentProperty = kClass.members
                    .find { it.name == COMPOSITION_IMPL_PARENT } as? KProperty1<Any, *>
                if (parentProperty == null) {
                    logger.log(Logger.Level.WARNING, TAG, "Cannot find parent property!")
                }
                parentProperty?.get(this) as CompositionContext?
            } else {
                logger.log(Logger.Level.WARNING, TAG, "Unknown composition type: $this")
                null
            }
        }

    private val CompositionContext.compoundHashKey: Int
        get() {
            val kClass = this::class
            val property = kClass.members
                .find { it.name == COMPOUND_HASH_KEY } as? KProperty1<Any, *>
            if (property == null) {
                logger.log(Logger.Level.WARNING, TAG, "Cannot find compoundHashKey property!")
                return 0
            }
            return property.get(this) as Int
        }

    companion object {
        private const val RECOMPOSER = "androidx.compose.runtime.Recomposer"
        private const val COMPOSITION_CONTEXT_IMPL = "CompositionContextImpl"
        private const val COMPOSITION_IMPL = "androidx.compose.runtime.CompositionImpl"
        private const val COMPOSITION_IMPL_PARENT = "parent"
        private const val COMPOUND_HASH_KEY = "compoundHashKey"
        private const val TAG = "CompositionExtractor"
    }
}
