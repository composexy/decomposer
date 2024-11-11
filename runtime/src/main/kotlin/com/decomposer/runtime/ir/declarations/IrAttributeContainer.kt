package com.decomposer.runtime.ir.declarations

import com.decomposer.runtime.ir.IrElement

interface IrAttributeContainer : IrElement {
    var attributeOwnerId: IrAttributeContainer
    var originalBeforeInline: IrAttributeContainer?
}
