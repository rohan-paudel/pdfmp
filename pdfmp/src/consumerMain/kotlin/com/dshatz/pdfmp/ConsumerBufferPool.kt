package com.dshatz.pdfmp

class ConsumerBufferPool {
    private val bufferFull: MutableMap<Int, ConsumerBuffer> = mutableMapOf()

    fun getBuffer(page: Int, transform: ImageTransform): ConsumerBuffer {
        val (width, height) = (transform.scaledWidth).toInt() to (transform.scaledHeight).toInt()
        val neededCapacity = width * height * 4
        val bufferMap = bufferFull
        val buffer = bufferMap[page]
        if (buffer == null || buffer.capacity() < neededCapacity) {
            bufferMap[page] = ConsumerBufferUtil.allocate(neededCapacity)
            println("[$page] Allocated ${neededCapacity/1024/1024}MB ${transform.scaledWidth} x ${transform.scaledHeight}")
            println("Total buffer memory: ${bufferFull.values.sumOf { it.capacity() } / 1024 / 1024} MB")
        }
        cleanDistant(page)
        return bufferMap[page]!!
    }

    fun cleanDistant(currentPage: Int) {
        bufferFull.keys.removeAll {
            val remove = it !in (currentPage - BUFFER_PAGE_WINDOW)..(currentPage + BUFFER_PAGE_WINDOW)
            if (remove) {
                bufferFull[it]?.free()
            }
            remove

        }
    }

    companion object {
        const val BUFFER_PAGE_WINDOW = 2
    }
}