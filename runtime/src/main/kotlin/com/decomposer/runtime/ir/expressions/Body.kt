package com.decomposer.runtime.ir.expressions

import com.decomposer.runtime.ir.Element
import com.decomposer.runtime.ir.ElementBase
import kotlinx.serialization.Serializable

@Serializable
sealed class Body : ElementBase, Element
