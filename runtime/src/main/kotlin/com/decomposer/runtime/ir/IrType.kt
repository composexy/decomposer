package com.decomposer.runtime.ir

import com.decomposer.runtime.ir.symbols.IrClassSymbol
import com.decomposer.runtime.ir.symbols.IrClassifierSymbol
import com.decomposer.runtime.ir.symbols.IrTypeAliasSymbol

sealed class IrType : IrTypeProjection, IrAnnotationContainer {
    final override val type: IrType
        get() = this
}

abstract class IrErrorType(
    private val errorClassStubSymbol: IrClassSymbol,
    val isMarkedNullable: Boolean = false
) : IrType() {
    val symbol: IrClassSymbol
        get() = errorClassStubSymbol
}

abstract class IrDynamicType : IrType()

enum class SimpleTypeNullability {
    MARKED_NULLABLE,
    NOT_SPECIFIED,
    DEFINITELY_NOT_NULL
}

abstract class IrSimpleType : IrType() {
    abstract val classifier: IrClassifierSymbol
    abstract val nullability: SimpleTypeNullability
    abstract val arguments: List<IrTypeArgument>
    abstract val abbreviation: IrTypeAbbreviation?

    override val variance = Variance.INVARIANT
}

sealed interface IrTypeArgument

interface IrStarProjection : IrTypeArgument

interface IrTypeProjection : IrTypeArgument {
    val variance: Variance
    val type: IrType
}

interface IrTypeAbbreviation : IrAnnotationContainer {
    val typeAlias: IrTypeAliasSymbol
    val hasQuestionMark: Boolean
    val arguments: List<IrTypeArgument>
}
