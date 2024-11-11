package com.decomposer.runtime.ir.expressions

import com.decomposer.runtime.ir.symbols.IrLocalDelegatedPropertySymbol
import com.decomposer.runtime.ir.symbols.IrSimpleFunctionSymbol
import com.decomposer.runtime.ir.symbols.IrVariableSymbol

abstract class IrLocalDelegatedPropertyReference : IrCallableReference<IrLocalDelegatedPropertySymbol>() {
    abstract var delegate: IrVariableSymbol
    abstract var getter: IrSimpleFunctionSymbol
    abstract var setter: IrSimpleFunctionSymbol?
}
