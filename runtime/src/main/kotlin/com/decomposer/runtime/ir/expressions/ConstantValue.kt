package com.decomposer.runtime.ir.expressions

import kotlinx.serialization.Serializable

@Serializable
sealed class ConstantValue : Expression()
