package com.decomposer.runtime.ir.expressions

abstract class IrExpressionBody : IrBody() {
    abstract var expression: IrExpression
}
