package com.dshatz.pdfmp

import com.dshatz.pdfmp.model.SizeB

expect class ConsumerBuffer {
    fun <T> withAddress(action: (Long) -> T): T
    fun capacity(): SizeB
    fun free()
    val isFree: Boolean
    fun setUnfree()
}

expect object ConsumerBufferUtil {
    fun allocate(size: SizeB): ConsumerBuffer
}