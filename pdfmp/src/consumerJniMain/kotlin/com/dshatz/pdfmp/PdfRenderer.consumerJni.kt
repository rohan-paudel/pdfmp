package com.dshatz.pdfmp

import com.dshatz.pdfmp.model.RenderRequest
import com.dshatz.pdfmp.model.RenderResponse
import com.dshatz.pdfmp.source.PdfSource

actual class PdfRenderer actual constructor(private val source: PdfSource) {

    private val renderer = PDFBridge.openFile(source.pack())
    
    actual fun render(renderRequest: RenderRequest): RenderResponse {
        val packed = renderRequest.pack()
        val response = RenderResponse.unpack(
            PDFBridge.render(renderer,packed)
        )
        return RenderResponse(
            response.transforms,
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