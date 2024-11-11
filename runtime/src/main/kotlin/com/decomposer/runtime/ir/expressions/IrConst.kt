package com.decomposer.runtime.ir.expressions

abstract class IrConst : IrExpression() {
    abstract var kind: IrConstKind
    abstract var value: Any?
}
