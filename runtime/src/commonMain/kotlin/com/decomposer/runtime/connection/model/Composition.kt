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

class EmptyData : Data {
    override val toString = "Empty"
}

@Serializable
class Context(
    val compoundHashKey: Int,
    override val toString: String
) : Data

@Serializable
class Group(
    val attributes: Attributes,
    val data: List<Data>,
    val children: List<Group>
)

@Serializable
class Attributes(
    val key: GroupKey,
    val sourceInformation: String?
)

@Serializable
class LayoutNode(override val toString: String) : Data

@Serializable
sealed interface GroupKey

@Serializable
class IntKey(val value: Int) : GroupKey

@Serializable
class ObjectKey(val value: String) : GroupKey

@Serializable
sealed interface Data {
    val toString: String
}

@Serializable
class Generic(override val toString: String) : Data

@Serializable
class ComposeState(
    val value: String,
    override val toString: String
) : Data

@Serializable
class RecomposeScope(
    val composeStates: List<ComposeState>,
    override val toString: String
) : Data
