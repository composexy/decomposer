package com.decomposer.runtime.ir.expressions

abstract class IrLoop : IrExpression() {
    abstract val origin: IrStatementOrigin?
    abstract val body: IrExpression?
    abstract val condition: IrExpression
    abstract val label: String?
}
