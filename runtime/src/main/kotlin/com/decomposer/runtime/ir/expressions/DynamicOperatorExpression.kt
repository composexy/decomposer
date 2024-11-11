package com.decomposer.runtime.ir.expressions

import com.decomposer.runtime.ir.Type
import com.decomposer.runtime.ir.declarations.AttributeContainer

data class DynamicOperatorExpression(
    val operator: DynamicOperator,
    val receiver: Expression,
    val arguments: MutableList<Expression>,
    override val startOffset: Int,
    override val type: Type,
    override val attributeMap: List<Any?>?,
    override val endOffset: Int,
    override val attributeOwnerId: AttributeContainer,
    override val originalBeforeInline: AttributeContainer?
) : DynamicExpression()
