package com.dshatz.pdfmp

import sun.nio.ch.DirectBuffer
import java.lang.ref.Cleaner
import java.nio.ByteBuffer
import java.nio.ByteOrder.LITTLE_ENDIAN


actual class ConsumerBuffer(val buffer: ByteBuffer) {
    actual fun <T> withAddress(action: (Long) -> T): T {
        return action(PDFBridge.getBufferAddress(buffer))
    }

    actual fun capacity(): Int {
        return buffer.capacity()
    }

    actual fun free() {
        /*val cleaner: jdk.internal.ref.Cleaner? = (buffer as DirectBuffer).cleaner()
        if (cleaner != null) cleaner.clean()*/
    }
}

actual object ConsumerBufferUtil {
    actual fun allocate(size: Int): ConsumerBuffer {
        return ConsumerBuffer(ByteBuffer.allocateDirect(size).order(LITTLE_ENDIAN))
    }
}