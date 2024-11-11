package com.decomposer.runtime.ir.expressions

import com.decomposer.runtime.ir.symbols.IrClassSymbol
import com.decomposer.runtime.ir.symbols.IrFieldSymbol

abstract class IrFieldAccessExpression : IrDeclarationReference() {
    abstract override var symbol: IrFieldSymbol
    abstract var superQualifierSymbol: IrClassSymbol?
    abstract var receiver: IrExpression?
    abstract var origin: IrStatementOrigin?
}
