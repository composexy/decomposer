package com.decomposer.runtime.ir.expressions

import com.decomposer.runtime.ir.IrType
import com.decomposer.runtime.ir.declarations.IrAttributeContainer

data class IrErrorCallExpression(
    val explicitReceiver: IrExpression?,
    val arguments: MutableList<IrExpression>,
    override val startOffset: Int,
    val description: String,
    override val type: IrType,
    override val attributeMap: List<Any?>?,
    override val endOffset: Int,
    override val attributeOwnerId: IrAttributeContainer,
    override val originalBeforeInline: IrAttributeContainer?
) : IrExpression()
