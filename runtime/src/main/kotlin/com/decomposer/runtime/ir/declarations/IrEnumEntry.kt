package com.decomposer.runtime.ir.declarations

import com.decomposer.runtime.ir.expressions.IrExpressionBody
import com.decomposer.runtime.ir.symbols.IrEnumEntrySymbol

abstract class IrEnumEntry : IrDeclarationBase(), IrDeclarationWithName {
    abstract override val symbol: IrEnumEntrySymbol
    abstract var initializerExpression: IrExpressionBody?
    abstract var correspondingClass: IrClass?
}
