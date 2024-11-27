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
import kotlin.reflect.full.declaredMembers

@Suppress("UNCHECKED_CAST")
internal abstract class CompositionExtractor(
    private val logger: Logger
) {

    fun map(composition: Composition): Root {
        val reflection = CompositionReflection(composition, logger)
        val compositionData = reflection.compositionData
        val observations = reflection.observations
        val context = reflection.parent
        if (compositionData == null || context == null) {
            logger.log(Logger.Level.WARNING, TAG, "Invalid composition: $composition")
            return EMPTY_ROOT
        }
        dumpCompositionData(compositionData)
        return map(compositionData, observations, context)
    }

    fun map(
        compositionData: CompositionData,
        observations: Map<Any, Set<Any>>,
        context: CompositionContext
    ): Root {
        return Root(
            context = mapCompositionContext(context),
            groups = compositionData.compositionGroups.map {
                mapCompositionGroup(it, observations)
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

    private fun mapCompositionGroup(
        group: CompositionGroup,
        observations: Map<Any, Set<Any>>
    ): Group {
        return Group(
            attributes = Attributes(
                key = mapKey(group.key),
                sourceInformation = group.sourceInfo
            ),
            data = group.data.map {
                mapData(it, observations)
            },
            children = group.compositionGroups.map {
                mapCompositionGroup(it, observations)
            }
        )
    }

    private fun mapData(any: Any?, observations: Map<Any, Set<Any>>): Data {
        return when {
            any == null -> EmptyData()
            any::class.qualifiedName == COMPOSITION_CONTEXT_IMPL ->
                mapCompositionContext(any as CompositionContext)!!
            any is State<*> -> mapState(any)
            any::class.qualifiedName == LAYOUT_NODE -> mapLayoutNode(any)
            any::class.qualifiedName == RECOMPOSE_SCOPE_IMPL -> mapRecomposeScope(any, observations)
            else -> mapGeneric(any)
        }
    }

    private fun mapRecomposeScope(
        recomposeScope: Any,
        observations: Map<Any, Set<Any>>
    ): RecomposeScope {
        return RecomposeScope(
            toString = recomposeScope.toString(),
            composeStates = findObservedStates(recomposeScope, observations)
        )
    }

    private fun findObservedStates(
        recomposeScope: Any,
        observations: Map<Any, Set<Any>>
    ): List<ComposeState> {
        val readStates = mutableListOf<ComposeState>()
        observations.forEach { entry ->
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

    fun dumpCompositionData(data: CompositionData) {
        if (!DEBUG) return
        data.compositionGroups.forEachIndexed { index, group ->
            dumpGroup(index, group)
        }
    }

    private fun dumpGroup(index: Int, group: CompositionGroup) {
        val groupInfo = buildString {
            append("Group $index: ${group.key}, ")
            append("node ${group.node}, ")
            append("sourceInfo ${group.sourceInfo}, ")
            append("identity ${group.identity}, ")
            append("slotSize: ${group.slotsSize}, ")
            append("groupSize: ${group.groupSize}\n")
        }
        logger.log(Logger.Level.DEBUG, TAG, groupInfo)
        group.data.forEachIndexed { i, d ->
            logger.log(Logger.Level.DEBUG, TAG, "Data $i: $d\n")
        }
        group.compositionGroups.forEachIndexed { i, g ->
            dumpGroup(i, g)
        }
    }

    companion object {
        private val EMPTY_ROOT = Root(null, emptyList())
        private const val RECOMPOSE_SCOPE_IMPL = "androidx.compose.runtime.RecomposeScopeImpl"
        private const val LAYOUT_NODE = "androidx.compose.ui.node.LayoutNode"
        private const val RECOMPOSER = "androidx.compose.runtime.Recomposer"
        private const val COMPOSITION_CONTEXT_IMPL = "androidx.compose.runtime.ComposerImpl.CompositionContextImpl"
        private const val COMPOUND_HASH_KEY = "compoundHashKey"
        private const val TAG = "CompositionExtractor"
        private const val DEBUG = false
    }
}
