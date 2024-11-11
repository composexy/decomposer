package com.decomposer.runtime.ir.expressions

import com.decomposer.runtime.ir.Type
import com.decomposer.runtime.ir.declarations.AttributeContainer

data class Break(
    override val startOffset: Int,
    override val loop: Loop,
    override val label: String?,
    override var type: Type,
    override val attributeMap: List<Any?>?,
    override val endOffset: Int,
    override val attributeOwnerId: AttributeContainer,
    override val originalBeforeInline: AttributeContainer?
) : BreakContinue()
