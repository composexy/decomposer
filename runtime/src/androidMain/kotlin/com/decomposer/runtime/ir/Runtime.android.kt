package com.decomposer.runtime.ir

import android.app.Application
import android.content.Context
import com.decomposer.runtime.connection.ProjectSnapshotHandler
import com.decomposer.runtime.connection.VirtualFileIrHandler

class AndroidRuntime(context: Context) {
    private val projectScanner = AndroidProjectScanner(context)
    private val client = AndroidClient(
        commandHandlers = setOf(
            ProjectSnapshotHandler(projectScanner),
            VirtualFileIrHandler(projectScanner)
        )
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
