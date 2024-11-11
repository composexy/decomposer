package com.decomposer.runtime.ir.expressions

import com.decomposer.runtime.ir.symbols.IrConstructorSymbol

abstract class IrConstructorCall : IrFunctionAccessExpression() {
    abstract override var symbol: IrConstructorSymbol
    abstract var constructorTypeArgumentsCount: Int
}
