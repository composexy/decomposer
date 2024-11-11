package com.decomposer.runtime.ir.declarations

import com.decomposer.runtime.ir.Name
import com.decomposer.runtime.ir.expressions.ConstructorCall
import com.decomposer.runtime.ir.expressions.ExpressionBody
import com.decomposer.runtime.ir.symbols.EnumEntrySymbol

data class EnumEntry(
    override val symbol: EnumEntrySymbol,
    val initializerExpression: ExpressionBody?,
    val correspondingClass: Class?,
    override val attributeMap: List<Any?>?,
    override val startOffset: Int,
    override val endOffset: Int,
    override val parent: DeclarationParent,
    override val annotations: List<ConstructorCall>,
    override val name: Name
) : DeclarationBase, DeclarationWithName
