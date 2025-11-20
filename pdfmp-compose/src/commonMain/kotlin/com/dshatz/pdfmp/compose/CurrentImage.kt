package com.dshatz.pdfmp.compose

import com.dshatz.pdfmp.ConsumerBuffer
import com.dshatz.pdfmp.ImageTransform
import com.dshatz.pdfmp.PageSize
import com.dshatz.pdfmp.RenderResponse


interface ICurrentImage {
    val requestedTransform: ImageTransform
    val loadedTransform: ImageTransform
    val pageSize: PageSize
    val buffer: ConsumerBuffer
}


expect class CurrentImage(
    requestedTransform: ImageTransform,
    loadedTransform: ImageTransform,
    pageSize: PageSize,
    buffer: ConsumerBuffer
): ICurrentImage {
    override val requestedTransform: ImageTransform
    override val loadedTransform: ImageTransform
    override val pageSize: PageSize
    override val buffer: ConsumerBuffer

}