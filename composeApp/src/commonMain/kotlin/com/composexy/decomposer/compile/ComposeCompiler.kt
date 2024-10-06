package com.composexy.decomposer.compile

interface ComposeCompiler {
    fun decompose(source: String): DecomposeResult
}

class DecomposeResult(
    val isSuccessful: Boolean,
    val decomposedSource: String?
)

expect fun getComposeCompiler(): ComposeCompiler
