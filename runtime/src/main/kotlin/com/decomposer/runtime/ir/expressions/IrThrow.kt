package com.decomposer.runtime.ir.expressions

abstract class IrThrow : IrExpression() {
    abstract var value: IrExpression
}
