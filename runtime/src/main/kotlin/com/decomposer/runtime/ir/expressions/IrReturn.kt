package com.decomposer.runtime.ir.expressions

import com.decomposer.runtime.ir.symbols.IrReturnTargetSymbol

abstract class IrReturn : IrExpression() {
    abstract var value: IrExpression
    abstract var returnTargetSymbol: IrReturnTargetSymbol
}
