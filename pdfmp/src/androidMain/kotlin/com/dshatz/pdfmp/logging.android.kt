package com.dshatz.pdfmp

import android.util.Log

actual fun isDebug(): Boolean = isAndroidDebug()
internal actual fun logPlatform(level: LogLevel, tag: String, message: String) {
    when (level) {
        LogLevel.VERBOSE -> Log.v(tag, message)
        LogLevel.DEBUG -> Log.d(tag, message)
        LogLevel.INFO -> Log.i(tag, message)
        LogLevel.WARN -> Log.w(tag, message)
        LogLevel.ERROR -> Log.e(tag, message)
    }
}