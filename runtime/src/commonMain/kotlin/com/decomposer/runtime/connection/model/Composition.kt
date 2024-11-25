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
    val compoundHashKey: Int
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

class IntKey(val value: Int) : GroupKey

class ObjectKey(
    val type: String,
    val value: String
) : GroupKey

@Serializable
sealed interface Data

class Plain(
    val value: String
) : Data

abstract class State<T>(
    val value: T,
    val readerKind: Int,
) : Data

class DoubleState(
    value: Double,
    readerKind: Int
) : State<Double>(value, readerKind)

class IntState(
    value: Int,
    readerKind: Int
) : State<Int>(value, readerKind)

class LongState(
    value: Long,
    readerKind: Int
) : State<Long>(value, readerKind)

class FloatState(
    value: Float,
    readerKind: Int
) : State<Float>(value, readerKind)

class PlainState(
    value: String,
    readerKind: Int
) : State<String>(value, readerKind)

class RecomposeScope(
    val states: List<State<*>>
) : Data
