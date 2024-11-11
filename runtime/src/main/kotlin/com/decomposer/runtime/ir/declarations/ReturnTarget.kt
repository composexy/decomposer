package com.decomposer.runtime.ir.declarations

import com.decomposer.runtime.ir.symbols.ReturnTargetSymbol

interface ReturnTarget : SymbolOwner {
    override val symbol: ReturnTargetSymbol
}
