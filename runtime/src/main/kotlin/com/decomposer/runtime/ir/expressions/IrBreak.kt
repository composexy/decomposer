package com.decomposer.runtime.ir.expressions

import com.decomposer.runtime.ir.IrType
import com.decomposer.runtime.ir.declarations.IrAttributeContainer

data class IrBreak(
    override val startOffset: Int,
    override val loop: IrLoop,
    override val label: String?,
    override var type: IrType,
    override val attributeMap: List<Any?>?,
    override val endOffset: Int,
    override val attributeOwnerId: IrAttributeContainer,
    override val originalBeforeInline: IrAttributeContainer?
) : IrBreakContinue()
