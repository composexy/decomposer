package com.decomposer.runtime.ir.expressions

abstract class IrErrorCallExpression : IrErrorExpression() {
    abstract var explicitReceiver: IrExpression?
    abstract val arguments: MutableList<IrExpression>
}
