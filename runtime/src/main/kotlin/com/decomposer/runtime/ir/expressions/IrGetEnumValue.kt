package com.decomposer.runtime.ir.expressions

import com.decomposer.runtime.ir.symbols.IrEnumEntrySymbol

abstract class IrGetEnumValue : IrGetSingletonValue() {
    abstract override var symbol: IrEnumEntrySymbol
}
