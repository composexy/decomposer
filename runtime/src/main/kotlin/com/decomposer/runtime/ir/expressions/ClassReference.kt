package com.decomposer.runtime.ir.expressions

import com.decomposer.runtime.ir.Type
import com.decomposer.runtime.ir.declarations.AttributeContainer
import com.decomposer.runtime.ir.symbols.ClassifierSymbol
import kotlinx.serialization.Serializable

@Serializable
data class ClassReference(
    override val symbol: ClassifierSymbol,
    val classType: Type,
    override val startOffset: Int,
    override val type: Type,
    override val endOffset: Int,
    override val attributeOwnerId: AttributeContainer,
    override val originalBeforeInline: AttributeContainer?
) : DeclarationReference()
