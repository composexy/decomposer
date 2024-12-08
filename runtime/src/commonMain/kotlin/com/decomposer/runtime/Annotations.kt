package com.decomposer.runtime

@MustBeDocumented
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class PreComposeIr(
    val filePath: String,
    val packageName: String,
    val isFileFacade: Boolean,
    val standardDump: Array<String>,
    val data: Array<String>,
)

@MustBeDocumented
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class PostComposeIr(
    val filePath: String,
    val packageName: String,
    val isFileFacade: Boolean,
    val standardDump: Array<String>,
    val data: Array<String>,
)
