package com.decomposer.runtime.ir.expressions

import com.decomposer.runtime.ir.symbols.IrFunctionSymbol

abstract class IrFunctionReference : IrCallableReference<IrFunctionSymbol>() {
    abstract var reflectionTarget: IrFunctionSymbol?
}
