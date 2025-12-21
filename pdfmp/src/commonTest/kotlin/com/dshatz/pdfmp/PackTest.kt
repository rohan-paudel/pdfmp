package com.dshatz.pdfmp

import com.dshatz.pdfmp.model.PageTransform
import com.dshatz.pdfmp.model.RenderRequest
import kotlinx.io.Buffer
import kotlinx.io.readByteArray
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.fail

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

    @Test
    fun `pack failure`() {
        val result = Result.failure<Unit>(RuntimeException("native message"))
        val buffer = Buffer()
        result.pack(buffer, {})

        val exception = assertFails {
            unpackResultOrThrow(buffer.readByteArray(), {})
        }
        assertContains(exception.message.orEmpty(), "native message")
    }

    @Test
    fun `pack success`() {
        val result = Result.success<Int>(999)
        val buffer = Buffer()
        result.pack(buffer, Buffer::writeInt)

        assertEquals(999, unpackResultOrThrow(buffer.readByteArray(), Buffer::readInt))
    }
}