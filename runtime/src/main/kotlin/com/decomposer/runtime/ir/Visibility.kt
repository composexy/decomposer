package com.decomposer.runtime.ir

import kotlinx.serialization.Serializable

@Serializable
enum class Visibility {
    PRIVATE,
    PRIVATE_TO_THIS,
    PROTECTED,
    INTERNAL,
    PUBLIC,
    LOCAL,
    INHERITED,
    INVISIBLE_FAKE,
    UNKNOWN
}
