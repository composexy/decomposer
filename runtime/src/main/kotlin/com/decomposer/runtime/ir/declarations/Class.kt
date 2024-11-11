package com.decomposer.runtime.ir.declarations

import com.decomposer.runtime.ir.ClassKind
import com.decomposer.runtime.ir.Type
import com.decomposer.runtime.ir.Modality
import com.decomposer.runtime.ir.Name
import com.decomposer.runtime.ir.Visibility
import com.decomposer.runtime.ir.expressions.ConstructorCall
import com.decomposer.runtime.ir.symbols.ClassSymbol
import kotlinx.serialization.Serializable

@Serializable
data class Class(
    override val symbol: ClassSymbol,
    val kind: ClassKind,
    val modality: Modality,
    val isCompanion: Boolean,
    val isInner: Boolean,
    val isData: Boolean,
    val isValue: Boolean,
    val isExpect: Boolean,
    val isFun: Boolean,
    val hasEnumEntries: Boolean,
    val superTypes: List<Type>,
    val thisReceiver: ValueParameter?,
    val sealedSubclasses: List<ClassSymbol>,
    override val startOffset: Int,
    override val endOffset: Int,
    override val parent: DeclarationParent,
    override val annotations: List<ConstructorCall>,
    override val isExternal: Boolean,
    override val name: Name,
    override val visibility: Visibility,
    override val typeParameters: List<TypeParameter>,
    override val declarations: MutableList<Declaration>,
    override val attributeOwnerId: AttributeContainer,
    override val originalBeforeInline: AttributeContainer?,
) : DeclarationBase, PossiblyExternalDeclaration,
    DeclarationWithVisibility, TypeParametersContainer, DeclarationContainer,
    AttributeContainer, MetadataSourceOwner