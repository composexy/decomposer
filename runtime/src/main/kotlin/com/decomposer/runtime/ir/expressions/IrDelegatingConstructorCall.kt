package com.decomposer.runtime.ir.expressions

import com.decomposer.runtime.ir.symbols.IrConstructorSymbol

abstract class IrDelegatingConstructorCall : IrFunctionAccessExpression() {
    abstract override var symbol: IrConstructorSymbol
}
