package com.decomposer.runtime.ir.expressions

import com.decomposer.runtime.ir.IrElementBase
import com.decomposer.runtime.ir.IrStatement
import com.decomposer.runtime.ir.IrType
import com.decomposer.runtime.ir.declarations.IrAttributeContainer

abstract class IrExpression : IrElementBase(), IrStatement, IrVarargElement, IrAttributeContainer {
    abstract var type: IrType
}
