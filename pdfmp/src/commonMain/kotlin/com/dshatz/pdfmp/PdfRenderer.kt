package com.dshatz.pdfmp

import com.dshatz.pdfmp.source.PdfSource
import kotlinx.io.Buffer
import kotlinx.io.files.Path
import kotlinx.io.readByteArray
import kotlinx.io.readFloat
import kotlinx.io.writeFloat

open class RenderResponse(
    open val transform: ImageTransform,
) {
    internal fun pack(): ByteArray {
        val buffer = Buffer()
        buffer.write(transform.pack())
        return buffer.readByteArray()
    }

    companion object {
        fun fromPacked(data: ByteArray): RenderResponse {
            val buffer = Buffer()
            buffer.write(data)

            val transform = ImageTransform.unpack(data)
            return RenderResponse(
                transform = transform,
            )
        }
    }
}

/*data class PageSize(val width: Float, val height: Float) {
    fun pack(): ByteArray {
        return Buffer().also {
            it.writeFloat(width)
            it.writeFloat(height)
        }.readByteArray()
    }

    companion object {
        internal fun unpack(data: Buffer): PageSize {
            return PageSize(data.readFloat(), data.readFloat())
        }
    }
}*/

expect class PdfRenderer constructor(source: PdfSource) {
    fun render(renderRequest: RenderRequest): RenderResponse
    fun getPageCount(): Int
    fun getAspectRatio(pageIndex: Int): Float
    fun getPageRatios(): List<Float>
    fun close()
}