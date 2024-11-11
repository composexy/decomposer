package com.decomposer.runtime.ir.expressions

abstract class IrContainerExpression : IrExpression(), IrStatementContainer {
    abstract var origin: IrStatementOrigin?
}
