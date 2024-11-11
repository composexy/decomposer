package com.decomposer.runtime.ir.expressions

import kotlinx.serialization.Serializable

@Serializable
abstract class BreakContinue : Expression() {
    abstract val loop: Loop
    abstract val label: String?
}
