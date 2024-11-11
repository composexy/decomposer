package com.decomposer.runtime.ir.declarations

import com.decomposer.runtime.ir.IrAnnotationContainer
import com.decomposer.runtime.ir.IrElement
import com.decomposer.runtime.ir.expressions.IrConstructorCall

interface IrMutableAnnotationContainer : IrElement, IrAnnotationContainer {
    override var annotations: List<IrConstructorCall>
}
