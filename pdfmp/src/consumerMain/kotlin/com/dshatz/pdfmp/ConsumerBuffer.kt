package com.dshatz.pdfmp

expect class ConsumerBuffer {
    fun <T> withAddress(action: (Long) -> T): T
    fun capacity(): Int

    fun free()
}

expect object ConsumerBufferUtil {
    fun allocate(size: Int): ConsumerBuffer
}