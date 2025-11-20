package com.dshatz.pdfmp

fun <T> withRenderRequest(
    page: Int,
    transform: ImageTransform,
    bufferPool: ConsumerBufferPool,
    action: (RenderRequest) -> T
): Pair<T, ConsumerBuffer> {
    val buffer = bufferPool.getBuffer(page, transform)
    return buffer.withAddress { address ->
        action(RenderRequest(page, transform, address)) to buffer
    }
}