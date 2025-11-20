package com.dshatz.pdfmp

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.toLong
import kotlinx.cinterop.usePinned

actual class ConsumerBuffer(val byteArray: ByteArray) {
    @OptIn(ExperimentalForeignApi::class)
    actual fun <T> withAddress(action: (Long) -> T): T {
        return byteArray.usePinned {
            action(it.addressOf(0).toLong())
        }
    }

    actual fun capacity(): Int {
        return byteArray.size
    }
}

actual object ConsumerBufferUtil {
    actual fun allocate(size: Int): ConsumerBuffer {
        println("Allocating buffer ByteArray($size)")
        return ConsumerBuffer(ByteArray(size))
    }
}