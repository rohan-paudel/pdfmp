package com.dshatz.pdfmp

import java.nio.ByteBuffer
import java.nio.ByteOrder.LITTLE_ENDIAN

actual class ConsumerBuffer(val buffer: ByteBuffer) {
    actual fun <T> withAddress(action: (Long) -> T): T {
        return action(PDFBridge.getBufferAddress(buffer))
    }

    actual fun capacity(): Int {
        return buffer.capacity()
    }

}

actual object ConsumerBufferUtil {
    actual fun allocate(size: Int): ConsumerBuffer {
        println("Allocating direct java.nio.ByteBuffer($size)")
        return ConsumerBuffer(ByteBuffer.allocateDirect(size).order(LITTLE_ENDIAN))
    }
}