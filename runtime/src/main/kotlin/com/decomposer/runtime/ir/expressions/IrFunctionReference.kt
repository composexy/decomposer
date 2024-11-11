package com.decomposer.runtime.ir.expressions

import com.decomposer.runtime.ir.IrType
import com.decomposer.runtime.ir.declarations.IrAttributeContainer
import com.decomposer.runtime.ir.symbols.IrFunctionSymbol

data class IrFunctionReference(
    val reflectionTarget: IrFunctionSymbol?,
    override val startOffset: Int,
    override var symbol: IrFunctionSymbol,
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
) : IrCallableReference<IrFunctionSymbol>()
