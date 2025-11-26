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
            println("[$page] Allocated ${neededCapacity.stringMB} ${transform.scaledWidth} x ${transform.scaledHeight}")
            println("Total buffer memory: ${totalBufferMemory.stringMB}, Unfree: ${totalUnfreeBufferMemory.stringMB}")
            newBuffer
        }
        buffer.setUnfree()
//        cleanDistant(page)
        return buffer
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
        get() = buffers.fold(SizeB.ZERO) { s, buffer -> s + buffer.capacity() }

    private val totalUnfreeBufferMemory: SizeB
        get() = buffers.filter { !it.isFree }.fold(SizeB.ZERO) { s, buffer -> s + buffer.capacity() }


    /*fun cleanDistant(currentPage: Int) {
        bufferFullPage.keys.removeAll {
            val remove = it !in (currentPage - BUFFER_PAGE_WINDOW)..(currentPage + BUFFER_PAGE_WINDOW)
            if (remove) {
                bufferFullPage[it]?.free()
            }
            remove

        }
    }*/

    companion object {
        const val BUFFER_PAGE_WINDOW = 2
    }
}