package com.decomposer.runtime.ir.expressions

import com.decomposer.runtime.ir.symbols.IrClassSymbol
import com.decomposer.runtime.ir.symbols.IrSimpleFunctionSymbol

abstract class IrCall : IrFunctionAccessExpression() {
    abstract override var symbol: IrSimpleFunctionSymbol
    abstract var superQualifierSymbol: IrClassSymbol?
}
