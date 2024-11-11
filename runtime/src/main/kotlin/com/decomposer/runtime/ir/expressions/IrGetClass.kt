package com.decomposer.runtime.ir.expressions

abstract class IrGetClass : IrExpression() {
    abstract var argument: IrExpression
}
