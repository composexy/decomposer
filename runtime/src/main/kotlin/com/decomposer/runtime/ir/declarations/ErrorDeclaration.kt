package com.decomposer.runtime.ir.declarations

import com.decomposer.runtime.ir.expressions.ConstructorCall
import com.decomposer.runtime.ir.symbols.Symbol

data class ErrorDeclaration(
    override val annotations: List<ConstructorCall>,
    override val attributeMap: List<Any?>?,
    override val startOffset: Int,
    override val endOffset: Int,
    override val parent: DeclarationParent,
    override val symbol: Symbol
) : DeclarationBase
