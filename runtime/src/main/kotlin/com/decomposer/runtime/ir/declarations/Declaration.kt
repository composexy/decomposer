package com.decomposer.runtime.ir.declarations

import com.decomposer.runtime.ir.Statement

interface Declaration : Statement, SymbolOwner, MutableAnnotationContainer {
    val parent: DeclarationParent
}
