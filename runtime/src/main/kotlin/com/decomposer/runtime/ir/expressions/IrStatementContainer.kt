package com.decomposer.runtime.ir.expressions

import com.decomposer.runtime.ir.IrElement
import com.decomposer.runtime.ir.IrStatement

interface IrStatementContainer : IrElement {
    val statements: MutableList<IrStatement>
}
