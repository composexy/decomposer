package com.decomposer.runtime.ir.expressions

import com.decomposer.runtime.ir.symbols.IrFieldSymbol
import com.decomposer.runtime.ir.symbols.IrPropertySymbol
import com.decomposer.runtime.ir.symbols.IrSimpleFunctionSymbol

abstract class IrPropertyReference : IrCallableReference<IrPropertySymbol>() {
    abstract var field: IrFieldSymbol?
    abstract var getter: IrSimpleFunctionSymbol?
    abstract var setter: IrSimpleFunctionSymbol?
}
