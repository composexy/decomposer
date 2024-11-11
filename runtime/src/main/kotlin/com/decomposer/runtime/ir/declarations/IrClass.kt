package com.decomposer.runtime.ir.declarations

import com.decomposer.runtime.ir.ClassKind
import com.decomposer.runtime.ir.IrType
import com.decomposer.runtime.ir.Modality
import com.decomposer.runtime.ir.symbols.IrClassSymbol

abstract class IrClass : IrDeclarationBase(), IrPossiblyExternalDeclaration,
    IrDeclarationWithVisibility, IrTypeParametersContainer, IrDeclarationContainer,
    IrAttributeContainer, IrMetadataSourceOwner {
    abstract override val symbol: IrClassSymbol
    abstract var kind: ClassKind
    abstract var modality: Modality
    abstract var isCompanion: Boolean
    abstract var isInner: Boolean
    abstract var isData: Boolean
    abstract var isValue: Boolean
    abstract var isExpect: Boolean
    abstract var isFun: Boolean
    abstract var hasEnumEntries: Boolean
    abstract var superTypes: List<IrType>
    abstract var thisReceiver: IrValueParameter?
    abstract var sealedSubclasses: List<IrClassSymbol>
}
