package com.decomposer.runtime.ir.declarations

import com.decomposer.runtime.ir.symbols.IrPropertySymbol
import com.decomposer.runtime.ir.symbols.IrSimpleFunctionSymbol

abstract class IrSimpleFunction : IrFunction(), IrOverridableDeclaration<IrSimpleFunctionSymbol>,
    IrAttributeContainer {
    abstract override val symbol: IrSimpleFunctionSymbol
    abstract override var overriddenSymbols: List<IrSimpleFunctionSymbol>
    abstract var isTailrec: Boolean
    abstract var isSuspend: Boolean
    abstract var isOperator: Boolean
    abstract var isInfix: Boolean
    abstract var correspondingPropertySymbol: IrPropertySymbol?
}
