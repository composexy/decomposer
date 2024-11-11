package com.decomposer.runtime.ir.declarations

import com.decomposer.runtime.ir.IrType
import com.decomposer.runtime.ir.Name
import com.decomposer.runtime.ir.Variance
import com.decomposer.runtime.ir.expressions.IrConstructorCall
import com.decomposer.runtime.ir.symbols.IrTypeParameterSymbol

data class IrTypeParameter(
    override val symbol: IrTypeParameterSymbol,
    val variance: Variance,
    val index: Int,
    val isReified: Boolean,
    val superTypes: List<IrType>,
    override val attributeMap: List<Any?>?,
    override val startOffset: Int,
    override val endOffset: Int,
    override val parent: IrDeclarationParent,
    override val annotations: List<IrConstructorCall>,
    override val name: Name,
) : IrDeclarationBase, IrDeclarationWithName
