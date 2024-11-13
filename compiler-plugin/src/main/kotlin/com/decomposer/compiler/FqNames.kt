package com.decomposer.compiler

import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName

const val decomposerRuntimePackage = "com.decomposer.runtime"

val FQ_COMPOSE_IR: FqName = FqName("$decomposerRuntimePackage.ComposeIr")
val CLASS_ID_COMPOSE_IR: ClassId = ClassId.topLevel(FQ_COMPOSE_IR)
