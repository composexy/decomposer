package com.decomposer.runtime.ir.expressions

import com.decomposer.runtime.ir.Type
import com.decomposer.runtime.ir.declarations.AttributeContainer

data class ErrorCallExpression(
    val explicitReceiver: Expression?,
    val arguments: MutableList<Expression>,
    override val startOffset: Int,
    val description: String,
    override val type: Type,
    override val attributeMap: List<Any?>?,
    override val endOffset: Int,
    override val attributeOwnerId: AttributeContainer,
    override val originalBeforeInline: AttributeContainer?
) : Expression()
