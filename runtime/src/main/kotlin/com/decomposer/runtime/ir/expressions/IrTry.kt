package com.decomposer.runtime.ir.expressions

abstract class IrTry : IrExpression() {
    abstract var tryResult: IrExpression
    abstract val catches: MutableList<IrCatch>
    abstract var finallyExpression: IrExpression?
}
