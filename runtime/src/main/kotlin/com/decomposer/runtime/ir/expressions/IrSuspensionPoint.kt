package com.decomposer.runtime.ir.expressions

import com.decomposer.runtime.ir.declarations.IrVariable

abstract class IrSuspensionPoint : IrExpression() {
    abstract var suspensionPointIdParameter: IrVariable
    abstract var result: IrExpression
    abstract var resumeResult: IrExpression
}
