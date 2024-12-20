package com.decomposer.runtime.connection.model

import kotlinx.serialization.Serializable

@Serializable
class CompositionRoots(
    val compositionData: List<CompositionRoot>,
    val stateTable: List<ComposeState>,
    val snapshotObserverStateTable: Map<Int, List<Int>>,
    val stringTable: List<String>,
    val dataTable: List<Data>,
    val groupTable: List<Group>
)

@Serializable
class CompositionRoot(
    val contextIndex: Int?,
    val groupIndexes: List<Int>
)

@Serializable
class Group(
    val attributes: Attributes,
    val dataIndexes: List<Int>,
    val childIndexes: List<Int>
)

@Serializable
class Attributes(
    val key: GroupKey,
    val sourceInformationIndex: Int?
)

@Serializable
sealed interface GroupKey

@Serializable
class IntKey(val value: Int) : GroupKey

@Serializable
class ObjectKey(val valueIndex: Int) : GroupKey

@Serializable
sealed interface Data {
    val toStringIndex: Int
    val typeNameIndex: Int?
    val hashCode: Int
}

@Serializable
class EmptyData(
    override val toStringIndex: Int,
    override val typeNameIndex: Int?,
    override val hashCode: Int
) : Data

@Serializable
class Context(
    val compoundHashKey: Int,
    override val toStringIndex: Int,
    override val typeNameIndex: Int?,
    override val hashCode: Int
) : Data

@Serializable
class Default(
    override val toStringIndex: Int,
    override val typeNameIndex: Int?,
    override val hashCode: Int
) : Data

@Serializable
class ComposeState(
    val valueIndex: Int,
    val dependencyIndexes: List<Int>,
    val readInComposition: Boolean?,
    val readInSnapshotFlow: Boolean?,
    val readInSnapshotStateObserver: Boolean?,
    override val toStringIndex: Int,
    override val typeNameIndex: Int?,
    override val hashCode: Int
) : Data

@Serializable
class RecomposeScope(
    val stateIndexes: List<Int>,
    override val toStringIndex: Int,
    override val typeNameIndex: Int?,
    override val hashCode: Int
) : Data

@Serializable
class SubcomposeState(
    val compositions: List<CompositionRoot>,
    override val toStringIndex: Int,
    override val typeNameIndex: Int?,
    override val hashCode: Int
) : Data

@Serializable
class RememberObserverHolder(
    val wrappedIndex: Int,
    override val toStringIndex: Int,
    override val typeNameIndex: Int?,
    override val hashCode: Int
) : Data

@Serializable
class CompositionContextHolder(
    val refIndex: Int,
    override val toStringIndex: Int,
    override val typeNameIndex: Int?,
    override val hashCode: Int
) : Data

@Serializable
class ComposableLambdaImpl(
    val key: Int,
    val blockIndex: Int?,
    val tracked: Boolean,
    val scopeIndex: Int?,
    val scopeIndexes: List<Int>,
    override val toStringIndex: Int,
    override val typeNameIndex: Int?,
    override val hashCode: Int
) : Data

@Serializable
class LayoutNode(
    val lookaheadRootIndex: Int?,
    val childIndexes: List<Int>,
    val parentIndex: Int?,
    val nodeIndexes: List<Int>,
    val coordinatorIndexes: List<Int>,
    override val toStringIndex: Int,
    override val typeNameIndex: Int?,
    override val hashCode: Int
) : Data

@Serializable
data class ModifierNode(
    override val toStringIndex: Int,
    override val typeNameIndex: Int?,
    override val hashCode: Int
) : Data

@Serializable
class Coordinator(
    val tailNodeIndex: Int,
    override val toStringIndex: Int,
    override val typeNameIndex: Int?,
    override val hashCode: Int
) : Data
