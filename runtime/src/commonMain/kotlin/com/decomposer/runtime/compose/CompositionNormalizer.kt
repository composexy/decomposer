package com.decomposer.runtime.compose

import androidx.compose.runtime.Composition
import androidx.compose.runtime.CompositionContext
import androidx.compose.runtime.State
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.runtime.tooling.CompositionData
import androidx.compose.runtime.tooling.CompositionGroup
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.SubcomposeLayoutState
import com.decomposer.runtime.Logger
import com.decomposer.runtime.connection.model.Attributes
import com.decomposer.runtime.connection.model.ComposableLambdaImpl
import com.decomposer.runtime.connection.model.ComposeState
import com.decomposer.runtime.connection.model.CompositionContextHolder
import com.decomposer.runtime.connection.model.CompositionRoot
import com.decomposer.runtime.connection.model.CompositionRoots
import com.decomposer.runtime.connection.model.Context
import com.decomposer.runtime.connection.model.Coordinator
import com.decomposer.runtime.connection.model.Data
import com.decomposer.runtime.connection.model.Default
import com.decomposer.runtime.connection.model.EmptyData
import com.decomposer.runtime.connection.model.Group
import com.decomposer.runtime.connection.model.GroupKey
import com.decomposer.runtime.connection.model.IntKey
import com.decomposer.runtime.connection.model.LayoutNode
import com.decomposer.runtime.connection.model.ModifierNode
import com.decomposer.runtime.connection.model.ObjectKey
import com.decomposer.runtime.connection.model.RecomposeScope
import com.decomposer.runtime.connection.model.RememberObserverHolder
import com.decomposer.runtime.connection.model.SubcomposeState

internal abstract class CompositionNormalizer(
    private val logger: Logger
) {

    abstract suspend fun extractCompositionRoots(): CompositionRoots

    protected fun map(compositions: List<Composition>): CompositionRoots {
        val context = NormalizerContext()
        val roots = compositions.map { composition ->
            map(composition, context)
        }
        return CompositionRoots(
            compositionData = roots,
            stateTable = context.stateTable,
            stringTable = context.stringTable,
            dataTable = context.dataTable,
            groupTable = context.groupTable
        )
    }

    private fun map(composition: Composition, context: NormalizerContext): CompositionRoot {
        val reflection = CompositionReflection(composition, logger)
        val compositionData = reflection.compositionData
        val observations = reflection.observations
        val parentContext = reflection.parent
        if (compositionData == null || parentContext == null) {
            logger.log(Logger.Level.WARNING, TAG, "Invalid composition: $composition")
            return EMPTY_ROOT
        }
        dumpCompositionData(compositionData)
        return map(compositionData, observations, parentContext, context)
    }

    private fun map(
        compositionData: CompositionData,
        observations: Map<Any, Set<Any>>,
        parent: CompositionContext,
        context: NormalizerContext
    ): CompositionRoot {
        with(context) {
            val groups = compositionData.compositionGroups.map {
                group(it) { group ->
                    mapCompositionGroup(group, observations, context)
                }
            }
            return CompositionRoot(
                contextIndex = data(parent) { mapCompositionContext(parent, context) },
                groupIndexes = groups
            )
        }
    }

    private fun mapCompositionContext(
        compositionContext: CompositionContext,
        context: NormalizerContext
    ): Context {
        return with(context) {
            val reflection = CompositionContextReflection(compositionContext, logger)
            val compoundHashKey = when (compositionContext::class.qualifiedName) {
                COMPOSITION_CONTEXT_IMPL -> reflection.compoundHashKey
                RECOMPOSER -> RECOMPOSER_HASH_KEY
                else -> {
                    logger.log(
                        Logger.Level.WARNING, TAG,
                        "Unknown CompositionContext type: $compositionContext"
                    )
                    0
                }
            }
            Context(
                compoundHashKey = compoundHashKey,
                toStringIndex = string(compositionContext.toString()),
                typeNameIndex = compositionContext::class.qualifiedName?.let { string(it) },
                hashCode = compositionContext.hashCode()
            )
        }
    }

    private fun mapCompositionGroup(
        compositionGroup: CompositionGroup,
        observations: Map<Any, Set<Any>>,
        context: NormalizerContext
    ): Group {
        return with(context) {
            Group(
                attributes = Attributes(
                    key = mapKey(compositionGroup.key, context),
                    sourceInformationIndex = compositionGroup.sourceInfo?.let { string(it) }
                ),
                dataIndexes = compositionGroup.data.map { data ->
                    data(data) {
                        mapData(it, observations, context)
                    }
                },
                childIndexes = compositionGroup.compositionGroups.map { group ->
                    group(group) {
                        mapCompositionGroup(it, observations, context)
                    }
                }
            )
        }
    }

    private fun mapData(
        any: Any?,
        observations: Map<Any, Set<Any>>,
        context: NormalizerContext
    ): Data {
        return when {
            any == null -> EmptyData
            any is CompositionContext -> mapCompositionContext(any, context)
            any is State<*> -> mapState(any, observations, context)
            any is SnapshotStateList<*> -> mapStateList(any, observations, context)
            any is SnapshotStateMap<*, *> -> mapStateMap(any, observations, context)
            any::class.qualifiedName == LAYOUT_NODE -> mapLayoutNode(any, context)
            any::class.qualifiedName == RECOMPOSE_SCOPE_IMPL ->
                mapRecomposeScope(any, observations, context)
            any::class.qualifiedName == SUBCOMPOSE_LAYOUT_STATE ->
                mapSubcomposeLayoutState(any as SubcomposeLayoutState, context)
            any::class.qualifiedName == COMPOSABLE_LAMBDA_IMPL -> {
                mapComposableLambda(any, observations, context)
            }
            any::class.qualifiedName == REMEMBER_OBSERVER_HOLDER -> {
                mapRememberObserverHolder(any, context) ?: EmptyData
            }
            any::class.qualifiedName == COMPOSITION_CONTEXT_HOLDER -> {
                mapCompositionContextHolder(any, context) ?: EmptyData
            }
            else -> mapGeneric(any, context)
        }
    }

    private fun mapState(
        state: State<*>,
        observations: Map<Any, Set<Any>>,
        context: NormalizerContext
    ): ComposeState {
        return with(context) {
            val reflection = StateReflection(state, logger)
            val dependencies = reflection.dependencies.mapNotNull { dependency ->
                when (dependency) {
                    is State<*> -> {
                        state(dependency) {
                            mapState(dependency, observations, context)
                        }
                    }
                    is SnapshotStateList<*> -> {
                        state(dependency) {
                            mapStateList(dependency, observations, context)
                        }
                    }
                    is SnapshotStateMap<*, *> -> {
                        state(dependency) {
                            mapStateMap(dependency, observations, context)
                        }
                    }
                    else -> {
                        logger.log(
                            Logger.Level.WARNING, TAG,
                            "Unknown dependency type: $dependency"
                        )
                        null
                    }
                }
            }
            ComposeState(
                valueIndex = data(state.value) { mapData(state.value, observations, context) },
                dependencyIndexes = dependencies,
                readInComposition = reflection.readInComposition,
                readInSnapshotFlow = reflection.readInSnapshotFlow,
                readInSnapshotStateObserver = reflection.readInSnapshotObserver,
                toStringIndex = string(state.toString()),
                typeNameIndex = state::class.qualifiedName?.let { string(it) },
                hashCode = state.hashCode()
            )
        }
    }

    private fun mapGeneric(any: Any, context: NormalizerContext): Default {
        return with(context) {
            Default(
                toStringIndex = string(any.toString()),
                typeNameIndex = any::class.qualifiedName?.let { string(it) },
                hashCode = any.hashCode()
            )
        }
    }

    private fun mapRememberObserverHolder(
        any: Any,
        context: NormalizerContext
    ): RememberObserverHolder? {
        return with(context) {
            val reflection = RememberObserverHolderReflection(any, logger)
            reflection.wrapped?.let { wrapped ->
                RememberObserverHolder(
                    wrappedIndex = data(wrapped) {
                        mapGeneric(wrapped, context)
                    },
                    toStringIndex = string(any.toString()),
                    typeNameIndex = any::class.qualifiedName?.let { string(it) },
                    hashCode = any.hashCode()
                )
            }
        }
    }

    private fun mapCompositionContextHolder(
        any: Any,
        context: NormalizerContext
    ): CompositionContextHolder? {
        return with(context) {
            val reflection = CompositionContextHolderReflection(any, logger)
            reflection.ref?.let { ref ->
                CompositionContextHolder(
                    refIndex = data(ref) {
                        mapCompositionContext(ref, context)
                    },
                    toStringIndex = string(any.toString()),
                    typeNameIndex = any::class.qualifiedName?.let { string(it) },
                    hashCode = any.hashCode()
                )
            }
        }
    }

    private fun mapComposableLambda(
        any: Any,
        observations: Map<Any, Set<Any>>,
        context: NormalizerContext
    ): ComposableLambdaImpl {
        return with(context) {
            val reflection = ComposableLambdaImplReflection(any, logger)
            ComposableLambdaImpl(
                key = reflection.key,
                blockIndex = reflection.block?.let { block ->
                    data(block) {
                        mapGeneric(block, context)
                    }
                },
                tracked = reflection.tracked,
                scopeIndex = reflection.scope?.let { scope ->
                    data(scope) {
                        mapRecomposeScope(scope, observations, context)
                    }
                },
                scopeIndexes = reflection.scopes.map { scope ->
                    data(scope) {
                        mapRecomposeScope(scope, observations, context)
                    }
                },
                toStringIndex = string(any.toString()),
                typeNameIndex = any::class.qualifiedName?.let { string(it) },
                hashCode = any.hashCode()
            )
        }
    }

    private fun mapSubcomposeLayoutState(
        subcomposeLayoutState: SubcomposeLayoutState,
        context: NormalizerContext
    ): SubcomposeState {
        return with(context) {
            val reflection = SubcomposeLayoutStateReflection(subcomposeLayoutState, logger)
            SubcomposeState(
                compositions = reflection.subcompositions.map { composition ->
                    map(composition, context)
                },
                toStringIndex = string(subcomposeLayoutState.toString()),
                typeNameIndex = subcomposeLayoutState::class.qualifiedName?.let { string(it) },
                hashCode = subcomposeLayoutState.hashCode()
            )
        }
    }

    private fun mapRecomposeScope(
        recomposeScope: Any,
        observations: Map<Any, Set<Any>>,
        context: NormalizerContext
    ): RecomposeScope {
        return with(context) {
            RecomposeScope(
                stateIndexes = findObservedStateIndexes(recomposeScope, observations, context),
                toStringIndex = string(recomposeScope.toString()),
                typeNameIndex = recomposeScope::class.qualifiedName?.let { string(it) },
                hashCode = recomposeScope.hashCode()
            )
        }
    }

    private fun findObservedStateIndexes(
        recomposeScope: Any,
        observations: Map<Any, Set<Any>>,
        context: NormalizerContext
    ): List<Int> {
        return with(context) {
            val readStates = mutableListOf<Int>()
            observations.forEach { entry ->
                val state = entry.key
                val scopes = entry.value
                val index = when(state) {
                    is State<*> -> {
                        state(state) {
                            mapState(state, observations, context)
                        }
                    }
                    is SnapshotStateList<*> -> {
                        state(state) {
                            mapStateList(state, observations, context)
                        }
                    }
                    is SnapshotStateMap<*, *> -> {
                        state(state) {
                            mapStateMap(state, observations, context)
                        }
                    }
                    else -> {
                        logger.log(Logger.Level.WARNING, TAG, "Unexpected state type: $state")
                        return@forEach
                    }
                }
                if (scopes.contains(recomposeScope)) {
                    readStates.add(index)
                }
            }
            readStates
        }
    }

    private fun mapLayoutNode(any: Any, context: NormalizerContext): LayoutNode {
        return with(context) {
            val reflection = LayoutNodeReflection(any, logger)
            val lookaheadRootNode = reflection.lookaheadRoot
            val childNodes = reflection.children
            val parentNode = reflection.parent
            LayoutNode(
                lookaheadRootIndex = lookaheadRootNode?.let {
                    data(lookaheadRootNode) {
                        mapLayoutNode(lookaheadRootNode, context)
                    }
                },
                childIndexes = childNodes.map { child ->
                    data(child) {
                        mapLayoutNode(child, context)
                    }
                },
                parentIndex = parentNode?.let { parent ->
                    data(parent) {
                        mapLayoutNode(parent, context)
                    }
                },
                nodeIndexes = reflection.nodes.map { node ->
                    data(node) {
                        mapModifierNode(node, context)
                    }
                },
                coordinatorIndexes = reflection.coordinators.map { pair ->
                    val coordinator = pair.first
                    val tailNode = pair.second
                    data(coordinator) {
                        Coordinator(
                            tailNodeIndex = data(tailNode) {
                                mapModifierNode(tailNode, context)
                            },
                            toStringIndex = string(coordinator.toString()),
                            typeNameIndex = coordinator::class.qualifiedName?.let { string(it) },
                            hashCode = coordinator.hashCode()
                        )
                    }
                },
                toStringIndex = string(any.toString()),
                typeNameIndex = any::class.qualifiedName?.let { string(it) },
                hashCode = any.hashCode()
            )
        }
    }

    private fun mapModifierNode(
        node: Modifier.Node,
        context: NormalizerContext
    ): ModifierNode {
        return with(context) {
            ModifierNode(
                toStringIndex = string(node.toString()),
                typeNameIndex = node::class.qualifiedName?.let { name -> string(name) },
                hashCode = node.hashCode()
            )
        }
    }

    private fun mapStateList(
        state: SnapshotStateList<*>,
        observations: Map<Any, Set<Any>>,
        context: NormalizerContext
    ): ComposeState {
        return with(context) {
            ComposeState(
                valueIndex = data(state.toList()) { list ->
                    mapData(list, observations, context)
                },
                dependencyIndexes = emptyList(),
                readInComposition = null,
                readInSnapshotFlow = null,
                readInSnapshotStateObserver = null,
                toStringIndex = string(state.toString()),
                typeNameIndex = state::class.qualifiedName?.let { string(it) },
                hashCode = state.hashCode()
            )
        }
    }

    private fun mapStateMap(
        state: SnapshotStateMap<*, *>,
        observations: Map<Any, Set<Any>>,
        context: NormalizerContext
    ): ComposeState {
        return with(context) {
            ComposeState(
                valueIndex = data(state.toMap()) { map ->
                    mapData(map, observations, context)
                },
                dependencyIndexes = emptyList(),
                readInComposition = null,
                readInSnapshotFlow = null,
                readInSnapshotStateObserver = null,
                toStringIndex = string(state.toString()),
                typeNameIndex = state::class.qualifiedName?.let { string(it) },
                hashCode = state.hashCode()
            )
        }
    }

    private fun mapKey(key: Any, context: NormalizerContext): GroupKey {
        return with(context) {
            if (key is Int) {
                IntKey(key)
            } else {
                ObjectKey(string(key.toString()))
            }
        }
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
        private const val RECOMPOSER_HASH_KEY = 1000
        private const val COMPOSITION_CONTEXT_IMPL = "androidx.compose.runtime.ComposerImpl.CompositionContextImpl"
        private const val TAG = "CompositionExtractor"
        private const val DEBUG = false
    }
}

internal class NormalizerContext(
    private val usedString: MutableMap<String, Int> = mutableMapOf(),
    private val usedData: MutableMap<Any?, Int> = mutableMapOf(),
    private val usedState: MutableMap<Any, Int> = mutableMapOf(),
    private val usedGroup: MutableMap<CompositionGroup, Int> = mutableMapOf(),
    internal val stringTable: MutableList<String> = mutableListOf(),
    internal val dataTable: MutableList<Data> = mutableListOf(),
    internal val stateTable: MutableList<ComposeState> = mutableListOf(),
    internal val groupTable: MutableList<Group> = mutableListOf()
) {
    fun string(string: String): Int {
        return usedString.computeIfAbsent(string) {
            stringTable.add(string)
            stringTable.size - 1
        }
    }

    fun data(data: Any?, block: (Any?) -> Data): Int {
        return usedData.computeIfAbsent(data) {
            val converted = block(data)
            dataTable.add(converted)
            dataTable.size - 1
        }
    }

    fun state(state: Any, block: (Any) -> ComposeState): Int {
        return usedState.computeIfAbsent(state) {
            val converted = block(state)
            stateTable.add(converted)
            stateTable.size - 1
        }
    }

    fun group(group: CompositionGroup, block: (CompositionGroup) -> Group): Int {
        return usedGroup.computeIfAbsent(group) {
            val converted = block(group)
            groupTable.add(converted)
            groupTable.size - 1
        }
    }
}
