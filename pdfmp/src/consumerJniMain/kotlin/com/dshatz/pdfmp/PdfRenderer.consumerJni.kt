package com.dshatz.pdfmp

import com.dshatz.pdfmp.model.RenderRequest
import com.dshatz.pdfmp.model.RenderResponse
import com.dshatz.pdfmp.source.PdfSource
import kotlinx.io.Buffer

actual class PdfRenderer actual constructor(private val source: PdfSource) {

    private val renderer = unpackResult(PDFBridge.openFile(source.pack()), Buffer::readLong)
    
    actual fun render(renderRequest: RenderRequest): Result<RenderResponse> {
        return renderer.mapCatching { renderer ->
            val packed = renderRequest.pack()
            val response = unpackResult(
                PDFBridge.render(renderer,packed),
                RenderResponse::unpack
            )
            response.getOrThrow()
        }
    }

    actual fun close() {
        renderer.getOrNull()?.let(PDFBridge::close)
    }

    actual fun getPageCount(): Result<Int> {
        return renderer.mapCatching {
            unpackResult(PDFBridge.getPageCount(it), Buffer::readInt).getOrThrow()
        }
    }

    actual fun getPageRatios(): Result<List<Float>> {
        return renderer.mapCatching {
            unpackResult(PDFBridge.getPageRatios(it), ::unpackFloats).getOrThrow()
        }
    }
}