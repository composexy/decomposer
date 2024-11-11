package com.decomposer.runtime.ir.expressions

import com.decomposer.runtime.ir.Element
import com.decomposer.runtime.ir.ElementBase
import com.decomposer.runtime.ir.declarations.Variable

data class Catch(
    val catchParameter: Variable,
    val result: Expression,
    val origin: StatementOrigin?,
    override val attributeMap: List<Any?>?,
    override val startOffset: Int,
    override val endOffset: Int
) : ElementBase, Element
