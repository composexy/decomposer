package com.decomposer.runtime.ir.expressions

abstract class IrBreakContinue : IrExpression() {
    abstract var loop: IrLoop
    abstract var label: String?
}
