package com.decomposer.runtime.ir

import kotlinx.serialization.Serializable

@Serializable
data class Name(
    val name: String,
    val special: Boolean
)
