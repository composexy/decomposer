package com.decomposer.runtime.ir.expressions

import com.decomposer.runtime.ir.ElementBase

data class SpreadElement(
    val expression: Expression,
    override val attributeMap: List<Any?>?,
    override val startOffset: Int,
    override val endOffset: Int
) : ElementBase, VarargElement
