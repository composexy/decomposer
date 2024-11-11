package com.decomposer.runtime.ir.declarations

import com.decomposer.runtime.ir.Type
import com.decomposer.runtime.ir.Name
import com.decomposer.runtime.ir.Variance
import com.decomposer.runtime.ir.expressions.ConstructorCall
import com.decomposer.runtime.ir.symbols.TypeParameterSymbol

data class TypeParameter(
    override val symbol: TypeParameterSymbol,
    val variance: Variance,
    val index: Int,
    val isReified: Boolean,
    val superTypes: List<Type>,
    override val attributeMap: List<Any?>?,
    override val startOffset: Int,
    override val endOffset: Int,
    override val parent: DeclarationParent,
    override val annotations: List<ConstructorCall>,
    override val name: Name,
) : DeclarationBase, DeclarationWithName
