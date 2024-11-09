package com.decomposer.compiler

import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName

const val decomposerRuntimePackage = "com.decomposer.runtime"

val FQ_PRE_COMPOSE_IR: FqName = FqName("$decomposerRuntimePackage.PreComposeIr")

val FQ_POST_COMPOSE_IR: FqName = FqName("$decomposerRuntimePackage.PostComposeIr")

val FQ_IR_MANIFEST: FqName = FqName("$decomposerRuntimePackage.IrManifest")

val CLASS_ID_PRE_COMPOSE_IR: ClassId = ClassId.topLevel(FQ_PRE_COMPOSE_IR)

val CLASS_ID_POST_COMPOSE_IR: ClassId = ClassId.topLevel(FQ_POST_COMPOSE_IR)

val CLASS_ID_IR_MANIFEST: ClassId = ClassId.topLevel(FQ_IR_MANIFEST)

const val COMPILER_PACKAGE = "com.decomposer.compiler"

const val IR_MANIFEST_NAME = "IrManifestKt"

fun irManifestPackage(ownerPackage: String) = "$ownerPackage.$COMPILER_PACKAGE"
