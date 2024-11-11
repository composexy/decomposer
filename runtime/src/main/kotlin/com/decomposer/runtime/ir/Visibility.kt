package com.decomposer.runtime.ir

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
