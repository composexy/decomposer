package com.decomposer.runtime.ir

import android.app.Application
import android.content.Context

class AndroidRuntime(context: Context) {
    private val projectScanner = AndroidProjectScanner(context)
    private val compositionExtractor = AndroidCompositionExtractor(context)
    private val client = AndroidOkHttpClient(
        projectScanner = projectScanner,
        compositionExtractor = compositionExtractor
    )

    fun init() {
        projectScanner.scanProject()
        client.start()
    }
}

fun Application.runtimeInit(): AndroidRuntime {
    return AndroidRuntime(this).also {
        it.init()
    }
}
