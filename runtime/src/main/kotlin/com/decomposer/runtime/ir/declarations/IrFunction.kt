package com.decomposer.runtime.ir.declarations

import com.decomposer.runtime.ir.IrType
import com.decomposer.runtime.ir.expressions.IrBody
import com.decomposer.runtime.ir.symbols.IrFunctionSymbol

sealed class IrFunction : IrDeclarationBase(), IrPossiblyExternalDeclaration, IrDeclarationWithVisibility, IrTypeParametersContainer,
    IrSymbolOwner, IrDeclarationParent, IrReturnTarget, IrMemberWithContainerSource, IrMetadataSourceOwner {

    abstract override val symbol: IrFunctionSymbol
    abstract var isInline: Boolean
    abstract var isExpect: Boolean
    abstract var returnType: IrType
    abstract var dispatchReceiverParameter: IrValueParameter?
    abstract var extensionReceiverParameter: IrValueParameter?
    var valueParameters: List<IrValueParameter> = emptyList()
    abstract var contextReceiverParametersCount: Int
    abstract var body: IrBody?
}
