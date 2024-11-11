package com.decomposer.runtime.ir.expressions

import com.decomposer.runtime.ir.IrType
import com.decomposer.runtime.ir.symbols.IrSymbol

abstract class IrMemberAccessExpression<S : IrSymbol> : IrDeclarationReference() {
    abstract val dispatchReceiver: IrExpression?
    abstract val extensionReceiver: IrExpression?
    abstract override val symbol: S
    abstract val origin: IrStatementOrigin?
    protected abstract val valueArguments: List<IrExpression?>
    protected abstract val typeArguments: List<IrType?>
    internal val targetContextParameterCount: Int get() = -1
    internal val targetHasDispatchReceiver: Boolean get() = false
    internal val targetHasExtensionReceiver: Boolean get() = false
}
