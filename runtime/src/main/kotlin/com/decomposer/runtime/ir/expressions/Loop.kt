package com.decomposer.runtime.ir.expressions

import kotlinx.serialization.Serializable

@Serializable
abstract class Loop : Expression() {
    abstract val origin: StatementOrigin?
    abstract val body: Expression?
    abstract val condition: Expression
    abstract val label: String?
}
