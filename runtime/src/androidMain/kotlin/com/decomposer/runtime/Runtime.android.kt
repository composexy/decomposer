package com.decomposer.runtime

import android.app.Application
import android.content.Context
import com.decomposer.runtime.composition.AndroidCompositionNormalizer
import com.decomposer.runtime.ir.AndroidProjectScanner

class AndroidRuntime(context: Context) {
    private val projectScanner = AndroidProjectScanner(context)
    private val compositionNormalizer = AndroidCompositionNormalizer(context)
    private val client = AndroidOkHttpClient(
        projectScanner = projectScanner,
        compositionNormalizer = compositionNormalizer
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
