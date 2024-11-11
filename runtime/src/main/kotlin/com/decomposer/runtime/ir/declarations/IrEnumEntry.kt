package com.decomposer.runtime.ir.declarations

import com.decomposer.runtime.ir.Name
import com.decomposer.runtime.ir.expressions.IrConstructorCall
import com.decomposer.runtime.ir.expressions.IrExpressionBody
import com.decomposer.runtime.ir.symbols.IrEnumEntrySymbol

data class IrEnumEntry(
    override val symbol: IrEnumEntrySymbol,
    val initializerExpression: IrExpressionBody?,
    val correspondingClass: IrClass?,
    override val attributeMap: List<Any?>?,
    override val startOffset: Int,
    override val endOffset: Int,
    override val parent: IrDeclarationParent,
    override val annotations: List<IrConstructorCall>,
    override val name: Name
) : IrDeclarationBase, IrDeclarationWithName
