package com.decomposer.runtime.ir.expressions

import com.decomposer.runtime.ir.declarations.IrSimpleFunction

abstract class IrFunctionExpression : IrExpression() {
    abstract var origin: IrStatementOrigin
    abstract var function: IrSimpleFunction
}
