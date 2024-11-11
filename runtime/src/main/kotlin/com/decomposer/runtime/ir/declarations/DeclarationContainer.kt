package com.decomposer.runtime.ir.declarations

interface DeclarationContainer : DeclarationParent {
    val declarations: MutableList<Declaration>
}
