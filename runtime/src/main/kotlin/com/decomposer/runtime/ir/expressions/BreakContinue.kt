package com.decomposer.runtime.ir.expressions

abstract class BreakContinue : Expression() {
    abstract val loop: Loop
    abstract val label: String?
}
