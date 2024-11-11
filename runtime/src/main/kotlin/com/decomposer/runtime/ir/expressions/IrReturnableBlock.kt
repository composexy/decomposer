package com.decomposer.runtime.ir.expressions

import com.decomposer.runtime.ir.IrStatement
import com.decomposer.runtime.ir.IrType
import com.decomposer.runtime.ir.declarations.IrAttributeContainer
import com.decomposer.runtime.ir.declarations.IrReturnTarget
import com.decomposer.runtime.ir.declarations.IrSymbolOwner
import com.decomposer.runtime.ir.symbols.IrReturnableBlockSymbol

data class IrReturnableBlock(
    override val symbol: IrReturnableBlockSymbol,
    override val attributeMap: List<Any?>?,
    override val origin: IrStatementOrigin?,
    override val type: IrType,
    override val startOffset: Int,
    override val endOffset: Int,
    override val attributeOwnerId: IrAttributeContainer,
    override val originalBeforeInline: IrAttributeContainer?,
    override val statements: MutableList<IrStatement>
) : IrContainerExpression(), IrSymbolOwner, IrReturnTarget
