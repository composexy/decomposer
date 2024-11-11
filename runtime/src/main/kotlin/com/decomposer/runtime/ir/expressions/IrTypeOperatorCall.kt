package com.decomposer.runtime.ir.expressions

import com.decomposer.runtime.ir.IrType

abstract class IrTypeOperatorCall : IrExpression() {
    abstract var operator: IrTypeOperator
    abstract var argument: IrExpression
    abstract var typeOperand: IrType
}

enum class IrTypeOperator {
    CAST,
    IMPLICIT_CAST,
    IMPLICIT_NOTNULL,
    IMPLICIT_COERCION_TO_UNIT,
    IMPLICIT_INTEGER_COERCION,
    SAFE_CAST,
    INSTANCEOF,
    NOT_INSTANCEOF,
    SAM_CONVERSION,
    IMPLICIT_DYNAMIC_CAST,
    REINTERPRET_CAST;
}
