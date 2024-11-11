package com.decomposer.runtime.ir.expressions

import com.decomposer.runtime.ir.Element
import com.decomposer.runtime.ir.ElementBase
import kotlinx.serialization.Serializable

@Serializable
data class Branch(
    val condition: Expression,
    val result: Expression,
    override val startOffset: Int,
    override val endOffset: Int
) : ElementBase, Element
