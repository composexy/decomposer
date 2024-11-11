package com.decomposer.runtime.ir.expressions

import com.decomposer.runtime.ir.Type
import com.decomposer.runtime.ir.declarations.AttributeContainer
import com.decomposer.runtime.ir.symbols.ClassSymbol
import com.decomposer.runtime.ir.symbols.FieldSymbol

data class SetField(
    val value: Expression,
    override val startOffset: Int,
    override val symbol: FieldSymbol,
    override val superQualifierSymbol: ClassSymbol?,
    override val receiver: Expression?,
    override val origin: StatementOrigin?,
    override val type: Type,
    override val attributeMap: List<Any?>?,
    override val endOffset: Int,
    override val attributeOwnerId: AttributeContainer,
    override val originalBeforeInline: AttributeContainer?
) : FieldAccessExpression()
