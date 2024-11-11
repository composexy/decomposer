package com.decomposer.runtime.ir.expressions

import com.decomposer.runtime.ir.Type
import com.decomposer.runtime.ir.declarations.AttributeContainer
import kotlinx.serialization.Serializable

@Serializable
data class WhileLoop(
    override val startOffset: Int,
    override val origin: StatementOrigin?,
    override val body: Expression?,
    override val condition: Expression,
    override val label: String?,
    override val type: Type,
    override val endOffset: Int,
    override val attributeOwnerId: AttributeContainer,
    override val originalBeforeInline: AttributeContainer?
) : Loop()
