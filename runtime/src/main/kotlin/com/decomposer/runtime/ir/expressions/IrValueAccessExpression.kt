package com.decomposer.runtime.ir.expressions

import com.decomposer.runtime.ir.symbols.IrValueSymbol

abstract class IrValueAccessExpression : IrDeclarationReference() {
    abstract override var symbol: IrValueSymbol
    abstract var origin: IrStatementOrigin?
}
