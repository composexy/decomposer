package com.decomposer.runtime.ir.expressions

import kotlinx.serialization.Serializable

@Serializable
data class ExpressionBody(
    val expression: Expression,
    override val startOffset: Int,
    override val endOffset: Int
) : Body()
