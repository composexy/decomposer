package com.decomposer.runtime.ir.declarations

import com.decomposer.runtime.ir.Modality
import com.decomposer.runtime.ir.Name
import com.decomposer.runtime.ir.Visibility
import com.decomposer.runtime.ir.expressions.ConstructorCall
import com.decomposer.runtime.ir.symbols.Symbol

data class Property(
    override val symbol: Symbol,
    override val overriddenSymbols: List<Symbol>,
    val isBound: Boolean,
    val isVar: Boolean,
    val isConst: Boolean,
    val isLateinit: Boolean,
    val isDelegated: Boolean,
    val isExpect: Boolean,
    val backingField: Field?,
    val getter: SimpleFunction?,
    val setter: SimpleFunction?,
    override val attributeMap: List<Any?>?,
    override var startOffset: Int,
    override var endOffset: Int,
    override val parent: DeclarationParent,
    override val annotations: List<ConstructorCall>,
    override val isExternal: Boolean,
    override val name: Name,
    override val isFakeOverride: Boolean,
    override val modality: Modality,
    override val visibility: Visibility,
    override val attributeOwnerId: AttributeContainer,
    override val originalBeforeInline: AttributeContainer?
) : DeclarationBase, PossiblyExternalDeclaration,
    OverridableDeclaration<Symbol>,
    MetadataSourceOwner,
    AttributeContainer, MemberWithContainerSource
