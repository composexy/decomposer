package com.decomposer.runtime.ir.expressions

import com.decomposer.runtime.ir.symbols.IrFunctionSymbol

abstract class IrRawFunctionReference : IrDeclarationReference() {
    abstract override var symbol: IrFunctionSymbol
}
