package com.decomposer.runtime.ir.expressions

import com.decomposer.runtime.ir.Statement
import kotlinx.serialization.Serializable

@Serializable
data class BlockBody(
    override val startOffset: Int,
    override val endOffset: Int,
    override val statements: MutableList<Statement>
) : Body(), StatementContainer
