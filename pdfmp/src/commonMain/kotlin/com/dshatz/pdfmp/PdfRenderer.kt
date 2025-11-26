package com.dshatz.pdfmp

import com.dshatz.pdfmp.model.RenderRequest
import com.dshatz.pdfmp.model.RenderResponse
import com.dshatz.pdfmp.source.PdfSource

expect class PdfRenderer constructor(source: PdfSource) {
    fun render(renderRequest: RenderRequest): RenderResponse
    fun getPageCount(): Int
    fun getAspectRatio(pageIndex: Int): Float
    fun getPageRatios(): List<Float>
    fun close()
}