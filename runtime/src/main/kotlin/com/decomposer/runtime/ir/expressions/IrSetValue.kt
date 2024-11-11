package com.decomposer.runtime.ir.expressions

abstract class IrSetValue : IrValueAccessExpression() {
    abstract var value: IrExpression
}
