package com.decomposer.runtime.ir.declarations

import com.decomposer.runtime.ir.IrType
import com.decomposer.runtime.ir.symbols.IrValueSymbol

interface IrValueDeclaration : IrDeclarationWithName, IrSymbolOwner {
    override val symbol: IrValueSymbol
    var type: IrType
}
