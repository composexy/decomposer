package com.decomposer.runtime.ir.expressions

import com.decomposer.runtime.ir.ElementBase
import com.decomposer.runtime.ir.Statement
import com.decomposer.runtime.ir.Type
import com.decomposer.runtime.ir.declarations.AttributeContainer
import kotlinx.serialization.Serializable

@Serializable
abstract class Expression : ElementBase, Statement, VarargElement, AttributeContainer {
    abstract val type: Type
}
