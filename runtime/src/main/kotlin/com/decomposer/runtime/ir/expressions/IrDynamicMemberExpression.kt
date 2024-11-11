package com.decomposer.runtime.ir.expressions

abstract class IrDynamicMemberExpression : IrDynamicExpression() {
    abstract var memberName: String
    abstract var receiver: IrExpression
}
