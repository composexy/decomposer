package com.decomposer.runtime.ir.declarations

import com.decomposer.runtime.ir.symbols.IrSymbol

abstract class IrPropertyWithLateBinding : IrProperty() {
    abstract val isBound: Boolean
    abstract fun acquireSymbol(symbol: IrSymbol): IrPropertyWithLateBinding
}
