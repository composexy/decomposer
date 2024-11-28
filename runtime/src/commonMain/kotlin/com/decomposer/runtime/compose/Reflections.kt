@file:Suppress("UNCHECKED_CAST")
package com.decomposer.runtime.compose

import androidx.collection.ObjectIntMap
import androidx.collection.ScatterMap
import androidx.compose.runtime.Composition
import androidx.compose.runtime.CompositionContext
import androidx.compose.runtime.State
import androidx.compose.runtime.snapshots.StateObject
import androidx.compose.runtime.tooling.CompositionData
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.SubcomposeLayoutState
import com.decomposer.runtime.Logger
import com.decomposer.runtime.compose.LayoutNodeReflection.Companion
import com.decomposer.runtime.compose.RememberObserverHolderReflection.Companion.REMEMBER_OBSERVER_HOLDER_WRAPPED
import java.util.concurrent.atomic.AtomicInteger
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.KType
import kotlin.reflect.full.declaredFunctions
import kotlin.reflect.full.declaredMembers
import kotlin.reflect.jvm.isAccessible

class CompositionReflection(
    private val composition: Composition,
    private val logger: Logger
) {
    val parent: CompositionContext?
        get() {
            if (composition::class.qualifiedName == COMPOSITION_IMPL) {
                val compositionImplClazz = composition::class
                val parentProperty = compositionImplClazz.declaredMembers
                    .find { it.name == COMPOSITION_IMPL_PARENT } as? KProperty1<Any, *>
                if (parentProperty == null) {
                    logger.log(Logger.Level.WARNING, TAG, "Cannot find parent property!")
                    return null
                }
                parentProperty.isAccessible = true
                return parentProperty.get(composition) as CompositionContext?
            } else {
                logger.log(Logger.Level.WARNING, TAG, "Unknown composition type: $this")
                return null
            }
        }

    val observations: Map<Any, Set<Any>>
        get() {
            val compositionImplClazz = composition::class
            val observationsProperty = compositionImplClazz.declaredMembers
                .find { it.name == COMPOSITION_IMPL_OBSERVATIONS } as? KProperty1<Any, *>
            if (observationsProperty == null) {
                logger.log(Logger.Level.WARNING, TAG, "Cannot find observations property!")
                return emptyMap()
            }
            observationsProperty.isAccessible = true
            val observations = observationsProperty.get(composition)
            if (observations == null) {
                logger.log(Logger.Level.WARNING, TAG, "Cannot find get observations!")
                return emptyMap()
            }
            val asMapMethod = observations::class.declaredFunctions
                .find { it.name == SCOPE_MAP_AS_MAP }
            if (asMapMethod == null) {
                logger.log(Logger.Level.WARNING, TAG, "Cannot find asMap method!")
                return emptyMap()
            }
            asMapMethod.isAccessible = true
            return asMapMethod.call(observations) as Map<Any, Set<Any>>
        }

    val compositionData: CompositionData?
        get() {
            val compositionImplClazz = composition::class
            val slotTableProperty = compositionImplClazz.declaredMembers
                .find { it.name == COMPOSITION_IMPL_SLOT_TABLE } as? KProperty1<Any, *>
            if (slotTableProperty == null) {
                logger.log(Logger.Level.WARNING, TAG, "Cannot find slotTable property!")
                return null
            }
            val slotTable = slotTableProperty.get(composition)
            if (slotTable == null) {
                logger.log(Logger.Level.WARNING, TAG, "Cannot get slotTable!")
                return null
            }
            return slotTable as? CompositionData?
        }

    companion object {
        private const val COMPOSITION_IMPL = "androidx.compose.runtime.CompositionImpl"
        private const val COMPOSITION_IMPL_OBSERVATIONS = "observations"
        private const val COMPOSITION_IMPL_PARENT = "parent"
        private const val COMPOSITION_IMPL_SLOT_TABLE = "slotTable"
        private const val SCOPE_MAP_AS_MAP = "asMap"
        private const val TAG = "CompositionReflection"
    }
}

class SubcomposeLayoutStateReflection(
    private val state: SubcomposeLayoutState,
    private val logger: Logger
) {

    val subcompositions: List<Composition>
        get() {
            val stateClazz = state::class
            val stateProperty = stateClazz.declaredMembers
                .find { it.name == SUBCOMPOSE_LAYOUT_STATE_STATE } as? KProperty1<Any, *>
            if (stateProperty == null) {
                logger.log(Logger.Level.WARNING, TAG, "Cannot find state property!")
                return emptyList()
            }
            stateProperty.isAccessible = true
            val layoutNodeState = stateProperty.get(state)
            if (layoutNodeState == null) {
                logger.log(Logger.Level.WARNING, TAG, "Cannot get layout node state property!")
                return emptyList()
            }
            val subcompositionStateClazz = layoutNodeState::class
            if (subcompositionStateClazz.qualifiedName != LAYOUT_NODE_SUBCOMPOSITION_STATE) {
                logger.log(Logger.Level.WARNING, TAG, "Unknown type $subcompositionStateClazz")
                return emptyList()
            }
            val nodeStateMapProperty = subcompositionStateClazz.declaredMembers
                .find { it.name == LAYOUT_NODE_NODE_STATE } as? KProperty1<Any, *>
            if (nodeStateMapProperty == null) {
                logger.log(Logger.Level.WARNING, TAG, "Cannot fine nodeToNodeState property!")
                return emptyList()
            }
            nodeStateMapProperty.isAccessible = true
            val nodeMap = nodeStateMapProperty.get(layoutNodeState) as? HashMap<Any, Any>
                ?: (nodeStateMapProperty.get(layoutNodeState) as? ScatterMap<Any, Any>)?.asMap()
            if (nodeMap == null) {
                logger.log(Logger.Level.WARNING, TAG, "Cannot get nodeMap!")
                return emptyList()
            }
            val compositions = mutableListOf<Composition>()
            nodeMap.forEach { entry ->
                val node = entry.value
                val nodeClazz = node::class
                if (nodeClazz.qualifiedName != NODE_STATE) {
                    logger.log(Logger.Level.WARNING, TAG, "Unknown type: $nodeClazz!")
                    return emptyList()
                }
                val compositionProperty = nodeClazz.declaredMembers
                    .find { it.name == NODE_STATE_COMPOSITION } as? KProperty1<Any, *>
                if (compositionProperty == null) {
                    logger.log(Logger.Level.WARNING, TAG, "Cannot find composition property!")
                    return emptyList()
                }
                compositionProperty.isAccessible = true
                val composition = compositionProperty.get(node) as Composition?
                if (composition != null) {
                    compositions.add(composition)
                }
            }
            return compositions
        }

    companion object {
        private const val LAYOUT_NODE_SUBCOMPOSITION_STATE =
            "androidx.compose.ui.layout.LayoutNodeSubcompositionsState"
        private const val NODE_STATE = "NodeState"
        private const val SUBCOMPOSE_LAYOUT_STATE_STATE = "state"
        private const val LAYOUT_NODE_NODE_STATE = "nodeToNodeState"
        private const val NODE_STATE_COMPOSITION = "composition"
        private const val TAG = "SubcomposeLayoutStateReflection"
    }
}

class StateReflection(
    private val state: State<*>,
    private val logger: Logger
) {
    private val readerKind: Int?
        get() {
            val stateClazz = state::class
            val stateObjectImplClazz = stateClazz.cast(STATE_OBJECT_IMPL)
            if (stateObjectImplClazz == null) {
                logger.log(Logger.Level.WARNING, TAG, "Unknown state type: $state")
                return null
            }
            val readerKindProperty = stateObjectImplClazz.members
                .find { it.name == STATE_OBJECT_READER_KIND } as? KProperty1<Any, *>
            if (readerKindProperty == null) {
                logger.log(Logger.Level.WARNING, TAG, "Cannot find readerKind property!")
                return null
            }
            readerKindProperty.isAccessible = true
            val readerKind = readerKindProperty.get(state) as AtomicInteger
            return readerKind.get()
        }

    val readInComposition: Boolean?
        get() {
            return readerKind?.let {
                it and (1 shl 0) != 0
            }
        }

    val readInSnapshotObserver: Boolean?
        get() {
            return readerKind?.let {
                it and (1 shl 1) != 0
            }
        }

    val readInSnapshotFlow: Boolean?
        get() {
            return readerKind?.let {
                it and (1 shl 2) != 0
            }
        }

    val dependencies: List<StateObject>
        get() {
            val stateClazz = state::class
            val superTypes = stateClazz.allSuperTypes
            if (!superTypes.any { it.toString().contains(DERIVED_STATE) }) {
                return emptyList()
            }
            val currentRecordProperty = stateClazz.members
                .find { it.name == DERIVED_STATE_CURRENT_RECORD } as? KProperty1<Any, *>
            if (currentRecordProperty == null) {
                logger.log(Logger.Level.WARNING, TAG, "Cannot find currentRecord property!")
                return emptyList()
            }
            currentRecordProperty.isAccessible = true
            val record = currentRecordProperty.get(state)
            if (record == null) {
                logger.log(Logger.Level.WARNING, TAG, "Cannot get currentRecord!")
                return emptyList()
            }
            val recordClazz = record::class
            val dependenciesProperty = recordClazz.members
                .find { it.name == DERIVED_STATE_RECORD_DEPENDENCIES } as? KProperty1<Any, *>
            if (dependenciesProperty == null) {
                logger.log(Logger.Level.WARNING, TAG, "Cannot find dependencies property!")
                return emptyList()
            }
            dependenciesProperty.isAccessible = true
            val dependencies = dependenciesProperty.get(record) as ObjectIntMap<StateObject>
            val states = mutableListOf<StateObject>()
            dependencies.forEachKey {
                states.add(it)
            }
            return states
        }

    companion object {
        private const val STATE_OBJECT_IMPL = "androidx.compose.runtime.snapshots.StateObjectImpl"
        private const val DERIVED_STATE = "androidx.compose.runtime.DerivedState"
        private const val STATE_OBJECT_READER_KIND = "readerKind"
        private const val DERIVED_STATE_CURRENT_RECORD = "currentRecord"
        private const val DERIVED_STATE_RECORD_DEPENDENCIES = "dependencies"
        private const val TAG = "StateReflection"
    }
}

class ComposableLambdaImplReflection(private val lambda: Any, private val logger: Logger) {

    val key: Int
        get() {
            val lambdaClazz = lambda::class
            val keyProperty = lambdaClazz.declaredMembers
                .find { it.name == COMPOSABLE_LAMBDA_KEY } as? KProperty1<Any, *>
            if (keyProperty == null) {
                logger.log(Logger.Level.WARNING, TAG, "Cannot find key property!")
                return 0
            }
            keyProperty.isAccessible = true
            return keyProperty.get(lambda) as Int
        }

    val tracked: Boolean
        get() {
            val lambdaClazz = lambda::class
            val trackedProperty = lambdaClazz.declaredMembers
                .find { it.name == COMPOSABLE_LAMBDA_TRACKED } as? KProperty1<Any, *>
            if (trackedProperty == null) {
                logger.log(Logger.Level.WARNING, TAG, "Cannot find tracked property!")
                return false
            }
            trackedProperty.isAccessible = true
            return trackedProperty.get(lambda) as Boolean
        }

    val scopeHash: Int?
        get() {
            val lambdaClazz = lambda::class
            val scopeProperty = lambdaClazz.declaredMembers
                .find { it.name == COMPOSABLE_LAMBDA_SCOPE } as? KProperty1<Any, *>
            if (scopeProperty == null) {
                logger.log(Logger.Level.WARNING, TAG, "Cannot find scope property!")
                return null
            }
            scopeProperty.isAccessible = true
            return scopeProperty.get(lambda)?.hashCode()
        }

    val scopeHashes: List<Int>
        get() {
            val lambdaClazz = lambda::class
            val scopesProperty = lambdaClazz.declaredMembers
                .find { it.name == COMPOSABLE_LAMBDA_SCOPES } as? KProperty1<Any, *>
            if (scopesProperty == null) {
                logger.log(Logger.Level.WARNING, TAG, "Cannot find scopes property!")
                return emptyList()
            }
            scopesProperty.isAccessible = true
            return (scopesProperty.get(lambda) as List<Any>?)?.map {
                it.hashCode()
            } ?: emptyList()
        }

    companion object {
        private const val COMPOSABLE_LAMBDA_KEY = "key"
        private const val COMPOSABLE_LAMBDA_TRACKED = "tracked"
        private const val COMPOSABLE_LAMBDA_SCOPE = "scope"
        private const val COMPOSABLE_LAMBDA_SCOPES = "scopes"
        private const val TAG = "ComposableLambdaImplReflection"
    }
}

class LayoutNodeReflection(private val layoutNode: Any, private val logger: Logger) {
    val lookaheadRootHash: Int?
        get() {
            val layoutNodeClazz = layoutNode::class
            val lookAheadRootProperty = layoutNodeClazz.declaredMembers
                .find { it.name == LAYOUT_NODE_LOOKAHEAD_ROOT } as? KProperty1<Any, *>
            if (lookAheadRootProperty == null) {
                logger.log(Logger.Level.WARNING, TAG, "Cannot find lookAheadRoot property!")
                return null
            }
            lookAheadRootProperty.isAccessible = true
            return lookAheadRootProperty.get(layoutNode)?.hashCode()
        }

    val childrenHashes: List<Int>
        get() {
            val layoutNodeClazz = layoutNode::class
            val childrenProperty = layoutNodeClazz.declaredMembers
                .find { it.name == LAYOUT_NODE_CHILDREN } as? KProperty1<Any, *>
            if (childrenProperty == null) {
                logger.log(Logger.Level.WARNING, TAG, "Cannot find children property!")
                return emptyList()
            }
            childrenProperty.isAccessible = true
            return (childrenProperty.get(layoutNode) as List<Any>).map {
                it.hashCode()
            }
        }

    val parentHash: Int?
        get() {
            val layoutNodeClazz = layoutNode::class
            val parentProperty = layoutNodeClazz.declaredMembers
                .find { it.name == LAYOUT_NODE_PARENT } as? KProperty1<Any, *>
            if (parentProperty == null) {
                logger.log(Logger.Level.WARNING, TAG, "Cannot find parent property!")
                return null
            }
            parentProperty.isAccessible = true
            return parentProperty.get(layoutNode)?.hashCode()
        }

    val nodes: List<Modifier.Node>
        get() {
            val layoutNodeClazz = layoutNode::class
            val nodesProperty = layoutNodeClazz.declaredMembers
                .find { it.name == LAYOUT_NODE_NODES } as? KProperty1<Any, *>
            if (nodesProperty == null) {
                logger.log(Logger.Level.WARNING, TAG, "Cannot find nodes property!")
                return emptyList()
            }
            nodesProperty.isAccessible = true
            val nodes = nodesProperty.get(layoutNode)
            if (nodes == null) {
                logger.log(Logger.Level.WARNING, TAG, "Cannot get nodes!")
                return emptyList()
            }
            val nodesClazz = nodes::class
            val headProperty = nodesClazz.declaredMembers
                .find { it.name == NODE_CHAIN_HEAD } as? KProperty1<Any, *>
            if (headProperty == null) {
                logger.log(Logger.Level.WARNING, TAG, "Cannot find head property!")
                return emptyList()
            }
            headProperty.isAccessible = true
            val head = headProperty.get(nodes) as Modifier.Node
            var current: Modifier.Node? = head
            val modifierNodes = mutableListOf<Modifier.Node>()
            while (current != null) {
                modifierNodes.add(current)
                current = current.child
            }
            return modifierNodes
        }

    val coordinators: List<Pair<Any, Modifier.Node>>
        get() {
            val layoutNodeClazz = layoutNode::class
            val outerCoordinatorProperty = layoutNodeClazz.declaredMembers
                .find { it.name == LAYOUT_NODE_OUTER_COORDINATOR } as? KProperty1<Any, *>
            if (outerCoordinatorProperty == null) {
                logger.log(Logger.Level.WARNING, TAG, "Cannot find outerCoordinator property!")
                return emptyList()
            }
            outerCoordinatorProperty.isAccessible = true
            val outerCoordinator = outerCoordinatorProperty.get(layoutNode)
            if (outerCoordinator == null) {
                logger.log(Logger.Level.WARNING, TAG, "Cannot get outerCoordinator!")
                return emptyList()
            }
            val wrappedProperty = outerCoordinator::class.members
                .find { it.name == NODE_COORDINATOR_WRAPPED } as? KProperty1<Any, *>
            if (wrappedProperty == null) {
                logger.log(Logger.Level.WARNING, TAG, "Cannot find wrapped property!")
                return emptyList()
            }
            var current = outerCoordinator
            val coordinators = mutableListOf<Pair<Any, Modifier.Node>>()
            while (current != null) {
                val tailProperty = current::class.members
                    .find { it.name == NODE_COORDINATOR_TAIL } as? KProperty1<Any, *>
                if (tailProperty == null) {
                    logger.log(Logger.Level.WARNING, TAG, "Cannot find tail property!")
                    return emptyList()
                }
                val tail = tailProperty.get(current) as Modifier.Node
                coordinators.add(Pair(current, tail))
                val wrapped = wrappedProperty.get(current)
                current = wrapped
            }
            return coordinators
        }

    private val Modifier.Node.child: Modifier.Node?
        get() {
            val nodeClazz = this::class
            val childProperty = nodeClazz.members
                .find { it.name == MODIFIER_NODE_CHILD } as? KProperty1<Any, *>
            if (childProperty == null) {
                logger.log(Logger.Level.WARNING, TAG, "Cannot find child property!")
                return null
            }
            childProperty.isAccessible = true
            return childProperty.get(this) as Modifier.Node?
        }

    companion object {
        private const val LAYOUT_NODE_LOOKAHEAD_ROOT = "lookaheadRoot"
        private const val LAYOUT_NODE_CHILDREN = "children"
        private const val LAYOUT_NODE_PARENT = "parent"
        private const val LAYOUT_NODE_NODES = "nodes"
        private const val LAYOUT_NODE_OUTER_COORDINATOR = "outerCoordinator"
        private const val NODE_CHAIN_HEAD = "head"
        private const val MODIFIER_NODE_CHILD = "child"
        private const val NODE_COORDINATOR_WRAPPED = "wrapped"
        private const val NODE_COORDINATOR_TAIL = "tail"
        private const val TAG = "LayoutNodeReflection"
    }
}

class RememberObserverHolderReflection(private val holder: Any, private val logger: Logger) {

    val wrapped: Any?
        get() {
            val holderClazz = holder::class
            val wrappedProperty = holderClazz.declaredMembers
                .find { it.name == REMEMBER_OBSERVER_HOLDER_WRAPPED } as? KProperty1<Any, *>
            if (wrappedProperty == null) {
                logger.log(Logger.Level.WARNING, TAG, "Cannot find wrapped property!")
                return null
            }
            wrappedProperty.isAccessible = true
            return wrappedProperty.get(holder)
        }

    companion object {
        private const val REMEMBER_OBSERVER_HOLDER_WRAPPED = "wrapped"
        private const val TAG = "RememberObserverHolderReflection"
    }
}

class CompositionContextHolderReflection(private val holder: Any, private val logger: Logger) {

    val ref: CompositionContext?
        get() {
            val holderClazz = holder::class
            val refProperty = holderClazz.declaredMembers
                .find { it.name == COMPOSITION_CONTEXT_HOLDER_REF } as? KProperty1<Any, *>
            if (refProperty == null) {
                logger.log(Logger.Level.WARNING, TAG, "Cannot find ref property!")
                return null
            }
            refProperty.isAccessible = true
            return refProperty.get(holder) as? CompositionContext
        }

    companion object {
        private const val COMPOSITION_CONTEXT_HOLDER_REF = "ref"
        private const val TAG = "CompositionContextHolderReflection"
    }
}

private val KClass<*>.allSuperTypes: List<KType>
    get() {
        val superTypes = mutableListOf<KType>()
        val immediateSupers = this.supertypes
        superTypes.addAll(immediateSupers)
        immediateSupers.forEach {
            it.classifier?.let { classifier ->
                if (classifier is KClass<*>) {
                    superTypes.addAll(classifier.allSuperTypes)
                }
            }
        }
        return superTypes
    }

private fun KClass<*>.cast(identifier: String): KClass<*>? {
    val superClasses = mutableListOf<KClass<*>>()
    superClasses.add(this)
    while (superClasses.isNotEmpty()) {
        val clazz = superClasses.removeLast()
        if (clazz.qualifiedName?.contains(identifier) == true) return clazz
        clazz.supertypes.forEach {
            it.classifier?.let { classifier ->
                if (classifier is KClass<*>) {
                    superClasses.add(classifier)
                }
            }
        }
    }
    return null
}
