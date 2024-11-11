package com.decomposer.runtime.ir.expressions

import com.decomposer.runtime.ir.IrType
import com.decomposer.runtime.ir.declarations.IrAttributeContainer

data class IrWhileLoop(
    override val startOffset: Int,
    override val origin: IrStatementOrigin?,
    override val body: IrExpression?,
    override val condition: IrExpression,
    override val label: String?,
    override val type: IrType,
    override val attributeMap: List<Any?>?,
    override val endOffset: Int,
    override val attributeOwnerId: IrAttributeContainer,
    override val originalBeforeInline: IrAttributeContainer?
) : IrLoop()
