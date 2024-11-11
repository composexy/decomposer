package com.decomposer.runtime.ir.expressions

abstract class IrContainerExpression : IrExpression(), IrStatementContainer {
    abstract val origin: IrStatementOrigin?
}
