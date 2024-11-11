package com.decomposer.runtime.ir

import kotlinx.serialization.Serializable

@Serializable
enum class Modality {
    FINAL, SEALED, OPEN, ABSTRACT
}
