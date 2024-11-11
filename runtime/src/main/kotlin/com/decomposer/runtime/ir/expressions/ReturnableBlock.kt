package com.decomposer.runtime.ir.expressions

import com.decomposer.runtime.ir.Statement
import com.decomposer.runtime.ir.Type
import com.decomposer.runtime.ir.declarations.AttributeContainer
import com.decomposer.runtime.ir.declarations.ReturnTarget
import com.decomposer.runtime.ir.declarations.SymbolOwner
import com.decomposer.runtime.ir.symbols.ReturnableBlockSymbol

data class ReturnableBlock(
    override val symbol: ReturnableBlockSymbol,
    override val attributeMap: List<Any?>?,
    override val origin: StatementOrigin?,
    override val type: Type,
    override val startOffset: Int,
    override val endOffset: Int,
    override val attributeOwnerId: AttributeContainer,
    override val originalBeforeInline: AttributeContainer?,
    override val statements: MutableList<Statement>
) : ContainerExpression(), SymbolOwner, ReturnTarget
