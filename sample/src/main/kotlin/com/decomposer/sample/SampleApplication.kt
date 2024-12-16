package com.decomposer.sample

import android.app.Application
import com.decomposer.runtime.runtimeInit

class SampleApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            runtimeInit {
                packagePrefixes = listOf(this@SampleApplication.packageName)
            }
        }
    }
}
