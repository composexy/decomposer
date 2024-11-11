package com.decomposer.runtime.ir.expressions

abstract class IrSetField : IrFieldAccessExpression() {
    abstract var value: IrExpression
}
