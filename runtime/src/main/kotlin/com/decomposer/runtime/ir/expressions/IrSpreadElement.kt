package com.decomposer.runtime.ir.expressions

import com.decomposer.runtime.ir.IrElementBase

data class IrSpreadElement(
    val expression: IrExpression,
    override val attributeMap: List<Any?>?,
    override val startOffset: Int,
    override val endOffset: Int
) : IrElementBase, IrVarargElement
