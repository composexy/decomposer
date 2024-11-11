package com.decomposer.runtime.ir.expressions

import com.decomposer.runtime.ir.Statement
import com.decomposer.runtime.ir.Type
import com.decomposer.runtime.ir.declarations.AttributeContainer
import kotlinx.serialization.Serializable

@Serializable
data class Composite(
    override val startOffset: Int,
    override var origin: StatementOrigin?,
    override val type: Type,
    override val endOffset: Int,
    override val attributeOwnerId: AttributeContainer,
    override val originalBeforeInline: AttributeContainer?,
    override val statements: MutableList<Statement>
) : ContainerExpression()
