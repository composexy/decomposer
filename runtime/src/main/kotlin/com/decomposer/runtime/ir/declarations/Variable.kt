package com.decomposer.runtime.ir.declarations

import com.decomposer.runtime.ir.Type
import com.decomposer.runtime.ir.Name
import com.decomposer.runtime.ir.expressions.ConstructorCall
import com.decomposer.runtime.ir.expressions.Expression
import com.decomposer.runtime.ir.symbols.VariableSymbol
import kotlinx.serialization.Serializable

@Serializable
data class Variable(
    override val symbol: VariableSymbol,
    val isVar: Boolean,
    val isConst: Boolean,
    val isLateinit: Boolean,
    val initializer: Expression?,
    override val startOffset: Int,
    override val endOffset: Int,
    override val parent: DeclarationParent,
    override val annotations: List<ConstructorCall>,
    override val type: Type,
    override val name: Name
) : DeclarationBase, ValueDeclaration
