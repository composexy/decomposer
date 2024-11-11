package com.decomposer.runtime.ir.declarations

import com.decomposer.runtime.ir.expressions.ConstructorCall
import com.decomposer.runtime.ir.symbols.Symbol
import kotlinx.serialization.Serializable

@Serializable
data class ErrorDeclaration(
    override val annotations: List<ConstructorCall>,
    override val startOffset: Int,
    override val endOffset: Int,
    override val parent: DeclarationParent,
    override val symbol: Symbol
) : DeclarationBase
