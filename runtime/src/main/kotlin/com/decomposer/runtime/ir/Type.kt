package com.decomposer.runtime.ir

import com.decomposer.runtime.ir.symbols.ClassSymbol
import com.decomposer.runtime.ir.symbols.ClassifierSymbol
import com.decomposer.runtime.ir.symbols.TypeAliasSymbol

sealed class Type : IrTypeProjection, AnnotationContainer {
    final override val type: Type
        get() = this
}

abstract class ErrorType(
    private val errorClassStubSymbol: ClassSymbol,
    val isMarkedNullable: Boolean = false
) : Type() {
    val symbol: ClassSymbol
        get() = errorClassStubSymbol
}

abstract class DynamicType : Type()

enum class SimpleTypeNullability {
    MARKED_NULLABLE,
    NOT_SPECIFIED,
    DEFINITELY_NOT_NULL
}

abstract class SimpleType : Type() {
    abstract val classifier: ClassifierSymbol
    abstract val nullability: SimpleTypeNullability
    abstract val arguments: List<IrTypeArgument>
    abstract val abbreviation: TypeAbbreviation?

    override val variance = Variance.INVARIANT
}

sealed interface IrTypeArgument

interface IrStarProjection : IrTypeArgument

interface IrTypeProjection : IrTypeArgument {
    val variance: Variance
    val type: Type
}

interface TypeAbbreviation : AnnotationContainer {
    val typeAlias: TypeAliasSymbol
    val hasQuestionMark: Boolean
    val arguments: List<IrTypeArgument>
}
