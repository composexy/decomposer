package com.decomposer.runtime.ir.expressions

data class IrSyntheticBody(
    val kind: IrSyntheticBodyKind,
    override val startOffset: Int,
    override val attributeMap: List<Any?>?,
    override val endOffset: Int
) : IrBody()

enum class IrSyntheticBodyKind {
    ENUM_VALUES,
    ENUM_VALUEOF,
    ENUM_ENTRIES
}
