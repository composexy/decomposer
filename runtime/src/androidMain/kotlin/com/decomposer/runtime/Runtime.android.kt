package com.decomposer.runtime

import android.app.Application
import android.content.Context
import com.decomposer.runtime.composition.AndroidCompositionNormalizer
import com.decomposer.runtime.ir.AndroidProjectScanner

class AndroidRuntime(context: Context, config: RuntimeConfig) {
    private val projectScanner = AndroidProjectScanner(
        context = context,
        preloadAllIr = config.preloadAllIr,
    )
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

fun Application.runtimeInit(block: RuntimeConfigScope.() -> Unit): AndroidRuntime {
    val scope = RuntimeConfigScope()
    scope.block()
    return AndroidRuntime(this, scope).also {
        it.init()
    }
}

interface RuntimeConfig {
    val preloadAllIr: Boolean
}

class RuntimeConfigScope internal constructor(
    override var preloadAllIr: Boolean = false,
) : RuntimeConfig
