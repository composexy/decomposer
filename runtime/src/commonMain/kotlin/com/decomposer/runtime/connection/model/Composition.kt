package com.decomposer.runtime.connection.model

import kotlinx.serialization.Serializable

@Serializable
class CompositionRoots(
    val compositionData: List<CompositionRoot>,
    val stateTable: Set<ComposeState>
)

@Serializable
class CompositionRoot(
    val context: Context?,
    val groups: List<Int>,
    val groupTable: List<Group>
)

@Serializable
class Group(
    val attributes: Attributes,
    val data: List<Data>,
    val children: List<Int>
)

@Serializable
class Attributes(
    val key: GroupKey,
    val sourceInformation: String?
)

@Serializable
sealed interface GroupKey

@Serializable
class IntKey(val value: Int) : GroupKey

@Serializable
class ObjectKey(val value: String) : GroupKey

@Serializable
sealed interface Data {
    val toString: String
    val typeName: String?
    val hashCode: Int
}

@Serializable
data object EmptyData : Data {
    override val toString = "EmptyData"
    override val typeName = null
    override val hashCode = 0
}

@Serializable
class Context(
    val compoundHashKey: Int,
    override val toString: String,
    override val typeName: String?,
    override val hashCode: Int
) : Data

@Serializable
class Default(
    override val toString: String,
    override val typeName: String?,
    override val hashCode: Int
) : Data

@Serializable
class ComposeState(
    val value: Data,
    val dependencyHashes: List<Int>,
    val readInComposition: Boolean?,
    val readInSnapshotFlow: Boolean?,
    val readInSnapshotStateObserver: Boolean?,
    override val toString: String,
    override val typeName: String?,
    override val hashCode: Int
) : Data

@Serializable
class RecomposeScope(
    val composeStateHashes: List<Int>,
    override val toString: String,
    override val typeName: String?,
    override val hashCode: Int
) : Data

@Serializable
class SubcomposeState(
    val compositions: List<CompositionRoot>,
    override val toString: String,
    override val typeName: String?,
    override val hashCode: Int
) : Data

@Serializable
class RememberObserverHolder(
    val wrapped: Data
) : Data by wrapped

@Serializable
class CompositionContextHolder(
    val ref: Context
) : Data by ref

@Serializable
class ComposableLambdaImpl(
    val key: Int,
    val block: Data?,
    val tracked: Boolean,
    val scopeHash: Int?,
    val scopeHashes: List<Int>,
    override val toString: String,
    override val typeName: String?,
    override val hashCode: Int
) : Data

@Serializable
class LayoutNode(
    val lookaheadRootHash: Int?,
    val childrenHashes: List<Int>,
    val parentHash: Int?,
    val nodes: List<ModifierNode>,
    val coordinators: List<Coordinator>,
    override val toString: String,
    override val typeName: String?,
    override val hashCode: Int
) : Data

@Serializable
data class ModifierNode(
    override val toString: String,
    override val typeName: String?,
    override val hashCode: Int
) : Data

@Serializable
class Coordinator(
    val tailNodeHash: Int,
    override val toString: String,
    override val typeName: String?,
    override val hashCode: Int
) : Data
