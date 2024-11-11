package com.decomposer.runtime.ir.declarations

import com.decomposer.runtime.ir.IrType
import com.decomposer.runtime.ir.symbols.IrTypeAliasSymbol

abstract class IrTypeAlias : IrDeclarationBase(), IrDeclarationWithName,
    IrDeclarationWithVisibility, IrTypeParametersContainer, IrMetadataSourceOwner {
    abstract override val symbol: IrTypeAliasSymbol
    abstract var isActual: Boolean
    abstract var expandedType: IrType
}
