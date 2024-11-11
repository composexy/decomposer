package com.decomposer.runtime.ir.expressions

data class IrExpressionBody(
    val expression: IrExpression,
    override val startOffset: Int,
    override val attributeMap: List<Any?>?,
    override val endOffset: Int
) : IrBody()
