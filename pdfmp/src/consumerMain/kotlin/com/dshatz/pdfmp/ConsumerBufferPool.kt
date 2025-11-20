package com.dshatz.pdfmp

class ConsumerBufferPool {
    private val buffersScale1: MutableMap<Int, ConsumerBuffer> = mutableMapOf()
    private val bufferScaleX: MutableMap<Int, ConsumerBuffer> = mutableMapOf()

    fun getBuffer(page: Int, transform: ImageTransform): ConsumerBuffer {
        val neededCapacity = transform.viewportWidth * transform.viewportHeight * 4
        val bufferMap = if (transform.scale == 1f) {
            buffersScale1
        } else {
            bufferScaleX
        }
        val buffer = bufferMap[page]
        if (buffer == null || buffer.capacity() < neededCapacity) {
            bufferMap[page] = ConsumerBufferUtil.allocate(neededCapacity)
        }
        return bufferMap[page]!!
    }
}