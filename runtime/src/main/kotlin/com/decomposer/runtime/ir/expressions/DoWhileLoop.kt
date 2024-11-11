package com.decomposer.runtime.ir.expressions

import com.decomposer.runtime.ir.Type
import com.decomposer.runtime.ir.declarations.AttributeContainer

data class DoWhileLoop(
    override val startOffset: Int,
    override var origin: StatementOrigin?,
    override var body: Expression?,
    override var condition: Expression,
    override var label: String?,
    override val type: Type,
    override val attributeMap: List<Any?>?,
    override val endOffset: Int,
    override val attributeOwnerId: AttributeContainer,
    override val originalBeforeInline: AttributeContainer?
) : Loop()
