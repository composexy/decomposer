package com.decomposer.runtime.ir.expressions

data class SyntheticBody(
    val kind: IrSyntheticBodyKind,
    override val startOffset: Int,
    override val attributeMap: List<Any?>?,
    override val endOffset: Int
) : Body()

enum class IrSyntheticBodyKind {
    ENUM_VALUES,
    ENUM_VALUEOF,
    ENUM_ENTRIES
}
