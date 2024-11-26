package com.decomposer.runtime.connection.model

import kotlinx.serialization.Serializable

@Serializable
class CompositionRoots(
    val compositionData: List<Root>
)

@Serializable
class Root(
    val context: Context?,
    val groups: List<Group>
)

@Serializable
class Context(
    val compoundHashKey: Int,
    override val hashCode: String
) : Data

@Serializable
class Group(
    val attributes: Attributes,
    val data: List<Data>,
    val layoutNode: LayoutNode?,
    val children: List<Group>
)

@Serializable
class Attributes(
    val key: GroupKey,
    val sourceInformation: String?
)

@Serializable
class LayoutNode

@Serializable
sealed interface GroupKey

@Serializable
class IntKey(val value: Int) : GroupKey

@Serializable
class ObjectKey(
    val type: String,
    val value: String
) : GroupKey

@Serializable
sealed interface Data {
    val hashCode: String
}

@Serializable
class Plain(
    val value: String,
    override val hashCode: String
) : Data

@Serializable
sealed interface State : Data {
    val readerKind: Int
}

class DoubleState(
    val value: Double,
    override val readerKind: Int,
    override val hashCode: String
) : State

@Serializable
class IntState(
    val value: Int,
    override val readerKind: Int,
    override val hashCode: String
) : State

@Serializable
class LongState(
    val value: Long,
    override val readerKind: Int,
    override val hashCode: String
) : State

@Serializable
class FloatState(
    val value: Float,
    override val readerKind: Int,
    override val hashCode: String
) : State

@Serializable
class PlainState(
    val value: String,
    override val readerKind: Int,
    override val hashCode: String
) : State

@Serializable
class RecomposeScope(
    val states: List<State>,
    override val hashCode: String
) : Data
