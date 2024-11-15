package com.decomposer.runtime.ir

import android.app.Application
import android.content.Context

class AndroidRuntime(context: Context) {
    private val projectScanner = AndroidProjectScanner(context)
    private val client = AndroidOkHttpClient(projectScanner = projectScanner)

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
