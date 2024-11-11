package com.decomposer.runtime.ir.expressions

import com.decomposer.runtime.ir.IrStatement
import com.decomposer.runtime.ir.IrType
import com.decomposer.runtime.ir.declarations.IrAttributeContainer
import com.decomposer.runtime.ir.declarations.IrFileEntry
import com.decomposer.runtime.ir.symbols.IrFunctionSymbol

data class IrInlinedFunctionBlock(
    val inlineFunctionSymbol: IrFunctionSymbol?,
    val fileEntry: IrFileEntry,
    override val startOffset: Int,
    override var origin: IrStatementOrigin?,
    override var type: IrType,
    override val attributeMap: List<Any?>?,
    override val endOffset: Int,
    override val attributeOwnerId: IrAttributeContainer,
    override val originalBeforeInline: IrAttributeContainer?,
    override val statements: MutableList<IrStatement>
) : IrContainerExpression()
