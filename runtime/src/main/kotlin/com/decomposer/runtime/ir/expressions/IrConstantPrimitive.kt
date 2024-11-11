package com.decomposer.runtime.ir.expressions

abstract class IrConstantPrimitive : IrConstantValue() {
    abstract var value: IrConst
}
