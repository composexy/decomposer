package com.decomposer.runtime.ir.expressions

import com.decomposer.runtime.ir.IrStatement

data class IrBlockBody(
    override val attributeMap: List<Any?>?,
    override val startOffset: Int,
    override val endOffset: Int,
    override val statements: MutableList<IrStatement>
) : IrBody(), IrStatementContainer
