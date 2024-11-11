package com.decomposer.runtime.ir.expressions

abstract class IrLoop : IrExpression() {
    abstract var origin: IrStatementOrigin?
    abstract var body: IrExpression?
    abstract var condition: IrExpression
    abstract var label: String?
}
