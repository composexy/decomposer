package com.decomposer.runtime.ir.expressions

import com.decomposer.runtime.ir.IrType
import com.decomposer.runtime.ir.declarations.IrAttributeContainer
import com.decomposer.runtime.ir.declarations.IrVariable

data class IrSuspensionPoint(
    val suspensionPointIdParameter: IrVariable,
    val result: IrExpression,
    val resumeResult: IrExpression,
    override val startOffset: Int,
    override val type: IrType,
    override val attributeMap: List<Any?>?,
    override val endOffset: Int,
    override val attributeOwnerId: IrAttributeContainer,
    override val originalBeforeInline: IrAttributeContainer?
) : IrExpression()
