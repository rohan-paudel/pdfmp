package com.dshatz.pdfmp

import com.dshatz.pdfmp.model.BufferDimensions
import com.dshatz.pdfmp.model.SizeB
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.toLong
import kotlinx.cinterop.usePinned

actual class ConsumerBuffer(
    val byteArray: ByteArray,
    actual val dimensions: BufferDimensions
) {
    @OptIn(ExperimentalForeignApi::class)
    actual fun <T> withAddress(action: (Long) -> T): T {
        return byteArray.usePinned {
            action(it.addressOf(0).toLong())
        }
    }

    actual fun capacity(): SizeB {
        return SizeB(byteArray.size.toLong())
    }

    actual fun free() {
        _isFree = true
    }

    private var _isFree: Boolean = true

    actual val isFree: Boolean get() = _isFree

    actual fun setUnfree() {
        _isFree = false
    }

    actual fun dispose() {
    }
}

actual object ConsumerBufferUtil {
    actual fun allocate(size: SizeB, width: Int, height: Int): ConsumerBuffer {
        d("Allocating buffer ByteArray($size)")
        return ConsumerBuffer(
            ByteArray(size.bytes.toInt()),
            dimensions = BufferDimensions(width, height, width * 4)
        )
    }
}