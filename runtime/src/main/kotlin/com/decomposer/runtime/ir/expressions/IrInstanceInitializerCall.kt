package com.decomposer.runtime.ir.expressions

import com.decomposer.runtime.ir.symbols.IrClassSymbol

abstract class IrInstanceInitializerCall : IrExpression() {
    abstract var classSymbol: IrClassSymbol
}
