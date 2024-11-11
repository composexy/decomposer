package com.decomposer.runtime.ir.expressions

import com.decomposer.runtime.ir.IrElement
import com.decomposer.runtime.ir.IrElementBase
import com.decomposer.runtime.ir.declarations.IrVariable

data class IrCatch(
    val catchParameter: IrVariable,
    val result: IrExpression,
    val origin: IrStatementOrigin?,
    override val attributeMap: List<Any?>?,
    override val startOffset: Int,
    override val endOffset: Int
) : IrElementBase, IrElement
