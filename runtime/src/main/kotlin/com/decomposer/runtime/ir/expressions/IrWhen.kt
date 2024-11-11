package com.decomposer.runtime.ir.expressions

abstract class IrWhen : IrExpression() {
    abstract var origin: IrStatementOrigin?
    abstract val branches: MutableList<IrBranch>
}
