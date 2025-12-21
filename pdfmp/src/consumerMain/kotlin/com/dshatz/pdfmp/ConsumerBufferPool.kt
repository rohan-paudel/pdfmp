package com.dshatz.pdfmp

import com.dshatz.pdfmp.model.PageTransform
import com.dshatz.pdfmp.model.SizeB

class ConsumerBufferPool {

    private val buffers: LinkedHashSet<ConsumerBuffer> =  linkedSetOf()
    private var bufferViewport: ConsumerBuffer? = null

    fun getBufferPage(transform: PageTransform): ConsumerBuffer {
        val neededCapacity = transform.bufferSize
        val page = transform.pageIndex
        val buffer = buffers.firstOrNull { it.isFree && it.capacity() >= neededCapacity } ?: run {
            val newBuffer = ConsumerBufferUtil.allocate(neededCapacity)
            buffers.add(newBuffer)
            d("[$page] Allocated ${neededCapacity.stringMB} ${transform.scaledWidth} x ${transform.scaledHeight}")
            d("Total buffer memory: ${totalBufferMemory.stringMB}, Unfree: ${totalUnfreeBufferMemory.stringMB}")
            newBuffer
        }
        buffer.setUnfree()
        return buffer
    }

    fun getBufferViewport(transforms: List<PageTransform>): ConsumerBuffer {
        val neededCapacity = transforms.fold(SizeB.ZERO) { total, new ->
            total + new.bufferSizeWithGap
        }

        return bufferViewport?.takeUnless { it.capacity() < neededCapacity } ?: run {
            val newBuffer = ConsumerBufferUtil.allocate(neededCapacity)
            d("Allocated viewport buffer ${neededCapacity.stringMB}")
            this.bufferViewport = newBuffer
            newBuffer
        }
    }

    private val totalBufferMemory: SizeB
        get() = buffers.fold(SizeB.ZERO) { s, buffer -> s + buffer.capacity() }

    private val totalUnfreeBufferMemory: SizeB
        get() = buffers.filter { !it.isFree }.fold(SizeB.ZERO) { s, buffer -> s + buffer.capacity() }
}