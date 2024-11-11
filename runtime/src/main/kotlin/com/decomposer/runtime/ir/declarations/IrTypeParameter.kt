package com.decomposer.runtime.ir.declarations

import com.decomposer.runtime.ir.IrType
import com.decomposer.runtime.ir.Variance
import com.decomposer.runtime.ir.symbols.IrTypeParameterSymbol

abstract class IrTypeParameter : IrDeclarationBase(), IrDeclarationWithName {
    abstract override val symbol: IrTypeParameterSymbol
    abstract var variance: Variance
    abstract var index: Int
    abstract var isReified: Boolean
    abstract var superTypes: List<IrType>
}
