package com.dshatz.pdfmp

import cnames.structs.fpdf_document_t__
import com.dshatz.internal.pdfium.*
import com.dshatz.pdfmp.error.PdfiumException
import com.dshatz.pdfmp.model.PageTransform
import com.dshatz.pdfmp.model.RenderRequest
import com.dshatz.pdfmp.model.RenderResponse
import com.dshatz.pdfmp.source.PdfSource
import kotlinx.atomicfu.locks.SynchronizedObject
import kotlinx.atomicfu.locks.synchronized
import kotlinx.cinterop.*

@OptIn(ExperimentalForeignApi::class)
actual class PdfRenderer actual constructor(private val source: PdfSource): SynchronizedObject() {

    private var doc: CPointer<fpdf_document_t__>? = null
    private var pinnedData: Pinned<ByteArray>? = null

    companion object {
        fun init() {
            FPDF_InitLibrary()
        }
    }

    @OptIn(UnsafeNumber::class)
    fun openFile(): Result<Unit> = synchronized(this) {
        return runCatching {
            doc = when (source) {
                is PdfSource.PdfBytes -> {
                    pinnedData = source.bytes.pin()
                    FPDF_LoadMemDocument(
                        pinnedData!!.addressOf(0),
                        source.bytes.size,
                        null
                    )
                }
                is PdfSource.PdfPath -> FPDF_LoadDocument(source.path.toString(), null)
            }
            if (doc == null || doc.rawValue == nativeNullPtr) {
                val pdfErrorCode = FPDF_GetLastError().toByte()
                PdfiumException.getError(pdfErrorCode)?.let { throw it }
            }
        }
    }

    @OptIn(UnsafeNumber::class)
    actual fun render(renderRequest: RenderRequest): Result<RenderResponse> = synchronized(this) {
        runCatching {
            renderPages(
                renderRequest.transforms,
                renderRequest.bufferAddress,
            )
        }
    }

    actual fun getPageCount(): Result<Int> = synchronized(this) {
        runCatching {
            val document = getDocument()
            FPDF_GetPageCount(document)
        }
    }

    @OptIn(UnsafeNumber::class)
    private fun CPointer<fpdf_document_t__>.openPage(pageIndex: Int): FPDF_PAGE? {
        return FPDF_LoadPage(this, pageIndex)
            ?: error("Failed to load page $pageIndex. Error: ${FPDF_GetLastError()}")
    }

    @OptIn(ExperimentalForeignApi::class, UnsafeNumber::class)
    private fun renderPages(
        transforms: List<PageTransform>,
        bufferAddress: Long,
    ): RenderResponse {
        val document = getDocument()

        var totalHeight = 0
        var maxWidth = 0

        transforms.forEachIndexed { index, it ->
            val h = it.scaledHeight - it.topCutoff - it.bottomCutoff
            val w = it.scaledWidth - it.leftCutoff - it.rightCutoff
            if (h <= 0 || w <= 0) error("Invalid slice dimensions")
            totalHeight += h + it.topGap
            maxWidth = maxOf(maxWidth, w)
        }

        if (totalHeight == 0 || maxWidth == 0) error("Total dimensions are zero")

        val stride = maxWidth * 4

        val targetPtr: CPointer<ByteVar> = bufferAddress.toCPointer<ByteVar>()
            ?: throw IllegalArgumentException("Invalid target memory address")

        val combinedBitmap = FPDFBitmap_CreateEx(
            maxWidth,
            totalHeight,
            FPDFBitmap_BGRA,
            targetPtr,
            stride
        ) ?: throw IllegalStateException("Failed to create combined bitmap wrapper")

        try {
            FPDFBitmap_FillRect(combinedBitmap, 0, 0, maxWidth, totalHeight, 0x00000000u)

            var currentY = 0

            transforms.forEach { transform ->
                currentY += transform.topGap
                val page = document.openPage(transform.pageIndex)
                try {
                    val sliceHeight = transform.scaledHeight - transform.topCutoff - transform.bottomCutoff
                    val sliceWidth = transform.scaledWidth - transform.leftCutoff - transform.rightCutoff

                    val offsetBytes = currentY * stride
                    val sliceBufferPtr = targetPtr.plus(offsetBytes)!!.reinterpret<CPointed>()

                    val subBitmap = FPDFBitmap_CreateEx(
                        sliceWidth,
                        sliceHeight,
                        FPDFBitmap_BGRA,
                        sliceBufferPtr,
                        stride
                    ) ?: throw IllegalStateException("Failed to create sub-bitmap")

                    try {
                        FPDFBitmap_FillRect(subBitmap, 0, 0, sliceWidth, sliceHeight, 0xFFFFFFFFu)

                        val startX = -transform.leftCutoff
                        val startY = -transform.topCutoff

                        FPDF_RenderPageBitmap(
                            subBitmap,
                            page,
                            startX,
                            startY,
                            transform.scaledWidth,
                            transform.scaledHeight,
                            0,
                            0
                        )
                    } finally {
                        FPDFBitmap_Destroy(subBitmap)
                    }
                    currentY += sliceHeight

                } finally {
                    FPDF_ClosePage(page)
                }
            }
            return RenderResponse(transforms)

        } finally {
            FPDFBitmap_Destroy(combinedBitmap)
        }
    }

    /**
     * Call this when the Screen/Component is destroyed
     */
    actual fun close() = synchronized(this) {
        runCatching {
            if (doc != null) {
                FPDF_CloseDocument(doc)
                pinnedData?.unpin()
                doc = null
            }
        }.onFailure {
            e("Could not close document", it)
        }
        Unit
    }

    fun getAspectRatio(pageIndex: Int): Float = synchronized(this) {
        return runCatching {
            val document = getDocument()

            val page = document.openPage(pageIndex)

            try {
                val width = FPDF_GetPageWidthF(page)
                val height = FPDF_GetPageHeightF(page)

                if (width <= 0f || height <= 0f) {
                    return@runCatching 1f
                }
                width / height

            } finally {
                FPDF_ClosePage(page)
            }
        }.getOrElse { e ->
            e("Native Error getting aspect ratio", e)
            0.707f
        }
    }

    actual fun getPageRatios(): Result<List<Float>> = synchronized(this) {
        val pageCount = getPageCount()
        pageCount.mapCatching { pageCount ->
            (0..<pageCount).map {
                getAspectRatio(it)
            }
        }
    }

    private fun getDocument(): CPointer<fpdf_document_t__> {
        return doc ?: throw IllegalStateException("Document is not open")
    }
}

