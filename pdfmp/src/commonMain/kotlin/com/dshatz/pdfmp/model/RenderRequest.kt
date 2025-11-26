package com.dshatz.pdfmp.model

import com.dshatz.pdfmp.packList
import com.dshatz.pdfmp.packMap
import com.dshatz.pdfmp.unpackList
import com.dshatz.pdfmp.unpackMap
import kotlinx.io.Buffer
import kotlinx.io.readByteArray

data class RenderRequest(
    val transforms: List<PageTransform>,
    val bufferAddress: Long
) {
    fun pack(): ByteArray {
        val buffer = Buffer()
        buffer.writeLong(bufferAddress)
        transforms.packList(
            buffer,
            packItem = PageTransform::pack
        )
        return buffer.readByteArray()
    }

    companion object {
        fun unpack(data: ByteArray): RenderRequest {
            val buffer = Buffer()
            buffer.write(data)
            val bufferAddress = buffer.readLong()

            val imageTransforms = unpackList(
                buffer,
                unpackItem = PageTransform::unpack
            )
            return RenderRequest(
                imageTransforms, bufferAddress
            )
        }
    }
}