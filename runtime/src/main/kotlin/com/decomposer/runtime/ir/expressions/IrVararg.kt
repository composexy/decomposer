package com.decomposer.runtime.ir.expressions

import com.decomposer.runtime.ir.IrType

abstract class IrVararg : IrExpression() {
    abstract var varargElementType: IrType
    abstract val elements: MutableList<IrVarargElement>
}
