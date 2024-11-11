package com.decomposer.runtime.ir.expressions

import com.decomposer.runtime.ir.IrElement
import com.decomposer.runtime.ir.IrElementBase

data class IrBranch(
    val condition: IrExpression,
    val result: IrExpression,
    override val attributeMap: List<Any?>?,
    override val startOffset: Int,
    override val endOffset: Int
) : IrElementBase, IrElement
