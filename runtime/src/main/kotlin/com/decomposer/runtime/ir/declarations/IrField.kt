package com.decomposer.runtime.ir.declarations

import com.decomposer.runtime.ir.expressions.IrExpressionBody
import com.decomposer.runtime.ir.IrType
import com.decomposer.runtime.ir.symbols.IrFieldSymbol
import com.decomposer.runtime.ir.symbols.IrPropertySymbol

abstract class IrField : IrDeclarationBase(), IrPossiblyExternalDeclaration,
    IrDeclarationWithVisibility,
    IrDeclarationParent, IrMetadataSourceOwner {
    abstract override val symbol: IrFieldSymbol
    abstract var type: IrType
    abstract var isFinal: Boolean
    abstract var isStatic: Boolean
    abstract var initializer: IrExpressionBody?
    abstract var correspondingPropertySymbol: IrPropertySymbol?
}
