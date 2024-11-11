package com.decomposer.runtime.ir.expressions

abstract class IrErrorExpression : IrExpression() {
    abstract var description: String
}
