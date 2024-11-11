package com.decomposer.runtime.ir.expressions

import com.decomposer.runtime.ir.ElementBase
import kotlinx.serialization.Serializable

@Serializable
data class SpreadElement(
    val expression: Expression,
    override val startOffset: Int,
    override val endOffset: Int
) : ElementBase, VarargElement
