package com.dshatz.pdfmp.compose

import androidx.compose.ui.geometry.Size
import com.dshatz.pdfmp.PdfRenderer
import com.dshatz.pdfmp.compose.state.PdfState
import com.dshatz.pdfmp.compose.state.VisiblePageInfo
import com.dshatz.pdfmp.source.PdfSource
import io.mockk.every
import io.mockk.mockk
import kotlinx.io.files.Path
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals

class DocumentStateTest {

    private fun getMockedState(
        pageRatio: Float = 1f,
        pageCount: Int = 10,
        viewportSize: Size = Size(1000f, 1500f),
    ): PdfState {
        val renderer = mockk<PdfRenderer>()
        val state = PdfState(PdfSource.PdfPath(Path("")))
        every { renderer.getPageRatios() } returns generateSequence { pageRatio }.take(pageCount).toList()
        state.initPages(renderer)

        state.isInitialized.value = true

        state.setViewport(viewportSize)
        return state
    }

    @Test
    fun `visible pages`() {
        val state = getMockedState()
        state.reportVisibleItem(0, 0)
        val visible = state.visiblePages.value

        assertContains(
            visible,
            VisiblePageInfo(
                0,
                0f,
                0f,
                1000f,
                1000f
            )
        )
        assertContains(
            visible,
            VisiblePageInfo(
                1,
                0f,
                500f,
                1000f,
                1000f
            )
        )

        assertEquals(
            10,
            state.pages.size
        )
    }

    @Test
    fun scaled() {
        val state = getMockedState()
        state.setScale(2f)
        state.reportVisibleItem(0, 0)

        // scaled 2x so width is 2000 and height is 2000 but viewport is still 1000x1500.
        // First page does not fit.
        assertEquals(
            listOf(
                VisiblePageInfo(
                    0, 0f, 500f, 2000f, 2000f
                )
            ),
            state.visiblePages.value,
        )
    }

    @Test
    fun `visible pages scrolled down`() {
        val state = getMockedState()
        // First item is taking 1000-400=600 of the viewport (900 remaining) Second is taking remaining 900 with 100 cut off.
        state.reportVisibleItem(0, 400)
        val visible = state.visiblePages.value
        assertEquals(
            listOf(
                VisiblePageInfo(0, 400f, 0f, 1000f, 1000f),
                VisiblePageInfo(1, 0f, 100f, 1000f, 1000f),
            ),
            visible
        )
    }

    @Test
    fun `last page`() {
        val state = getMockedState()
        state.reportVisibleItem(8, 0)
        val visible = state.visiblePages.value
        assertEquals(
            listOf(
                VisiblePageInfo(8, 0f, 0f, 1000f, 1000f),
                VisiblePageInfo(9, 0f, 500f, 1000f, 1000f),
            ),
            visible
        )
    }
}