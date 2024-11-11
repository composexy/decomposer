package com.decomposer.runtime.ir.expressions

abstract class IrStringConcatenation : IrExpression() {
    abstract val arguments: MutableList<IrExpression>
}
