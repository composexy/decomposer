package com.decomposer.runtime.compose

import androidx.compose.runtime.Composition
import androidx.compose.runtime.CompositionContext
import androidx.compose.runtime.State
import androidx.compose.runtime.tooling.CompositionData
import androidx.compose.runtime.tooling.CompositionGroup
import com.decomposer.runtime.Logger
import com.decomposer.runtime.connection.model.Attributes
import com.decomposer.runtime.connection.model.ComposeState
import com.decomposer.runtime.connection.model.CompositionRoots
import com.decomposer.runtime.connection.model.Context
import com.decomposer.runtime.connection.model.Data
import com.decomposer.runtime.connection.model.EmptyData
import com.decomposer.runtime.connection.model.Generic
import com.decomposer.runtime.connection.model.Group
import com.decomposer.runtime.connection.model.GroupKey
import com.decomposer.runtime.connection.model.IntKey
import com.decomposer.runtime.connection.model.LayoutNode
import com.decomposer.runtime.connection.model.ObjectKey
import com.decomposer.runtime.connection.model.RecomposeScope
import com.decomposer.runtime.connection.model.Root
import kotlin.reflect.KProperty1
import kotlin.reflect.full.declaredFunctions
import kotlin.reflect.full.declaredMembers
import kotlin.reflect.jvm.isAccessible

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
                mapCompositionGroup(it, compositionData)
            }
        )
    }

    private fun mapCompositionContext(compositionContext: CompositionContext): Context? {
        return when (compositionContext::class.qualifiedName) {
            COMPOSITION_CONTEXT_IMPL ->
                Context(compositionContext.compoundHashKey, compositionContext.toString())
            RECOMPOSER -> null
            else -> {
                logger.log(
                    Logger.Level.WARNING, TAG,
                    "Unknown CompositionContext type: $compositionContext"
                )
                null
            }
        }
    }

    private fun mapCompositionGroup(group: CompositionGroup, data: CompositionData): Group {
        return Group(
            attributes = Attributes(
                key = mapKey(group.key),
                sourceInformation = group.sourceInfo
            ),
            data = group.data.map {
                mapData(it, data)
            },
            children = group.compositionGroups.map {
                mapCompositionGroup(it, data)
            }
        )
    }

    private fun mapData(any: Any?, data: CompositionData): Data {
        return when {
            any == null -> EmptyData()
            any::class.qualifiedName == COMPOSITION_CONTEXT_IMPL ->
                mapCompositionContext(any as CompositionContext)!!
            any is State<*> -> mapState(any)
            any::class.qualifiedName == LAYOUT_NODE -> mapLayoutNode(any)
            any::class.qualifiedName == RECOMPOSE_SCOPE_IMPL -> mapRecomposeScope(any, data)
            else -> mapGeneric(any)
        }
    }

    private fun mapRecomposeScope(recomposeScope: Any, data: CompositionData): RecomposeScope {
        return RecomposeScope(
            toString = recomposeScope.toString(),
            composeStates = /* findObservedStates(recomposeScope, data) */ emptyList()
        )
    }

    private fun findObservedStates(
        recomposeScope: Any,
        data: CompositionData
    ): List<ComposeState> {
        val compositionDataImplClazz = data::class
        val compositionProperty = compositionDataImplClazz.declaredMembers
            .find { it.name == COMPOSITION_DATA_IMPL_COMPOSITION } as? KProperty1<Any, *>
        if (compositionProperty == null) {
            logger.log(Logger.Level.WARNING, TAG, "Cannot find composition property!")
            return emptyList()
        }
        val composition = compositionProperty.get(this) as Composition
        if (composition::class.qualifiedName != COMPOSITION_IMPL) {
            logger.log(Logger.Level.WARNING, TAG, "Unexpected composition type $composition")
            return emptyList()
        }
        val compositionImplClazz = composition::class
        val observationsProperty = compositionImplClazz.declaredMembers
            .find { it.name == COMPOSITION_IMPL_OBSERVATIONS } as? KProperty1<Any, *>
        if (observationsProperty == null) {
            logger.log(Logger.Level.WARNING, TAG, "Cannot find observations property!")
            return emptyList()
        }
        val observations = observationsProperty.get(composition)
        if (observations == null) {
            logger.log(Logger.Level.WARNING, TAG, "Cannot find get observations!")
            return emptyList()
        }
        val asMapMethod = observations::class.declaredFunctions
            .find { it.name == SCOPE_MAP_AS_MAP }
        if (asMapMethod == null) {
            logger.log(Logger.Level.WARNING, TAG, "Cannot find asMap method!")
            return emptyList()
        }
        asMapMethod.isAccessible = true
        val map = asMapMethod.call(observations) as Map<Any, Set<Any>>
        val readStates = mutableListOf<ComposeState>()
        map.forEach { entry ->
            val state = entry.key
            if (state !is State<*>) {
                logger.log(Logger.Level.WARNING, TAG, "Unexpected state type: $state")
                return@forEach
            }
            val scopes = entry.value
            if (scopes.contains(recomposeScope)) {
                readStates.add(ComposeState(state.value.toString(), state.toString()))
            }
        }
        return readStates
    }

    private fun mapKey(key: Any): GroupKey {
        return if (key is Int) {
            IntKey(key)
        } else {
            ObjectKey(key.toString())
        }
    }

    private fun mapLayoutNode(any: Any): LayoutNode {
        return LayoutNode(any.toString())
    }

    private fun mapState(state: State<*>): ComposeState {
        return ComposeState(state.value.toString(), state.toString())
    }

    private fun mapGeneric(any: Any): Generic {
        return Generic(any.toString())
    }

    abstract suspend fun extractCompositionRoots(): CompositionRoots
    abstract fun dumpCompositionData(data: CompositionData)

    private val CompositionData.parent: CompositionContext?
        get() {
            if (this::class.qualifiedName == COMPOSITION_DATA_IMPL) {
                val compositionDataImplClazz = this::class
                val compositionProperty = compositionDataImplClazz.declaredMembers
                    .find { it.name == COMPOSITION_DATA_IMPL_COMPOSITION } as? KProperty1<Any, *>
                if (compositionProperty == null) {
                    logger.log(Logger.Level.WARNING, TAG, "Cannot find composition property!")
                    return null
                }
                val composition = compositionProperty.get(this) as Composition
                if (composition::class.qualifiedName != COMPOSITION_IMPL) {
                    logger.log(Logger.Level.WARNING, TAG, "Unexpected composition type $composition")
                    return null
                }
                val compositionImplClazz = composition::class
                val parentProperty = compositionImplClazz.declaredMembers
                    .find { it.name == COMPOSITION_IMPL_PARENT } as? KProperty1<Any, *>
                if (parentProperty == null) {
                    logger.log(Logger.Level.WARNING, TAG, "Cannot find parent property!")
                    return null
                }
                return parentProperty.get(composition) as CompositionContext?
            } else {
                logger.log(Logger.Level.WARNING, TAG, "Unknown composition type: $this")
                return null
            }
        }

    private val CompositionContext.compoundHashKey: Int
        get() {
            val kClass = this::class
            val property = kClass.declaredMembers
                .find { it.name == COMPOUND_HASH_KEY } as? KProperty1<Any, *>
            if (property == null) {
                logger.log(Logger.Level.WARNING, TAG, "Cannot find compoundHashKey property!")
                return 0
            }
            return property.get(this) as Int
        }

    companion object {
        private const val RECOMPOSE_SCOPE_IMPL = "androidx.compose.runtime.RecomposeScopeImpl"
        private const val LAYOUT_NODE = "androidx.compose.ui.node.LayoutNode"
        private const val RECOMPOSER = "androidx.compose.runtime.Recomposer"
        private const val COMPOSITION_CONTEXT_IMPL = "androidx.compose.runtime.ComposerImpl.CompositionContextImpl"
        private const val COMPOSITION_DATA_IMPL = "androidx.compose.runtime.CompositionDataImpl"
        private const val COMPOSITION_IMPL = "androidx.compose.runtime.CompositionDataImpl"
        private const val COMPOSITION_DATA_IMPL_COMPOSITION = "composition"
        private const val COMPOSITION_IMPL_OBSERVATIONS = "observations"
        private const val COMPOSITION_IMPL_PARENT = "parent"
        private const val COMPOUND_HASH_KEY = "compoundHashKey"
        private const val SCOPE_MAP_AS_MAP = "asMap"
        private const val TAG = "CompositionExtractor"
    }
}
