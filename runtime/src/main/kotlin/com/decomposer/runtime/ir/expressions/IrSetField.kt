package com.decomposer.runtime.ir.expressions

import com.decomposer.runtime.ir.IrType
import com.decomposer.runtime.ir.declarations.IrAttributeContainer
import com.decomposer.runtime.ir.symbols.IrClassSymbol
import com.decomposer.runtime.ir.symbols.IrFieldSymbol

data class IrSetField(
    val value: IrExpression,
    override val startOffset: Int,
    override val symbol: IrFieldSymbol,
    override val superQualifierSymbol: IrClassSymbol?,
    override val receiver: IrExpression?,
    override val origin: IrStatementOrigin?,
    override val type: IrType,
    override val attributeMap: List<Any?>?,
    override val endOffset: Int,
    override val attributeOwnerId: IrAttributeContainer,
    override val originalBeforeInline: IrAttributeContainer?
) : IrFieldAccessExpression()
