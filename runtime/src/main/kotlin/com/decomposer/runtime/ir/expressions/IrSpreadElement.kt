package com.decomposer.runtime.ir.expressions

import com.decomposer.runtime.ir.IrElementBase

abstract class IrSpreadElement : IrElementBase(), IrVarargElement {
    abstract var expression: IrExpression
}
