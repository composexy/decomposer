package com.decomposer.runtime.ir.expressions

import com.decomposer.runtime.ir.IrType
import com.decomposer.runtime.ir.declarations.IrAttributeContainer

data class IrDoWhileLoop(
    override val startOffset: Int,
    override var origin: IrStatementOrigin?,
    override var body: IrExpression?,
    override var condition: IrExpression,
    override var label: String?,
    override val type: IrType,
    override val attributeMap: List<Any?>?,
    override val endOffset: Int,
    override val attributeOwnerId: IrAttributeContainer,
    override val originalBeforeInline: IrAttributeContainer?
) : IrLoop()
