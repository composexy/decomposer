package com.decomposer.runtime.ir.expressions

import com.decomposer.runtime.ir.IrType
import com.decomposer.runtime.ir.symbols.IrSymbol

abstract class IrMemberAccessExpression<S : IrSymbol> : IrDeclarationReference() {
    abstract var dispatchReceiver: IrExpression?
    abstract var extensionReceiver: IrExpression?
    abstract override val symbol: S
    abstract var origin: IrStatementOrigin?
    protected abstract val valueArguments: Array<IrExpression?>

    protected abstract val typeArguments: Array<IrType?>

    internal val targetContextParameterCount: Int get() = -1
    internal val targetHasDispatchReceiver: Boolean get() = false
    internal val targetHasExtensionReceiver: Boolean get() = false

    val valueArgumentsCount: Int
        get() = valueArguments.size

    val typeArgumentsCount: Int
        get() = typeArguments.size
}
