package com.dshatz.pdfmp

import com.dshatz.pdfmp.source.PdfSource
import java.nio.ByteBuffer
import java.nio.ByteOrder.LITTLE_ENDIAN

actual class PdfRenderer actual constructor(private val source: PdfSource) {

    private val renderer = PDFBridge.openFile(source.pack())
    
    actual fun render(renderRequest: RenderRequest): RenderResponse {
        val packed = renderRequest.pack()
        val response = RenderResponse.fromPacked(
            PDFBridge.render(renderer,packed)
        )
        return RenderResponse(
            response.transform,
            response.pageSize
        )
    }

    actual fun close()  {
        PDFBridge.close(renderer)
    }

    actual fun getPageCount(): Int {
        return PDFBridge.getPageCount(renderer)
    }

    actual fun getAspectRatio(pageIndex: Int): Float {
        return PDFBridge.getAspectRatio(renderer, pageIndex)
    }

    actual fun getPageRatios(): List<Float> {
        return unpackFloats(PDFBridge.getPageRatios(renderer))
    }


    /*data class JvmRenderResponse(
        override val transform: ImageTransform,
        override val pageSize: PageSize,
        val byteBuffer: ByteBuffer
    ): RenderResponse(transform, pageSize)*/

}