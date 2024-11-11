package com.decomposer.runtime.ir.expressions

import com.decomposer.runtime.ir.symbols.FunctionSymbol

sealed class FunctionAccessExpression : MemberAccessExpression<FunctionSymbol>()
