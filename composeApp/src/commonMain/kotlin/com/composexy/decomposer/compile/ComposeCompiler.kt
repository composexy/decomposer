package com.composexy.decomposer.compile

import com.composexy.decomposer.Config

interface ComposeCompiler {
    fun decompose(
        composeSource: String,
        options: Map<Config, Boolean>,
    ): DecomposeResult
}

class DecomposeResult(
    val isSuccessful: Boolean,
    val decomposedSource: String?
)

expect fun getComposeCompiler(): ComposeCompiler
