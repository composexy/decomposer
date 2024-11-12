package com.decomposer.runtime

import kotlinx.serialization.Serializable

@MustBeDocumented
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class PreComposeIr(
    val fileData: Array<String>,
    val fqName: String,
    val path: String,
    val types: Array<String>,
    val signatures: Array<String>,
    val strings: Array<String>,
    val bodies: Array<String>,
    val declarations: Array<String>,
    val debugInfo: Array<String>,
    val backendSpecificMetadata: Array<String>
)

@MustBeDocumented
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class PostComposeIr(
    val fileData: Array<String>,
    val fqName: String,
    val path: String,
    val types: Array<String>,
    val signatures: Array<String>,
    val strings: Array<String>,
    val bodies: Array<String>,
    val declarations: Array<String>,
    val debugInfo: Array<String>,
    val backendSpecificMetadata: Array<String>
)

@Serializable
class SerializedIrFile(
    val fileData: Array<String>,
    val fqName: String,
    val path: String,
    val types: Array<String>,
    val signatures: Array<String>,
    val strings: Array<String>,
    val bodies: Array<String>,
    val declarations: Array<String>,
    val debugInfo: Array<String>,
    val backendSpecificMetadata: Array<String>,
)
