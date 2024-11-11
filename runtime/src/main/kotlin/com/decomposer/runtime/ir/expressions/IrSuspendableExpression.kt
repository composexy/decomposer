package com.decomposer.runtime.ir.expressions

abstract class IrSuspendableExpression : IrExpression() {
    abstract var suspensionPointId: IrExpression
    abstract var result: IrExpression
}
