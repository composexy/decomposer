package com.decomposer.runtime.ir.declarations

import com.decomposer.runtime.ir.symbols.IrConstructorSymbol

abstract class IrConstructor : IrFunction() {
    abstract override val symbol: IrConstructorSymbol
    abstract var isPrimary: Boolean
}
