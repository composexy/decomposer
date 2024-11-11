package com.decomposer.runtime.ir.expressions

import com.decomposer.runtime.ir.IrType
import com.decomposer.runtime.ir.symbols.IrConstructorSymbol

abstract class IrConstantObject : IrConstantValue() {
    abstract var constructor: IrConstructorSymbol
    abstract val valueArguments: MutableList<IrConstantValue>
    abstract val typeArguments: MutableList<IrType>
}
