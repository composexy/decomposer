package com.decomposer.runtime.ir.expressions

import com.decomposer.runtime.ir.IrType
import com.decomposer.runtime.ir.declarations.IrAttributeContainer
import com.decomposer.runtime.ir.symbols.IrConstructorSymbol

data class IrConstantObject(
    val constructor: IrConstructorSymbol,
    val valueArguments: MutableList<IrConstantValue>,
    val typeArguments: MutableList<IrType>,
    override val startOffset: Int,
    override val type: IrType,
    override val attributeMap: List<Any?>?,
    override val endOffset: Int,
    override val attributeOwnerId: IrAttributeContainer,
    override val originalBeforeInline: IrAttributeContainer?
) : IrConstantValue()
