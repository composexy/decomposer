package com.decomposer.runtime.ir.declarations

import com.decomposer.runtime.ir.IrElement

interface IrAttributeContainer : IrElement {
    val attributeOwnerId: IrAttributeContainer
    val originalBeforeInline: IrAttributeContainer?
}
