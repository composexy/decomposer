package com.decomposer.runtime.ir.declarations

import com.decomposer.runtime.ir.Name
import kotlinx.serialization.Serializable

@Serializable
data class Module(
     val name: Name
)
