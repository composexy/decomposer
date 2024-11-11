package com.decomposer.runtime.ir.expressions

import com.decomposer.runtime.ir.symbols.IrClassSymbol

abstract class IrGetObjectValue : IrGetSingletonValue() {
    abstract override var symbol: IrClassSymbol
}
