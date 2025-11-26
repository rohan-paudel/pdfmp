package com.dshatz.pdfmp

import cnames.structs.fpdf_document_t__
import com.dshatz.internal.pdfium.*
import com.dshatz.pdfmp.model.PageTransform
import com.dshatz.pdfmp.model.RenderRequest
import com.dshatz.pdfmp.model.RenderResponse
import com.dshatz.pdfmp.source.PdfSource
import kotlinx.atomicfu.locks.SynchronizedObject
import kotlinx.atomicfu.locks.synchronized
import kotlinx.cinterop.*
import platform.posix.memcpy

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
    fun openFile() = synchronized(this) {
        runCatching {
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
        }
        if (doc == null || doc.rawValue == nativeNullPtr) {
            // The document failed to load. Log an error or throw an exception.
            println("ERROR: FPDF_DOCUMENT failed to load. Check file path/integrity/password. ${FPDF_GetLastError()}")
        }
    }

    @OptIn(UnsafeNumber::class)
    actual fun render(renderRequest: RenderRequest): RenderResponse = synchronized(this) {
        if (doc == null) {
            val error = FPDF_GetLastError()
            println("Failed to load PDF from $source. Error code: $error")
            throw IllegalStateException("Failed to load PDF from $source. Error code: $error")
        }

        return renderPages(
            renderRequest.transforms,
            renderRequest.bufferAddress
        )
    }

    actual fun getPageCount(): Int = synchronized(this) {
        runCatching {
            val document = doc ?: throw IllegalStateException("Document is not open")
            return FPDF_GetPageCount(document)
        }.getOrElse {
            println("Could not get page count: ${it.message}")
            0
        }
    }

    @OptIn(UnsafeNumber::class)
    private fun CPointer<fpdf_document_t__>.openPage(pageIndex: Int): FPDF_PAGE? {
        return runCatching {
            FPDF_LoadPage(this, pageIndex)
                ?: error("Failed to load page $pageIndex. Error: ${FPDF_GetLastError()}")
        }.getOrElse {
            println(it.message)
            return null
        }
    }

    @OptIn(ExperimentalForeignApi::class, UnsafeNumber::class)
    private fun renderPages(
        transforms: List<PageTransform>, // Map <PageIndex, Transform>
        bufferAddress: Long
    ): RenderResponse {
        // Here we will render all the transforms on top of each other into one big bitmap.
        return runCatching {
            val document = doc ?: throw IllegalStateException("Document is not open")

            // Total dimensions
            var totalHeight = 0
            var maxWidth = 0

            transforms.forEach {
                val h = it.scaledHeight - it.topCutoff - it.bottomCutoff
                val w = it.scaledWidth - it.leftCutoff - it.rightCutoff
                if (h <= 0 || w <= 0) error("Invalid slice dimensions")
                totalHeight += h
                maxWidth = maxOf(maxWidth, w)
            }

            if (totalHeight == 0 || maxWidth == 0) error("Total dimensions are zero")

            // The giant result bitmap
            val combinedBitmap = FPDFBitmap_CreateEx(maxWidth, totalHeight, FPDFBitmap_BGRx, null, 0)
                ?: throw IllegalStateException("Failed to create combined bitmap")

            try {
                FPDFBitmap_FillRect(combinedBitmap, 0, 0, maxWidth, totalHeight, 0xFFFFFFFFu)

                // Get the raw pointer and stride of the giant bitmap
                val combinedBufferPtr = FPDFBitmap_GetBuffer(combinedBitmap)
                    ?: throw IllegalStateException("Failed to get combined buffer")
                val combinedStride = FPDFBitmap_GetStride(combinedBitmap)

                var currentY = 0

                // Render each page slice
                transforms.forEach { transform ->
                    val page = document.openPage(transform.pageIndex)
                    try {
                        val sliceHeight = transform.scaledHeight - transform.topCutoff - transform.bottomCutoff
                        val sliceWidth = transform.scaledWidth - transform.leftCutoff - transform.rightCutoff

                        // Calculate the pointer offset for this specific slice
                        // Offset = Rows * BytesPerRow
                        val offsetBytes = currentY * combinedStride

                        // Pointer arithmetic to find location of this slice's first pixel within the big bitmap.
                        val sliceBufferPtr = combinedBufferPtr.reinterpret<ByteVar>()
                            .plus(offsetBytes)!!.reinterpret<CPointed>()

                        // Create a "View" into the combined buffer
                        // Important to pass `combinedStride` so pdfium knows when to paint the next line.
                        // This has to be the big stride, not the slice stride in case slices have different width.
                        val subBitmap = FPDFBitmap_CreateEx(
                            sliceWidth,
                            sliceHeight,
                            FPDFBitmap_BGRx,
                            sliceBufferPtr,
                            combinedStride
                        ) ?: throw IllegalStateException("Failed to create sub-bitmap")

                        try {
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
                            // Destroy the slice bitmap.
                            FPDFBitmap_Destroy(subBitmap)
                        }

                        // next slice
                        currentY += sliceHeight

                    } finally {
                        FPDF_ClosePage(page)
                    }
                }

                // 4. Copy the final result to the user's buffer
                val totalByteCount = totalHeight * combinedStride
                memScoped {
                    val targetPtr: CPointer<out CPointed> = bufferAddress.toCPointer<CPointed>()
                        ?: throw IllegalArgumentException("Invalid target memory address")

                    // reinterpret needed to match memcpy signature
                    memcpy(targetPtr, combinedBufferPtr, totalByteCount.convert())
                }

                return RenderResponse(transforms)

            } finally {
                // Now we destroy the giant bitmap, which frees the memory we allocated at step 2
                FPDFBitmap_Destroy(combinedBitmap)
            }

        }.getOrElse {
            println("Could not render stacked pages: ${it.message}")
            throw(it)
        }
    }

    // You should call this when the Screen/Component is destroyed
    actual fun close() = synchronized(this) {
        runCatching {
            if (doc != null) {
                FPDF_CloseDocument(doc)
                pinnedData?.unpin()
                doc = null
            }
        }.onFailure {
            println("Could not close document: ${it.message}")
        }
        Unit
        // FPDF_DestroyLibrary() // Call this only when app exits strictly
    }

    actual fun getAspectRatio(pageIndex: Int): Float = synchronized(this) {
        return runCatching {
            val document = doc ?: error("Document is not open")

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
            println("Native Error getting aspect ratio: ${e.message}")
            0.707f
        }
    }

    actual fun getPageRatios(): List<Float> = synchronized(this) {
        val pageCount = getPageCount()
        (0..<pageCount).map {
            getAspectRatio(it)
        }
    }
}

