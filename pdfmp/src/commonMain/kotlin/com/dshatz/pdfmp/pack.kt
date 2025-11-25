package com.dshatz.pdfmp

import kotlinx.io.*

data class ImageTransform(
    val topCutoff: Int,
    val bottomCutoff: Int,
    val leftCutoff: Int,
    val rightCutoff: Int,
    val scaledWidth: Int,
    val scaledHeight: Int,
    val scale: Float
) {

    fun size(): Pair<Int, Int> {
        return scaledWidth - leftCutoff - rightCutoff to scaledHeight - topCutoff - bottomCutoff
    }

    fun uncut(): ImageTransform {
        return copy(
            topCutoff = 0,
            bottomCutoff = 0,
            leftCutoff = 0,
            rightCutoff = 0
        )
    }

    fun pack(): ByteArray {
        return Buffer().also {
            it.writeInt(topCutoff)
            it.writeInt(bottomCutoff)
            it.writeInt(leftCutoff)
            it.writeInt(rightCutoff)
            it.writeInt(scaledWidth)
            it.writeInt(scaledHeight)
            it.writeFloat(scale)
        }.readByteArray()
    }

    companion object {
        internal const val packedSizeBytes = Int.SIZE_BYTES * 6 + Float.SIZE_BYTES
        fun unpack(data: ByteArray): ImageTransform {
            val buffer = Buffer()
            buffer.write(data, 0, packedSizeBytes)
            return ImageTransform(
                buffer.readInt(),
                buffer.readInt(),
                buffer.readInt(),
                buffer.readInt(),
                buffer.readInt(),
                buffer.readInt(),
                buffer.readFloat()
            )
        }
    }
}

data class RenderRequest(
    val page: Int,
    val transform: ImageTransform,
    val bufferAddress: Long
) {
    fun pack(): ByteArray {
        val buffer = Buffer()
        buffer.writeLong(bufferAddress)
        buffer.writeInt(page)
        buffer.write(transform.pack())

        return buffer.readByteArray()
    }

    companion object {
        fun unpack(data: ByteArray): RenderRequest {
            val buffer = Buffer()
            buffer.write(data)
            val bufferAddress = buffer.readLong()
            val page = buffer.readInt()
            return RenderRequest(
                page, ImageTransform.unpack(buffer.readByteArray()), bufferAddress
            )
        }
    }
}

