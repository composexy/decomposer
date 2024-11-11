package com.decomposer.runtime.ir.declarations

import com.decomposer.runtime.ir.Element
import com.decomposer.runtime.ir.symbols.Symbol

interface SymbolOwner : Element {
    val symbol: Symbol
}
