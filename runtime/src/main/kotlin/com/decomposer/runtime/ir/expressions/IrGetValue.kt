package com.decomposer.runtime.ir.expressions

import com.decomposer.runtime.ir.IrType
import com.decomposer.runtime.ir.declarations.IrAttributeContainer
import com.decomposer.runtime.ir.symbols.IrValueSymbol

data class IrGetValue(
    override val startOffset: Int,
    override var symbol: IrValueSymbol,
    override var origin: IrStatementOrigin?,
    override val type: IrType,
    override val attributeMap: List<Any?>?,
    override val endOffset: Int,
    override val attributeOwnerId: IrAttributeContainer,
    override val originalBeforeInline: IrAttributeContainer?
) : IrValueAccessExpression()
