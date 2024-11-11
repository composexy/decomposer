package com.decomposer.runtime.ir.expressions

import com.decomposer.runtime.ir.symbols.IrSymbol

abstract class IrDeclarationReference : IrExpression() {
    abstract val symbol: IrSymbol
}
