package com.decomposer.runtime.ir.expressions

import com.decomposer.runtime.ir.IrType
import com.decomposer.runtime.ir.declarations.IrAttributeContainer
import com.decomposer.runtime.ir.symbols.IrLocalDelegatedPropertySymbol
import com.decomposer.runtime.ir.symbols.IrSimpleFunctionSymbol
import com.decomposer.runtime.ir.symbols.IrVariableSymbol

data class IrLocalDelegatedPropertyReference(
    val delegate: IrVariableSymbol,
    val getter: IrSimpleFunctionSymbol,
    val setter: IrSimpleFunctionSymbol?,
    override val startOffset: Int,
    override var symbol: IrLocalDelegatedPropertySymbol,
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
) : IrCallableReference<IrLocalDelegatedPropertySymbol>()
