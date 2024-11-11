package com.decomposer.runtime.ir.expressions

import com.decomposer.runtime.ir.IrElement
import com.decomposer.runtime.ir.IrElementBase

abstract class IrBranch : IrElementBase(), IrElement {
    abstract var condition: IrExpression
    abstract var result: IrExpression
}
