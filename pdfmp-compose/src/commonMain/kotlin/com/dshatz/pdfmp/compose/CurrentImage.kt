package com.dshatz.pdfmp.compose

import com.dshatz.pdfmp.ConsumerBuffer
import com.dshatz.pdfmp.ImageTransform


interface ICurrentImage {
    val requestedTransform: ImageTransform
    val loadedTransform: ImageTransform
    val buffer: ConsumerBuffer
}


data class CurrentImage(
    override val requestedTransform: ImageTransform,
    override val loadedTransform: ImageTransform,
    override val buffer: ConsumerBuffer
): ICurrentImage