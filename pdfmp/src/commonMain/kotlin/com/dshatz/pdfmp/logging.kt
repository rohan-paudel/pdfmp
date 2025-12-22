package com.dshatz.pdfmp

import kotlin.time.Clock
import kotlin.time.ExperimentalTime

expect fun isDebug(): Boolean

@OptIn(ExperimentalTime::class)
fun log(
    level: LogLevel,
    tag: String = "PDFMP",
    message: String
) {
    if (shouldLog(level)) {
        logPlatform(
            level,
            tag,
            message
        )
    }
}

internal expect fun logPlatform(
    level: LogLevel,
    tag: String = "PDFMP",
    message: String
)

@OptIn(ExperimentalTime::class)
internal fun logUsingPrintln(
    level: LogLevel,
    tag: String = "PDFMP",
    message: String
) {
    val paddedTag = "[$tag]".padStart(10, ' ')
    val paddedLevel = level.name.padStart(8, ' ')
    println("${Clock.System.now()} $paddedTag $paddedLevel: $message")
}

fun shouldLog(level: LogLevel): Boolean {
    return isDebug() || level == LogLevel.ERROR
}

fun v(
    message: String,
    tag: String = "PDFMP"
) {
    log(LogLevel.VERBOSE, tag, message)
}

fun d(
    message: String,
    tag: String = "PDFMP"
) {
    log(LogLevel.DEBUG, tag, message)
}

fun i(
    message: String,
    tag: String = "PDFMP"
) {
    log(LogLevel.INFO, tag, message)
}

fun w(
    message: String,
    exception: Throwable? = null,
    tag: String = "PDFMP"
) {
    log(LogLevel.WARN, tag, message + exception?.message?.let {
        ": $it"
    }.orEmpty())
}

fun e(
    message: String,
    exception: Throwable,
    tag: String = "PDFMP"
) {
    log(LogLevel.ERROR, tag, message + ": " + exception.message)
    exception.printStackTrace()
    if (exception is PDFFMPNativeException) println("Native stacktrace: \n${exception.nativeStackTrace}")
}
enum class LogLevel {
    VERBOSE,
    DEBUG,
    INFO,
    WARN,
    ERROR
}