package com.dshatz.pdfmp

import com.dshatz.pdfmp.model.PageTransform
import com.dshatz.pdfmp.model.RenderRequest

/*
fun <T> withRenderRequest(
    page: Int,
    transform: PageTransform,
    bufferPool: ConsumerBufferPool,
    action: (RenderRequest) -> T
): Pair<T, ConsumerBuffer> {
    val buffer = bufferPool.getBufferPage(page, transform)
    return buffer.withAddress { address ->
        action(RenderRequest(page, transform, address)) to buffer
    }
}*/
