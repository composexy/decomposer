package com.decomposer.runtime.ir.declarations

import com.decomposer.runtime.ir.ClassKind
import com.decomposer.runtime.ir.IrType
import com.decomposer.runtime.ir.Modality
import com.decomposer.runtime.ir.Name
import com.decomposer.runtime.ir.Visibility
import com.decomposer.runtime.ir.expressions.IrConstructorCall
import com.decomposer.runtime.ir.symbols.IrClassSymbol

data class IrClass(
    override val symbol: IrClassSymbol,
    val kind: ClassKind,
    val modality: Modality,
    val isCompanion: Boolean,
    val isInner: Boolean,
    val isData: Boolean,
    val isValue: Boolean,
    val isExpect: Boolean,
    val isFun: Boolean,
    val hasEnumEntries: Boolean,
    val superTypes: List<IrType>,
    val thisReceiver: IrValueParameter?,
    val sealedSubclasses: List<IrClassSymbol>,
    override val attributeMap: List<Any?>?,
    override val startOffset: Int,
    override val endOffset: Int,
    override val parent: IrDeclarationParent,
    override val annotations: List<IrConstructorCall>,
    override val isExternal: Boolean,
    override val name: Name,
    override val visibility: Visibility,
    override val typeParameters: List<IrTypeParameter>,
    override val declarations: MutableList<IrDeclaration>,
    override val attributeOwnerId: IrAttributeContainer,
    override val originalBeforeInline: IrAttributeContainer?,
) : IrDeclarationBase, IrPossiblyExternalDeclaration,
    IrDeclarationWithVisibility, IrTypeParametersContainer, IrDeclarationContainer,
    IrAttributeContainer, IrMetadataSourceOwner