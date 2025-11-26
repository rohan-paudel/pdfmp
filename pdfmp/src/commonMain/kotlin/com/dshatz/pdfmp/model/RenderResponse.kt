package com.dshatz.pdfmp.model

import com.dshatz.pdfmp.packList
import com.dshatz.pdfmp.unpackList
import kotlinx.io.Buffer
import kotlinx.io.readByteArray

open class RenderResponse(
    open val transforms: List<PageTransform>,
) {
    internal fun pack(): ByteArray {
        val buffer = Buffer()
        transforms.packList(
            buffer,
            packItem = PageTransform::pack
        )
        return buffer.readByteArray()
    }

    companion object {
        fun unpack(data: ByteArray): RenderResponse {
            val buffer = Buffer()
            buffer.write(data)

            val transforms = unpackList(
                buffer,
                unpackItem = PageTransform::unpack
            )
            return RenderResponse(
                transforms = transforms,
            )
        }
    }
}