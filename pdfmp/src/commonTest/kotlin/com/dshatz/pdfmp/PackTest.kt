package com.dshatz.pdfmp

import com.dshatz.pdfmp.model.PageTransform
import com.dshatz.pdfmp.model.RenderRequest
import kotlinx.io.Buffer
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals

class PackTest {

    private fun randomPageTransform(): PageTransform {
        return PageTransform(
            Random.nextInt(),
            Random.nextInt(),
            Random.nextInt(),
            Random.nextInt(),
            Random.nextInt(),
            Random.nextInt(),
            Random.nextInt(),
            Random.nextInt(),
            Random.nextFloat()
        )
    }

    @Test
    fun `pack page transform`() {
        val input = randomPageTransform()
        val buffer = Buffer()
        input.pack(buffer)

        assertEquals(
            input,
            PageTransform.unpack(buffer.copy())
        )
    }

    @Test
    fun `pack render request`() {
        val input = RenderRequest(
            transforms = generateSequence { randomPageTransform() }.take(Random.nextInt(10)).toList(),
            0,
            0,
            bufferAddress = Random.nextLong()
        )
        val bytes = input.pack()
        assertEquals(
            input,
            RenderRequest.unpack(bytes)
        )
    }
}