package com.decomposer.runtime.ir.declarations

import com.decomposer.runtime.ir.IrType
import com.decomposer.runtime.ir.Name
import com.decomposer.runtime.ir.expressions.IrConstructorCall
import com.decomposer.runtime.ir.expressions.IrExpression
import com.decomposer.runtime.ir.symbols.IrVariableSymbol

data class IrVariable(
    override val symbol: IrVariableSymbol,
    val isVar: Boolean,
    val isConst: Boolean,
    val isLateinit: Boolean,
    val initializer: IrExpression?,
    override val attributeMap: List<Any?>?,
    override val startOffset: Int,
    override val endOffset: Int,
    override val parent: IrDeclarationParent,
    override val annotations: List<IrConstructorCall>,
    override val type: IrType,
    override val name: Name
) : IrDeclarationBase, IrValueDeclaration
