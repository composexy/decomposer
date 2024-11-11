package com.decomposer.runtime.ir.declarations

import com.decomposer.runtime.ir.Type
import com.decomposer.runtime.ir.expressions.Body
import com.decomposer.runtime.ir.symbols.FunctionSymbol

sealed class Function : DeclarationBase, PossiblyExternalDeclaration,
    DeclarationWithVisibility, TypeParametersContainer, SymbolOwner, DeclarationParent,
    ReturnTarget, MemberWithContainerSource, MetadataSourceOwner {
    abstract override val symbol: FunctionSymbol
    abstract val isInline: Boolean
    abstract val isExpect: Boolean
    abstract val returnType: Type
    abstract val dispatchReceiverParameter: ValueParameter?
    abstract val extensionReceiverParameter: ValueParameter?
    val valueParameters: List<ValueParameter> = emptyList()
    abstract val contextReceiverParametersCount: Int
    abstract val body: Body?
}
