package com.decomposer.runtime.ir.declarations

import com.decomposer.runtime.ir.expressions.IrExpression
import com.decomposer.runtime.ir.symbols.IrVariableSymbol

abstract class IrVariable : IrDeclarationBase(), IrValueDeclaration {
    abstract override val symbol: IrVariableSymbol
    abstract var isVar: Boolean
    abstract var isConst: Boolean
    abstract var isLateinit: Boolean
    abstract var initializer: IrExpression?
}
