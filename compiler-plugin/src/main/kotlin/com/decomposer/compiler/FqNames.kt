package com.decomposer.compiler

import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName

const val decomposerRuntimePackage = "com.decomposer.runtime"

val FQ_PRE_COMPOSE_IR: FqName = FqName("$decomposerRuntimePackage.PreComposeIr")
val CLASS_ID_PRE_COMPOSE_IR: ClassId = ClassId.topLevel(FQ_PRE_COMPOSE_IR)
val FQ_POST_COMPOSE_IR: FqName = FqName("$decomposerRuntimePackage.PostComposeIr")
val CLASS_ID_POST_COMPOSE_IR: ClassId = ClassId.topLevel(FQ_POST_COMPOSE_IR)
