package com.decomposer.runtime.ir.expressions

import com.decomposer.runtime.ir.Element
import com.decomposer.runtime.ir.ElementBase
import com.decomposer.runtime.ir.declarations.Variable
import kotlinx.serialization.Serializable

@Serializable
data class Catch(
    val catchParameter: Variable,
    val result: Expression,
    val origin: StatementOrigin?,
    override val startOffset: Int,
    override val endOffset: Int
) : ElementBase, Element
