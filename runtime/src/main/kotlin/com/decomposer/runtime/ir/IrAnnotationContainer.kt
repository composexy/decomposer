package com.decomposer.runtime.ir

import com.decomposer.runtime.ir.expressions.IrConstructorCall

interface IrAnnotationContainer {
    val annotations: List<IrConstructorCall>
}
