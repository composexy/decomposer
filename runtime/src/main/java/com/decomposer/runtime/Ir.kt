package com.decomposer.runtime

interface Element {
    val startOffset: Int
    val endOffset: Int
    val lineNumber: Int
    val columnNumber: Int
    val packageName: String
    val moduleName: String
}

interface Declaration {
    val parent: Element
}

data class File(
    val name: String,
    val declarations: List<Declaration>
)

data class Class(
    val modifiers: Modifiers,
    val superTypes: List<Type>,
    val typeParameters: List<TypeParameter>,
    val thisReceiver: ValueParameter?
)

enum class Visibility {
    PRIVATE, PRIVATE_TO_THIS, PROTECTED, INTERNAL, PUBLIC, LOCAL, INHERITED, INVISIBLE_FAKE, UNKNOWN
}

enum class Modality {
    FINAL, SEALED, OPEN, ABSTRACT
}

enum class ClassKind {
    CLASS, INTERFACE, ENUM_CLASS, ENUM_ENTRY, ANNOTATION_CLASS, OBJECT
}

sealed interface Type {
    val name: String
}

data class SimpleType(
    override val name: String,
    val nullability: Nullability,
    val arguments: List<TypeArgument>,
) : Type

data class ErrorType(
    override val name: String
) : Type

data class DynamicType(
    override val name: String
) : Type

sealed interface TypeArgument

interface StarProjection : TypeArgument

interface TypeProjection : TypeArgument {
    val variance: Variance
    val type: Type
}

enum class Variance {
    INVARIANT, IN_VARIANCE, OUT_VARIANCE
}

enum class Nullability {
    MARKED_NULLABLE, NOT_SPECIFIED, DEFINITELY_NOT_NULL
}

interface AnnotationContainer {
    val annotations: List<ConstructorCall>
}

class ConstructorCall(
    val parentClassName: String,

)

data class Modifiers(
    val visibility: Visibility = Visibility.UNKNOWN,
    val isExpect: Boolean = false,
    val modality: Modality? = null,
    val isExternal: Boolean = false,
    val isOverride: Boolean = false,
    val isFakeOverride: Boolean = false,
    val isLateinit: Boolean = false,
    val isTailrec: Boolean = false,
    val isSuspend: Boolean = false,
    val isInner: Boolean = false,
    val isInline: Boolean = false,
    val isValue: Boolean = false,
    val isData: Boolean = false,
    val isCompanion: Boolean = false,
    val isFunInterface: Boolean = false,
    val classKind: ClassKind? = null,
    val isInfix: Boolean = false,
    val isOperator: Boolean = false,
    val isVararg: Boolean = false,
    val isCrossinline: Boolean = false,
    val isNoinline: Boolean = false,
    val isHidden: Boolean = false,
    val isAssignable: Boolean = false,
)