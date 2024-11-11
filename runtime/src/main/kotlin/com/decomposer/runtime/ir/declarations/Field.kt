package com.decomposer.runtime.ir.declarations

import com.decomposer.runtime.ir.expressions.ExpressionBody
import com.decomposer.runtime.ir.Type
import com.decomposer.runtime.ir.Name
import com.decomposer.runtime.ir.Visibility
import com.decomposer.runtime.ir.expressions.ConstructorCall
import com.decomposer.runtime.ir.symbols.FieldSymbol
import com.decomposer.runtime.ir.symbols.PropertySymbol
import kotlinx.serialization.Serializable

@Serializable
data class Field(
    override val symbol: FieldSymbol,
    val type: Type,
    val isFinal: Boolean,
    val isStatic: Boolean,
    val initializer: ExpressionBody?,
    val correspondingPropertySymbol: PropertySymbol?,
    override val startOffset: Int,
    override val endOffset: Int,
    override val parent: DeclarationParent,
    override val annotations: List<ConstructorCall>,
    override val isExternal: Boolean,
    override val name: Name,
    override val visibility: Visibility
) : DeclarationBase, PossiblyExternalDeclaration,
    DeclarationWithVisibility,
    DeclarationParent, MetadataSourceOwner
