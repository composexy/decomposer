package com.decomposer.runtime.ir.expressions

import com.decomposer.runtime.ir.symbols.IrClassSymbol
import com.decomposer.runtime.ir.symbols.IrFieldSymbol

abstract class IrFieldAccessExpression : IrDeclarationReference() {
    abstract override val symbol: IrFieldSymbol
    abstract val superQualifierSymbol: IrClassSymbol?
    abstract val receiver: IrExpression?
    abstract val origin: IrStatementOrigin?
}
