package com.decomposer.runtime.ir.expressions

abstract class IrDynamicOperatorExpression : IrDynamicExpression() {
    abstract var operator: IrDynamicOperator
    abstract var receiver: IrExpression
    abstract val arguments: MutableList<IrExpression>
}
