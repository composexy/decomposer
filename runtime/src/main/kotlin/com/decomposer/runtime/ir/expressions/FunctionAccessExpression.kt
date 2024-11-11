package com.decomposer.runtime.ir.expressions

import com.decomposer.runtime.ir.symbols.FunctionSymbol
import kotlinx.serialization.Serializable

@Serializable
sealed class FunctionAccessExpression : MemberAccessExpression<FunctionSymbol>()
