package com.decomposer.runtime.ir

import kotlinx.serialization.Serializable

@Serializable
enum class ClassKind {
    CLASS, INTERFACE, ENUM_CLASS, ENUM_ENTRY, ANNOTATION_CLASS, OBJECT
}
