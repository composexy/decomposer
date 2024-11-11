package com.decomposer.runtime.ir.expressions

import com.decomposer.runtime.ir.symbols.IrConstructorSymbol

abstract class IrEnumConstructorCall : IrFunctionAccessExpression() {
    abstract override var symbol: IrConstructorSymbol
}
