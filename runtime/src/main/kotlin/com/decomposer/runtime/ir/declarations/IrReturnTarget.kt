package com.decomposer.runtime.ir.declarations

import com.decomposer.runtime.ir.symbols.IrReturnTargetSymbol

interface IrReturnTarget : IrSymbolOwner {
    override val symbol: IrReturnTargetSymbol
}
