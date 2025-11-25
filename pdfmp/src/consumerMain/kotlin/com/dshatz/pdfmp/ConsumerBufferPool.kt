package com.dshatz.pdfmp

class ConsumerBufferPool {
    private val bufferFull: MutableMap<Int, ConsumerBuffer> = mutableMapOf()
    private val bufferSlice: MutableMap<Int, ConsumerBuffer> = mutableMapOf()

    fun getBuffer(page: Int, transform: ImageTransform): ConsumerBuffer {
        val (width, height) = (transform.scaledWidth).toInt() to (transform.scaledHeight).toInt()
        val neededCapacity = width * height * 4
        val bufferMap = bufferFull
        val buffer = bufferMap[page]
        if (buffer == null || buffer.capacity() < neededCapacity) {
            bufferMap[page] = ConsumerBufferUtil.allocate(neededCapacity)
        }
        return bufferMap[page]!!
    }
}