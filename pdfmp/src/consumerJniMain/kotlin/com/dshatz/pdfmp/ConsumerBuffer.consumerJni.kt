package com.dshatz.pdfmp

import com.dshatz.pdfmp.model.SizeB
import java.nio.ByteBuffer
import java.nio.ByteOrder.LITTLE_ENDIAN


actual class ConsumerBuffer(val buffer: ByteBuffer) {
    actual fun <T> withAddress(action: (Long) -> T): T {
        return action(PDFBridge.getBufferAddress(buffer))
    }

    actual fun capacity(): SizeB {
        return SizeB(buffer.capacity())
    }

    actual fun free() {}
}

actual object ConsumerBufferUtil {
    actual fun allocate(size: SizeB): ConsumerBuffer {
        return ConsumerBuffer(
            ByteBuffer.allocateDirect(size.bytes).order(LITTLE_ENDIAN)
        )
    }
}