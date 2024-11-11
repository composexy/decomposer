package com.decomposer.runtime.ir.declarations

import com.decomposer.runtime.ir.expressions.IrExpressionBody
import com.decomposer.runtime.ir.IrType
import com.decomposer.runtime.ir.Name
import com.decomposer.runtime.ir.Visibility
import com.decomposer.runtime.ir.expressions.IrConstructorCall
import com.decomposer.runtime.ir.symbols.IrFieldSymbol
import com.decomposer.runtime.ir.symbols.IrPropertySymbol

data class IrField(
    override val symbol: IrFieldSymbol,
    val type: IrType,
    val isFinal: Boolean,
    val isStatic: Boolean,
    val initializer: IrExpressionBody?,
    val correspondingPropertySymbol: IrPropertySymbol?,
    override val attributeMap: List<Any?>?,
    override val startOffset: Int,
    override val endOffset: Int,
    override val parent: IrDeclarationParent,
    override val annotations: List<IrConstructorCall>,
    override val isExternal: Boolean,
    override val name: Name,
    override val visibility: Visibility
) : IrDeclarationBase, IrPossiblyExternalDeclaration,
    IrDeclarationWithVisibility,
    IrDeclarationParent, IrMetadataSourceOwner
