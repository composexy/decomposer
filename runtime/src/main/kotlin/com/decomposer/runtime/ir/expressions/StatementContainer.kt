package com.decomposer.runtime.ir.expressions

import com.decomposer.runtime.ir.Element
import com.decomposer.runtime.ir.Statement

interface StatementContainer : Element {
    val statements: MutableList<Statement>
}
