package com.decomposer.runtime.ir.expressions

import com.decomposer.runtime.ir.IrType
import com.decomposer.runtime.ir.declarations.IrAttributeContainer
import com.decomposer.runtime.ir.symbols.IrClassSymbol
import com.decomposer.runtime.ir.symbols.IrSimpleFunctionSymbol

data class IrCall(
    override val symbol: IrSimpleFunctionSymbol,
    val superQualifierSymbol: IrClassSymbol?,
    override val startOffset: Int,
    override val dispatchReceiver: IrExpression?,
    override val extensionReceiver: IrExpression?,
    override val origin: IrStatementOrigin?,
    override val valueArguments: List<IrExpression?>,
    override val typeArguments: List<IrType?>,
    override val type: IrType,
    override val attributeMap: List<Any?>?,
    override val endOffset: Int,
    override val attributeOwnerId: IrAttributeContainer,
    override val originalBeforeInline: IrAttributeContainer?
) : IrFunctionAccessExpression()
