package com.decomposer.runtime.ir.declarations

import com.decomposer.runtime.ir.symbols.IrSymbol

abstract class IrProperty : IrDeclarationBase(), IrPossiblyExternalDeclaration,
    IrOverridableDeclaration<IrSymbol>,
    IrMetadataSourceOwner,
    IrAttributeContainer, IrMemberWithContainerSource {
    abstract override val symbol: IrSymbol
    abstract override var overriddenSymbols: List<IrSymbol>
    abstract var isVar: Boolean
    abstract var isConst: Boolean
    abstract var isLateinit: Boolean
    abstract var isDelegated: Boolean
    abstract var isExpect: Boolean
    abstract var backingField: IrField?
    abstract var getter: IrSimpleFunction?
    abstract var setter: IrSimpleFunction?
}
