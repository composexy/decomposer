package com.decomposer.runtime.ir.expressions

data class ExpressionBody(
    val expression: Expression,
    override val startOffset: Int,
    override val attributeMap: List<Any?>?,
    override val endOffset: Int
) : Body()
