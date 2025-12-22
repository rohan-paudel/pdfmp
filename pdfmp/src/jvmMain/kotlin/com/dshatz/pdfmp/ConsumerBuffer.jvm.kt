package com.dshatz.pdfmp

import com.dshatz.pdfmp.model.BufferDimensions
import com.dshatz.pdfmp.model.SizeB
import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.ColorAlphaType
import org.jetbrains.skia.ColorType
import org.jetbrains.skia.ImageInfo

actual class ConsumerBuffer(
    val skiaBitmap: Bitmap,
) {

    actual val dimensions: BufferDimensions = BufferDimensions(
        skiaBitmap.width,
        skiaBitmap.height,
        skiaBitmap.rowBytes
    )
    actual fun <T> withAddress(action: (Long) -> T): T {
        return skiaBitmap.peekPixels()?.buffer?.writableData()?.let(action) ?: error("Could not read bitmap buffer address")
    }

    actual fun capacity(): SizeB {
        return SizeB(skiaBitmap.computeByteSize().toLong())
    }

    private var free = true
    actual fun free() {
        free = true
    }

    actual val isFree: Boolean
        get() = free

    actual fun setUnfree() {
        free = false
    }

    actual fun dispose() {
        skiaBitmap.close()
    }
}

actual object ConsumerBufferUtil {
    actual fun allocate(size: SizeB, width: Int, height: Int): ConsumerBuffer {
        val imageInfo = ImageInfo(
            width,
            height,
            ColorType.BGRA_8888,
            ColorAlphaType.PREMUL
        )

        val skiaBitmap = Bitmap()
        skiaBitmap.allocPixels(imageInfo)
        return ConsumerBuffer(skiaBitmap)
    }
}