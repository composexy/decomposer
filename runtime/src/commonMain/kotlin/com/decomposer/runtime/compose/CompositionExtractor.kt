package com.decomposer.runtime.compose

import androidx.compose.runtime.Composition
import androidx.compose.runtime.CompositionContext
import androidx.compose.runtime.State
import androidx.compose.runtime.tooling.CompositionData
import androidx.compose.runtime.tooling.CompositionGroup
import androidx.compose.ui.layout.SubcomposeLayoutState
import com.decomposer.runtime.Logger
import com.decomposer.runtime.connection.model.Attributes
import com.decomposer.runtime.connection.model.ComposableLambdaImpl
import com.decomposer.runtime.connection.model.ComposeState
import com.decomposer.runtime.connection.model.CompositionContextHolder
import com.decomposer.runtime.connection.model.CompositionRoots
import com.decomposer.runtime.connection.model.Context
import com.decomposer.runtime.connection.model.Data
import com.decomposer.runtime.connection.model.EmptyData
import com.decomposer.runtime.connection.model.Default
import com.decomposer.runtime.connection.model.Group
import com.decomposer.runtime.connection.model.GroupKey
import com.decomposer.runtime.connection.model.IntKey
import com.decomposer.runtime.connection.model.LayoutNode
import com.decomposer.runtime.connection.model.ObjectKey
import com.decomposer.runtime.connection.model.RecomposeScope
import com.decomposer.runtime.connection.model.CompositionRoot
import com.decomposer.runtime.connection.model.Coordinator
import com.decomposer.runtime.connection.model.ModifierNode
import com.decomposer.runtime.connection.model.RememberObserverHolder
import com.decomposer.runtime.connection.model.SubcomposeState
import kotlin.reflect.KProperty1
import kotlin.reflect.full.declaredMembers

@Suppress("UNCHECKED_CAST")
internal abstract class CompositionExtractor(
    private val logger: Logger
) {

    fun map(composition: Composition): Pair<CompositionRoot, Set<ComposeState>> {
        val states = mutableSetOf<ComposeState>()
        val reflection = CompositionReflection(composition, logger)
        val compositionData = reflection.compositionData
        val observations = reflection.observations
        val context = reflection.parent
        if (compositionData == null || context == null) {
            logger.log(Logger.Level.WARNING, TAG, "Invalid composition: $composition")
            return Pair(EMPTY_ROOT, emptySet())
        }
        dumpCompositionData(compositionData)
        return Pair(map(compositionData, observations, context, states), states)
    }

    fun map(
        compositionData: CompositionData,
        observations: Map<Any, Set<Any>>,
        context: CompositionContext,
        outStates: MutableSet<ComposeState>
    ): CompositionRoot {
        return CompositionRoot(
            context = mapCompositionContext(context),
            groups = compositionData.compositionGroups.map {
                mapCompositionGroup(it, observations, outStates)
            }
        )
    }

    private fun mapCompositionContext(compositionContext: CompositionContext): Context? {
        return when (compositionContext::class.qualifiedName) {
            COMPOSITION_CONTEXT_IMPL ->
                Context(
                    compoundHashKey = compositionContext.compoundHashKey,
                    toString = compositionContext.toString(),
                    typeName = compositionContext::class.qualifiedName,
                    hashCode = compositionContext.hashCode()
                )
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
        observations: Map<Any, Set<Any>>,
        outStates: MutableSet<ComposeState>
    ): Group {
        return Group(
            attributes = Attributes(
                key = mapKey(group.key),
                sourceInformation = group.sourceInfo
            ),
            data = group.data.map {
                mapData(it, observations, outStates)
            },
            children = group.compositionGroups.map {
                mapCompositionGroup(it, observations, outStates)
            }
        )
    }

    private fun mapData(
        any: Any?,
        observations: Map<Any, Set<Any>>,
        outStates: MutableSet<ComposeState>
    ): Data {
        return when {
            any == null -> EmptyData
            any is CompositionContext -> mapCompositionContext(any)!!
            any is State<*> -> mapState(any, observations, outStates).also { state ->
                if (!outStates.any { it.hashCode == state.hashCode }) {
                    outStates.add(state)
                }
            }
            any::class.qualifiedName == LAYOUT_NODE -> mapLayoutNode(any)
            any::class.qualifiedName == RECOMPOSE_SCOPE_IMPL ->
                mapRecomposeScope(any, observations, outStates)
            any::class.qualifiedName == SUBCOMPOSE_LAYOUT_STATE ->
                mapSubcomposeLayoutState(any as SubcomposeLayoutState, outStates)
            any::class.qualifiedName == COMPOSABLE_LAMBDA_IMPL -> mapComposableLambda(any)
            any::class.qualifiedName == REMEMBER_OBSERVER_HOLDER -> {
                mapRememberObserverHolder(any) ?: EmptyData
            }
            any::class.qualifiedName == COMPOSITION_CONTEXT_HOLDER -> {
                mapCompositionContextHolder(any) ?: EmptyData
            }
            else -> mapGeneric(any)
        }
    }

    private fun mapRememberObserverHolder(any: Any): RememberObserverHolder? {
        val reflection = RememberObserverHolderReflection(any, logger)
        return reflection.wrapped?.let { wrapped ->
            RememberObserverHolder(
                wrapped = mapGeneric(wrapped)
            )
        }
    }

    private fun mapCompositionContextHolder(any: Any): CompositionContextHolder? {
        val reflection = CompositionContextHolderReflection(any, logger)
        return reflection.ref?.let {
            CompositionContextHolder(
                ref = mapCompositionContext(it)!!
            )
        }
    }

    private fun mapComposableLambda(any: Any): ComposableLambdaImpl {
        val reflection = ComposableLambdaImplReflection(any, logger)
        return ComposableLambdaImpl(
            key = reflection.key,
            block = reflection.block?.let { mapGeneric(it) },
            tracked = reflection.tracked,
            scopeHash = reflection.scopeHash,
            scopeHashes = reflection.scopeHashes,
            toString = any.toString(),
            typeName = any::class.qualifiedName,
            hashCode = any.hashCode()
        )
    }

    private fun mapSubcomposeLayoutState(
        subcomposeLayoutState: SubcomposeLayoutState,
        outStates: MutableSet<ComposeState>
    ): SubcomposeState {
        val reflection = SubcomposeLayoutStateReflection(subcomposeLayoutState, logger)
        val subcompositions = mutableListOf<CompositionRoot>()
        reflection.subcompositions.forEach {
            val data = map(it)
            subcompositions.add(data.first)
            outStates.addAll(data.second)
        }
        return SubcomposeState(
            compositions = subcompositions,
            toString = subcomposeLayoutState.toString(),
            typeName = subcomposeLayoutState::class.qualifiedName,
            hashCode = subcomposeLayoutState.hashCode()
        )
    }

    private fun mapRecomposeScope(
        recomposeScope: Any,
        observations: Map<Any, Set<Any>>,
        outStates: MutableSet<ComposeState>
    ): RecomposeScope {
        return RecomposeScope(
            composeStateHashes = findObservedStateHashes(recomposeScope, observations, outStates),
            toString = recomposeScope.toString(),
            typeName = recomposeScope::class.qualifiedName,
            hashCode = recomposeScope.hashCode()
        )
    }

    private fun findObservedStateHashes(
        recomposeScope: Any,
        observations: Map<Any, Set<Any>>,
        outStates: MutableSet<ComposeState>
    ): List<Int> {
        val readStates = mutableListOf<Int>()
        observations.forEach { entry ->
            val state = entry.key
            if (state !is State<*>) {
                logger.log(Logger.Level.WARNING, TAG, "Unexpected state type: $state")
                return@forEach
            }
            val hashCode = state.hashCode()
            if (!outStates.any { it.hashCode == hashCode }) {
                outStates.add(mapState(state, observations, outStates))
            }
            val scopes = entry.value
            if (scopes.contains(recomposeScope)) {
                readStates.add(hashCode)
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
        val reflection = LayoutNodeReflection(any, logger)
        return LayoutNode(
            lookaheadRootHash = reflection.lookaheadRootHash,
            childrenHashes = reflection.childrenHashes,
            parentHash = reflection.parentHash,
            nodes = reflection.nodes.map {
                ModifierNode(
                    toString = it.toString(),
                    typeName = it::class.qualifiedName,
                    hashCode = it.hashCode()
                )
            },
            coordinators = reflection.coordinators.map {
                val coordinator = it.first
                val tailNode = it.second
                Coordinator(
                    tailNodeHash = tailNode.hashCode(),
                    toString = coordinator.toString(),
                    typeName = coordinator::class.qualifiedName,
                    hashCode = coordinator.hashCode()
                )
            },
            toString = any.toString(),
            typeName = any::class.qualifiedName!!,
            hashCode = any.hashCode()
        )
    }

    private fun mapState(
        state: State<*>,
        observations: Map<Any, Set<Any>>,
        outStates: MutableSet<ComposeState>
    ): ComposeState {
        val reflection = StateReflection(state, logger)
        return ComposeState(
            value = mapData(state.value, observations, outStates),
            dependencyHashes = reflection.dependencies.map { dependency ->
                val hashCode = dependency.hashCode()
                if (dependency is State<*> && !outStates.any { it.hashCode == hashCode }) {
                    outStates.add(mapState(dependency, observations, outStates))
                }
                hashCode
            },
            readInComposition = reflection.readInComposition,
            readInSnapshotFlow = reflection.readInSnapshotFlow,
            readInSnapshotStateObserver = reflection.readInSnapshotObserver,
            toString = state.toString(),
            typeName = state::class.qualifiedName,
            hashCode = state.hashCode()
        )
    }

    private fun mapGeneric(any: Any): Default {
        return Default(
            toString = any.toString(),
            typeName = any::class.qualifiedName,
            hashCode = any.hashCode()
        )
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

    private fun dumpCompositionData(data: CompositionData) {
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
        private val EMPTY_ROOT = CompositionRoot(null, emptyList())
        private const val COMPOSITION_CONTEXT_HOLDER = "androidx.compose.runtime.ComposerImpl.CompositionContextHolder"
        private const val REMEMBER_OBSERVER_HOLDER = "androidx.compose.runtime.RememberObserverHolder"
        private const val COMPOSABLE_LAMBDA_IMPL = "androidx.compose.runtime.internal.ComposableLambdaImpl"
        private const val SUBCOMPOSE_LAYOUT_STATE = "androidx.compose.ui.layout.SubcomposeLayoutState"
        private const val RECOMPOSE_SCOPE_IMPL = "androidx.compose.runtime.RecomposeScopeImpl"
        private const val LAYOUT_NODE = "androidx.compose.ui.node.LayoutNode"
        private const val RECOMPOSER = "androidx.compose.runtime.Recomposer"
        private const val COMPOSITION_CONTEXT_IMPL = "androidx.compose.runtime.ComposerImpl.CompositionContextImpl"
        private const val COMPOUND_HASH_KEY = "compoundHashKey"
        private const val TAG = "CompositionExtractor"
        private const val DEBUG = false
    }
}
