package com.decomposer.runtime.ir.declarations

import com.decomposer.runtime.ir.IrElement
import com.decomposer.runtime.ir.symbols.IrSymbol

interface IrSymbolOwner : IrElement {
    val symbol: IrSymbol
}
