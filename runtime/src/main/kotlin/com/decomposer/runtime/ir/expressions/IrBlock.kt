package com.decomposer.runtime.ir.expressions

import com.decomposer.runtime.ir.IrStatement
import com.decomposer.runtime.ir.IrType
import com.decomposer.runtime.ir.declarations.IrAttributeContainer

data class IrBlock(
    override val startOffset: Int,
    override var origin: IrStatementOrigin?,
    override var type: IrType,
    override val attributeMap: List<Any?>?,
    override val endOffset: Int,
    override val attributeOwnerId: IrAttributeContainer,
    override val originalBeforeInline: IrAttributeContainer?,
    override val statements: MutableList<IrStatement>
) : IrContainerExpression()
