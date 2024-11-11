package com.decomposer.runtime.ir.expressions

import com.decomposer.runtime.ir.Type
import com.decomposer.runtime.ir.declarations.AttributeContainer

data class TypeOperatorCall(
    val operator: IrTypeOperator,
    val argument: Expression,
    val typeOperand: Type,
    override val startOffset: Int,
    override val type: Type,
    override val attributeMap: List<Any?>?,
    override val endOffset: Int,
    override val attributeOwnerId: AttributeContainer,
    override val originalBeforeInline: AttributeContainer?
) : Expression()

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
