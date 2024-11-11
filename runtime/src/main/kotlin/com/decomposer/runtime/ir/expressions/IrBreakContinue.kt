package com.decomposer.runtime.ir.expressions

abstract class IrBreakContinue : IrExpression() {
    abstract val loop: IrLoop
    abstract val label: String?
}
