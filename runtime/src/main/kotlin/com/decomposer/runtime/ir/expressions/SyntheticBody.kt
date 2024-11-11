package com.decomposer.runtime.ir.expressions

import kotlinx.serialization.Serializable

@Serializable
data class SyntheticBody(
    val kind: IrSyntheticBodyKind,
    override val startOffset: Int,
    override val endOffset: Int
) : Body()

@Serializable
enum class IrSyntheticBodyKind {
    ENUM_VALUES,
    ENUM_VALUEOF,
    ENUM_ENTRIES
}
