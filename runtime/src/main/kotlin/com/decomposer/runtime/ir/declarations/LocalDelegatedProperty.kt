package com.decomposer.runtime.ir.declarations

import com.decomposer.runtime.ir.Type
import com.decomposer.runtime.ir.Name
import com.decomposer.runtime.ir.expressions.ConstructorCall
import com.decomposer.runtime.ir.symbols.LocalDelegatedPropertySymbol

data class LocalDelegatedProperty(
    override val symbol: LocalDelegatedPropertySymbol,
    val type: Type,
    val isVar: Boolean,
    val delegate: Variable,
    val getter: SimpleFunction,
    val setter: SimpleFunction?,
    override val attributeMap: List<Any?>?,
    override val startOffset: Int,
    override val endOffset: Int,
    override val parent: DeclarationParent,
    override val annotations: List<ConstructorCall>,
    override val name: Name
) : DeclarationBase, DeclarationWithName, SymbolOwner, MetadataSourceOwner
