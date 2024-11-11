package com.decomposer.runtime.ir.declarations

abstract class IrFunctionWithLateBinding : IrSimpleFunction() {
    abstract val isBound: Boolean
}
