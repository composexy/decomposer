package com.decomposer.runtime.ir.expressions

import com.decomposer.runtime.ir.Statement
import com.decomposer.runtime.ir.Type
import com.decomposer.runtime.ir.declarations.AttributeContainer
import com.decomposer.runtime.ir.declarations.FileEntry
import com.decomposer.runtime.ir.symbols.FunctionSymbol

data class InlinedFunctionBlock(
    val inlineFunctionSymbol: FunctionSymbol?,
    val fileEntry: FileEntry,
    override val startOffset: Int,
    override var origin: StatementOrigin?,
    override var type: Type,
    override val attributeMap: List<Any?>?,
    override val endOffset: Int,
    override val attributeOwnerId: AttributeContainer,
    override val originalBeforeInline: AttributeContainer?,
    override val statements: MutableList<Statement>
) : ContainerExpression()
