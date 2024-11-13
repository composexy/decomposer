package com.decomposer.runtime

@Repeatable
@MustBeDocumented
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class ComposeIr(
    val composed: Boolean,
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
