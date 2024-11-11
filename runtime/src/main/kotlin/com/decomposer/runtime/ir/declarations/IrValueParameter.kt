package com.decomposer.runtime.ir.declarations

import com.decomposer.runtime.ir.IrType
import com.decomposer.runtime.ir.expressions.IrExpressionBody
import com.decomposer.runtime.ir.symbols.IrValueParameterSymbol

abstract class IrValueParameter : IrDeclarationBase(), IrValueDeclaration {
    abstract val isAssignable: Boolean
    abstract override val symbol: IrValueParameterSymbol
    abstract var varargElementType: IrType?
    abstract var isCrossinline: Boolean
    abstract var isNoinline: Boolean
    abstract var isHidden: Boolean
    abstract var defaultValue: IrExpressionBody?
    var index: Int = -1
}
