package com.decomposer.sample

import android.app.Application
import com.decomposer.runtime.ir.AndroidProjectScanner
import com.decomposer.runtime.ir.ProjectScanner

class SampleApplication : Application() {

    private lateinit var projectScanner: ProjectScanner

    override fun onCreate() {
        super.onCreate()
        projectScanner = AndroidProjectScanner(this, true)
    }
}
