package com.decomposer.runtime.ir.expressions

import com.decomposer.runtime.ir.IrElement
import com.decomposer.runtime.ir.IrElementBase
import com.decomposer.runtime.ir.declarations.IrVariable

abstract class IrCatch : IrElementBase(), IrElement {
    abstract var catchParameter: IrVariable
    abstract var result: IrExpression
    abstract var origin: IrStatementOrigin?
}
