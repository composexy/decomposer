package com.decomposer.sample

import android.app.Application
import com.decomposer.runtime.ir.runtimeInit

class SampleApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        runtimeInit()
    }
}
