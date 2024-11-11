package com.decomposer.runtime.ir.expressions

import com.decomposer.runtime.ir.IrType
import com.decomposer.runtime.ir.declarations.IrAttributeContainer

data class IrDynamicOperatorExpression(
    val operator: IrDynamicOperator,
    val receiver: IrExpression,
    val arguments: MutableList<IrExpression>,
    override val startOffset: Int,
    override val type: IrType,
    override val attributeMap: List<Any?>?,
    override val endOffset: Int,
    override val attributeOwnerId: IrAttributeContainer,
    override val originalBeforeInline: IrAttributeContainer?
) : IrDynamicExpression()
