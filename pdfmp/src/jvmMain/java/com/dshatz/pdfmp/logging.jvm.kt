package com.dshatz.pdfmp

internal actual fun logPlatform(level: com.dshatz.pdfmp.LogLevel, tag: String, message: String) {
    logUsingPrintln(level, tag, message)
}