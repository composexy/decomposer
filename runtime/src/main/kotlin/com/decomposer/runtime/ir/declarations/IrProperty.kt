package com.decomposer.runtime.ir.declarations

import com.decomposer.runtime.ir.Modality
import com.decomposer.runtime.ir.Name
import com.decomposer.runtime.ir.Visibility
import com.decomposer.runtime.ir.expressions.IrConstructorCall
import com.decomposer.runtime.ir.symbols.IrSymbol

data class IrProperty(
    override val symbol: IrSymbol,
    override val overriddenSymbols: List<IrSymbol>,
    val isBound: Boolean,
    val isVar: Boolean,
    val isConst: Boolean,
    val isLateinit: Boolean,
    val isDelegated: Boolean,
    val isExpect: Boolean,
    val backingField: IrField?,
    val getter: IrSimpleFunction?,
    val setter: IrSimpleFunction?,
    override val attributeMap: List<Any?>?,
    override var startOffset: Int,
    override var endOffset: Int,
    override val parent: IrDeclarationParent,
    override val annotations: List<IrConstructorCall>,
    override val isExternal: Boolean,
    override val name: Name,
    override val isFakeOverride: Boolean,
    override val modality: Modality,
    override val visibility: Visibility,
    override val attributeOwnerId: IrAttributeContainer,
    override val originalBeforeInline: IrAttributeContainer?
) : IrDeclarationBase, IrPossiblyExternalDeclaration,
    IrOverridableDeclaration<IrSymbol>,
    IrMetadataSourceOwner,
    IrAttributeContainer, IrMemberWithContainerSource
