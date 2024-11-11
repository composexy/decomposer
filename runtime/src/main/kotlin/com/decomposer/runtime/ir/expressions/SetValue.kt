package com.decomposer.runtime.ir.expressions

import com.decomposer.runtime.ir.Type
import com.decomposer.runtime.ir.declarations.AttributeContainer
import com.decomposer.runtime.ir.symbols.ValueSymbol

data class SetValue(
    val value: Expression,
    override val startOffset: Int,
    override var symbol: ValueSymbol,
    override var origin: StatementOrigin?,
    override val type: Type,
    override val attributeMap: List<Any?>?,
    override val endOffset: Int,
    override val attributeOwnerId: AttributeContainer,
    override val originalBeforeInline: AttributeContainer?
) : ValueAccessExpression()
