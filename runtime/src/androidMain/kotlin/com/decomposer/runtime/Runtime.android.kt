package com.decomposer.runtime

import android.app.Application
import android.content.Context
import com.decomposer.runtime.composition.AndroidCompositionNormalizer
import com.decomposer.runtime.ir.AndroidProjectScanner

class AndroidRuntime(context: Context, config: RuntimeConfig) {
    private val projectScanner = AndroidProjectScanner(
        context = context,
        preloadAllIr = config.preloadAllIr,
        cacheIr = config.cacheIr,
        packagePrefixes = config.packagePrefixes
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

fun Application.runtimeInit(block: (RuntimeConfigScope.() -> Unit)? = null): AndroidRuntime {
    val scope = RuntimeConfigScope()
    block?.let { scope.it() }
    return AndroidRuntime(this, scope).also {
        it.init()
    }
}

interface RuntimeConfig {
    val preloadAllIr: Boolean
    val cacheIr: Boolean
    val packagePrefixes: List<String>?
}

class RuntimeConfigScope internal constructor(
    override var preloadAllIr: Boolean = false,
    override var cacheIr: Boolean = true,
    override var packagePrefixes: List<String>? = null,
) : RuntimeConfig
