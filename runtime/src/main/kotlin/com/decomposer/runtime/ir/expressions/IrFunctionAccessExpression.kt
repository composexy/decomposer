package com.decomposer.runtime.ir.expressions

import com.decomposer.runtime.ir.symbols.IrFunctionSymbol

sealed class IrFunctionAccessExpression : IrMemberAccessExpression<IrFunctionSymbol>()
