package com.decomposer.runtime.ir.expressions

import kotlinx.serialization.Serializable

@Serializable
enum class ConstKind {
    NULL, BOOLEAN, CHAR, BYTE, SHORT, INT, LONG, STRING, FLOAT, DOUBLE
}
