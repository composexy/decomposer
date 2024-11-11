package com.decomposer.runtime.ir.declarations

import com.decomposer.runtime.ir.IrType
import com.decomposer.runtime.ir.symbols.IrLocalDelegatedPropertySymbol

abstract class IrLocalDelegatedProperty : IrDeclarationBase(), IrDeclarationWithName, IrSymbolOwner,
    IrMetadataSourceOwner {
    abstract override val symbol: IrLocalDelegatedPropertySymbol
    abstract var type: IrType
    abstract var isVar: Boolean
    abstract var delegate: IrVariable
    abstract var getter: IrSimpleFunction
    abstract var setter: IrSimpleFunction?
}
