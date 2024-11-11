package com.decomposer.runtime.ir.expressions

abstract class IrSyntheticBody : IrBody() {
    abstract var kind: IrSyntheticBodyKind
}

enum class IrSyntheticBodyKind {
    ENUM_VALUES,
    ENUM_VALUEOF,
    ENUM_ENTRIES
}
