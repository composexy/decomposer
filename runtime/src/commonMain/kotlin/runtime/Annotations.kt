package com.decomposer.runtime

@Repeatable
@MustBeDocumented
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class ComposeIr(
    val composed: Boolean,
    val data: Array<String>
)
