package com.decomposer.runtime.ir.declarations

import kotlinx.serialization.Serializable

@Serializable
data class FileEntry(
    val name: String,
    val maxOffset: Int
)
