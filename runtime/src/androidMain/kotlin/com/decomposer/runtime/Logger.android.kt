package com.decomposer.runtime

import android.util.Log

internal object AndroidLogger : Logger {
    override fun log(level: Logger.Level, tag: String, message: String) {
        when (level) {
            Logger.Level.DEBUG -> Log.d(tag, message)
            Logger.Level.INFO -> Log.i(tag, message)
            Logger.Level.WARNING -> Log.w(tag, message)
            Logger.Level.ERROR -> Log.e(tag, message)
        }
    }
}
