package com.decomposer.runtime.ir.declarations

import com.decomposer.runtime.ir.IrType
import com.decomposer.runtime.ir.Name
import com.decomposer.runtime.ir.expressions.IrConstructorCall
import com.decomposer.runtime.ir.symbols.IrLocalDelegatedPropertySymbol

data class IrLocalDelegatedProperty(
    override val symbol: IrLocalDelegatedPropertySymbol,
    val type: IrType,
    val isVar: Boolean,
    val delegate: IrVariable,
    val getter: IrSimpleFunction,
    val setter: IrSimpleFunction?,
    override val attributeMap: List<Any?>?,
    override val startOffset: Int,
    override val endOffset: Int,
    override val parent: IrDeclarationParent,
    override val annotations: List<IrConstructorCall>,
    override val name: Name
) : IrDeclarationBase, IrDeclarationWithName, IrSymbolOwner, IrMetadataSourceOwner
