package com.decomposer.runtime.ir.declarations

import com.decomposer.runtime.ir.IrType
import com.decomposer.runtime.ir.expressions.IrBody
import com.decomposer.runtime.ir.symbols.IrFunctionSymbol

sealed class IrFunction : IrDeclarationBase, IrPossiblyExternalDeclaration,
    IrDeclarationWithVisibility, IrTypeParametersContainer, IrSymbolOwner, IrDeclarationParent,
    IrReturnTarget, IrMemberWithContainerSource, IrMetadataSourceOwner {
    abstract override val symbol: IrFunctionSymbol
    abstract val isInline: Boolean
    abstract val isExpect: Boolean
    abstract val returnType: IrType
    abstract val dispatchReceiverParameter: IrValueParameter?
    abstract val extensionReceiverParameter: IrValueParameter?
    val valueParameters: List<IrValueParameter> = emptyList()
    abstract val contextReceiverParametersCount: Int
    abstract val body: IrBody?
}
