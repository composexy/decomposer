package com.decomposer.runtime.ir.declarations

import com.decomposer.runtime.ir.expressions.IrConstructorCall
import com.decomposer.runtime.ir.symbols.IrSymbol

data class IrErrorDeclaration(
    override val annotations: List<IrConstructorCall>,
    override val attributeMap: List<Any?>?,
    override val startOffset: Int,
    override val endOffset: Int,
    override val parent: IrDeclarationParent,
    override val symbol: IrSymbol
) : IrDeclarationBase
