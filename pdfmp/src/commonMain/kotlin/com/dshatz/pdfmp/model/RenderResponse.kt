package com.dshatz.pdfmp.model

import com.dshatz.pdfmp.packList
import com.dshatz.pdfmp.unpackList
import kotlinx.io.Buffer
import kotlinx.io.readByteArray

open class RenderResponse(
    open val transforms: List<PageTransform>,
) {

    companion object {
        fun unpack(buffer: Buffer): RenderResponse {
            val transforms = unpackList(
                buffer,
                unpackItem = PageTransform::unpack
            )
            return RenderResponse(
                transforms = transforms,
            )
        }

        internal fun pack(buffer: Buffer, response: RenderResponse) {
            response.transforms.packList(
                buffer,
                packItem = PageTransform::pack
            )
        }
    }
}