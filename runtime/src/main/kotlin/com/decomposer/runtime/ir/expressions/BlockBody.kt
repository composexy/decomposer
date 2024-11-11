package com.decomposer.runtime.ir.expressions

import com.decomposer.runtime.ir.Statement

data class BlockBody(
    override val attributeMap: List<Any?>?,
    override val startOffset: Int,
    override val endOffset: Int,
    override val statements: MutableList<Statement>
) : Body(), StatementContainer
