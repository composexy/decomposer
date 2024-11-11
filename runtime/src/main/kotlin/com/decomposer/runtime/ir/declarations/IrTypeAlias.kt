package com.decomposer.runtime.ir.declarations

import com.decomposer.runtime.ir.IrType
import com.decomposer.runtime.ir.Name
import com.decomposer.runtime.ir.Visibility
import com.decomposer.runtime.ir.expressions.IrConstructorCall
import com.decomposer.runtime.ir.symbols.IrTypeAliasSymbol

data class IrTypeAlias(
    override val symbol: IrTypeAliasSymbol,
    val isActual: Boolean,
    val expandedType: IrType,
    override val attributeMap: List<Any?>?,
    override val startOffset: Int,
    override val endOffset: Int,
    override val parent: IrDeclarationParent,
    override val annotations: List<IrConstructorCall>,
    override val name: Name,
    override val visibility: Visibility,
    override val typeParameters: List<IrTypeParameter>
) : IrDeclarationBase, IrDeclarationWithName,
    IrDeclarationWithVisibility, IrTypeParametersContainer, IrMetadataSourceOwner
