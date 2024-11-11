package com.decomposer.runtime.ir.expressions

import com.decomposer.runtime.ir.Type
import com.decomposer.runtime.ir.declarations.AttributeContainer
import com.decomposer.runtime.ir.declarations.Variable

data class SuspensionPoint(
    val suspensionPointIdParameter: Variable,
    val result: Expression,
    val resumeResult: Expression,
    override val startOffset: Int,
    override val type: Type,
    override val attributeMap: List<Any?>?,
    override val endOffset: Int,
    override val attributeOwnerId: AttributeContainer,
    override val originalBeforeInline: AttributeContainer?
) : Expression()
