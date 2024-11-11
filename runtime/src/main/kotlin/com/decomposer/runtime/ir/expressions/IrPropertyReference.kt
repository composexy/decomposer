package com.decomposer.runtime.ir.expressions

import com.decomposer.runtime.ir.IrType
import com.decomposer.runtime.ir.declarations.IrAttributeContainer
import com.decomposer.runtime.ir.symbols.IrFieldSymbol
import com.decomposer.runtime.ir.symbols.IrPropertySymbol
import com.decomposer.runtime.ir.symbols.IrSimpleFunctionSymbol

data class IrPropertyReference(
    val field: IrFieldSymbol?,
    val getter: IrSimpleFunctionSymbol?,
    val setter: IrSimpleFunctionSymbol?,
    override val startOffset: Int,
    override val symbol: IrPropertySymbol,
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
) : IrCallableReference<IrPropertySymbol>()
