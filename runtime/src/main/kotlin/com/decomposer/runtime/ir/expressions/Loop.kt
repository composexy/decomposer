package com.decomposer.runtime.ir.expressions

abstract class Loop : Expression() {
    abstract val origin: StatementOrigin?
    abstract val body: Expression?
    abstract val condition: Expression
    abstract val label: String?
}
