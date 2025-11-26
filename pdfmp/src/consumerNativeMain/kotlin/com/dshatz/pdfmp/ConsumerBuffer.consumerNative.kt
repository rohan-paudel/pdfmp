package com.dshatz.pdfmp

import com.dshatz.pdfmp.model.SizeB
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

    actual fun capacity(): SizeB {
        return SizeB(byteArray.size)
    }

    actual fun free() {
    }
}

actual object ConsumerBufferUtil {
    actual fun allocate(size: SizeB): ConsumerBuffer {
        println("Allocating buffer ByteArray($size)")
        return ConsumerBuffer(ByteArray(size.bytes))
    }
}