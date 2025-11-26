package com.dshatz.pdfmp

import com.dshatz.pdfmp.model.PageTransform
import com.dshatz.pdfmp.model.SizeB

class ConsumerBufferPool {
    private val bufferFullPage: MutableMap<Int, ConsumerBuffer> = mutableMapOf()
    private var bufferViewport: ConsumerBuffer? = null

    fun getBufferPage(transform: PageTransform): ConsumerBuffer {
        val neededCapacity = transform.bufferSize
        val page = transform.pageIndex
        val buffer = bufferFullPage[page]
        if (buffer == null || buffer.capacity() < neededCapacity) {
            bufferFullPage[page] = ConsumerBufferUtil.allocate(neededCapacity)
            println("[$page] Allocated ${neededCapacity.stringMB} ${transform.scaledWidth} x ${transform.scaledHeight}")
            println("Total buffer memory: ${totalBufferMemory.stringMB}")
        }
        cleanDistant(page)
        return bufferFullPage[page]!!
    }

    fun getBufferViewport(transforms: List<PageTransform>): ConsumerBuffer {
        val neededCapacity = transforms.fold(SizeB.ZERO) { total, new ->
            total + new.bufferSize
        }

        return bufferViewport?.takeUnless { it.capacity() < neededCapacity } ?: run {
            val newBuffer = ConsumerBufferUtil.allocate(neededCapacity)
            println("Allocated viewport buffer ${neededCapacity.stringMB}")
            this.bufferViewport = newBuffer
            newBuffer
        }
    }

    private val totalBufferMemory: SizeB
        get() {
            val fullPageBuffers = bufferFullPage.values.fold(SizeB.ZERO) { total, new -> total + new.capacity() }
            return fullPageBuffers + (bufferViewport?.capacity() ?: SizeB.ZERO)
    }


    fun cleanDistant(currentPage: Int) {
        bufferFullPage.keys.removeAll {
            val remove = it !in (currentPage - BUFFER_PAGE_WINDOW)..(currentPage + BUFFER_PAGE_WINDOW)
            if (remove) {
                bufferFullPage[it]?.free()
            }
            remove

        }
    }

    companion object {
        const val BUFFER_PAGE_WINDOW = 2
    }
}