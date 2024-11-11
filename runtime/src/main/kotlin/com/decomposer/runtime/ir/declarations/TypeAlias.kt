package com.decomposer.runtime.ir.declarations

import com.decomposer.runtime.ir.Type
import com.decomposer.runtime.ir.Name
import com.decomposer.runtime.ir.Visibility
import com.decomposer.runtime.ir.expressions.ConstructorCall
import com.decomposer.runtime.ir.symbols.TypeAliasSymbol
import kotlinx.serialization.Serializable

@Serializable
data class TypeAlias(
    override val symbol: TypeAliasSymbol,
    val isActual: Boolean,
    val expandedType: Type,
    override val startOffset: Int,
    override val endOffset: Int,
    override val parent: DeclarationParent,
    override val annotations: List<ConstructorCall>,
    override val name: Name,
    override val visibility: Visibility,
    override val typeParameters: List<TypeParameter>
) : DeclarationBase, DeclarationWithName,
    DeclarationWithVisibility, TypeParametersContainer, MetadataSourceOwner
