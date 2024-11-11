package com.decomposer.runtime.ir.expressions

abstract class IrConstantArray : IrConstantValue() {
    abstract val elements: MutableList<IrConstantValue>
}
