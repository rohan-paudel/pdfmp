package com.dshatz.pdfmp.compose

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import com.dshatz.pdfmp.PdfRenderer
import com.dshatz.pdfmp.compose.state.PdfState
import com.dshatz.pdfmp.compose.state.VisiblePageInfo
import com.dshatz.pdfmp.source.PdfSource
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.io.files.Path
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals

class DocumentStateTest {

    private fun getMockedState(
        pageRatio: Float = 1f,
        pageCount: Int = 10,
        gap: Int = 0,
        viewportSize: Size = Size(1000f, 1500f),
    ): PdfState {
        val scope = CoroutineScope(Dispatchers.Default)
        val renderer = mockk<PdfRenderer>()
        val state = PdfState(PdfSource.PdfPath(Path("")), pageSpacing = gap, scope = scope)
        every { renderer.getPageRatios() } returns generateSequence { pageRatio }.take(pageCount).toList()
        state.initPages(renderer)

        state.isInitialized.value = true

        state.setViewport(viewportSize)
        return state
    }

    @Test
    fun `visible pages`() {
        val state = getMockedState()
//        state.onScroll(Offset(0f, 0f))
        val visible = state.visiblePages.value

        assertContains(
            visible,
            VisiblePageInfo(
                0,
                0f,
                0f,
                0,
                0,
                scaledWidth = 1000f,
                scaledHeight = 1000f
            )
        )
        assertContains(
            visible,
            VisiblePageInfo(
                1,
                0f,
                500f,
                0,
                0,
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
    fun `visible pages wih gap`() {
        val state = getMockedState(gap = 100)
//        state.onScroll(Offset(0f, 0f))
        val visible = state.visiblePages.value

        assertContains(
            visible,
            VisiblePageInfo(
                0,
                0f,
                0f,
                0,
                0,
                scaledWidth = 1000f,
                scaledHeight = 1000f,
            )
        )
        // Offset from top of the document for page1. 1000px (fist page) + 100px (gap) = 1100px
        // Viewport height = 1500px
        // Bottom cutoff page1 = page height - available space = 1000px - (1500-1100) = 1000 - 400 = 600px
        assertContains(
            visible,
            VisiblePageInfo(
                1,
                0f,
                600f,
                0,
                0,
                1000f,
                1000f,
                topGap = 100
            )
        )

        assertEquals(
            10,
            state.pages.size
        )
    }

    @Test
    fun `visible pages wih gap scrolled`() {
        val state = getMockedState(gap = 100, viewportSize = Size(1000f, 1000f))
        // scroll by 100 - viewport bottom is at end of gap
        state.onScroll(Offset(0f, -100f))

        assertEquals(
            state.visiblePages.value.single(),
            VisiblePageInfo(
                0,
                100f,
                0f,
                0,
                0,
                scaledWidth = 1000f,
                scaledHeight = 1000f,
            )
        )

        // scroll by 1 - page 2 appears.
        state.onScroll(Offset(0f, -1f))

        assertEquals(
            2,
            state.visiblePages.value.size
        )

        assertEquals(
            state.visiblePages.value.first(),
            VisiblePageInfo(
                0,
                101f,
                0f,
                0,
                0,
                scaledWidth = 1000f,
                scaledHeight = 1000f,
            )
        )

        assertEquals(
            state.visiblePages.value[1],
            VisiblePageInfo(
                1,
                0f,
                999f, // just top 1px visible
                0,
                0,
                scaledWidth = 1000f,
                scaledHeight = 1000f,
                topGap = 100
            )
        )
    }

    @Test
    fun scaled() {
        val state = getMockedState(gap = 100, viewportSize = Size(1000f, 2500f))
        state.zoom(2f, Offset.Zero)
        // Zoom with mouse at 0,0

        // scaled 2x so width is 2000 and height is 2000 but viewport is still 1000x2500.
        // First page does not fit.
        assertEquals(
            listOf(
                VisiblePageInfo(
                    0, 0f, 0f, 0, 1000, 2000f, 2000f
                ),
                VisiblePageInfo(
                    1, 0f, 1700f, 0, 1000, 2000f, 2000f, topGap = 200
                )
            ),
            state.visiblePages.value,
        )
    }

    @Test
    fun `visible pages scrolled down`() {
        val state = getMockedState()
        // First item is taking 1000-400=600 of the viewport (900 remaining) Second is taking remaining 900 with 100 cut off.
        state.onScroll(Offset(0f, -400f))
        val visible = state.visiblePages.value
        assertEquals(
            listOf(
                VisiblePageInfo(0, 400f, 0f, 0, 0, 1000f, 1000f),
                VisiblePageInfo(1, 0f, 100f, 0, 0, 1000f, 1000f),
            ),
            visible
        )
    }

    @Test
    fun `last page`() {
        val state = getMockedState()
        state.onScroll(Offset(0f, -Float.MAX_VALUE))
        val visible = state.visiblePages.value
        assertEquals(
            listOf(
                VisiblePageInfo(8, 500f, 0f, 0, 0, 1000f, 1000f),
                VisiblePageInfo(9, 0f, 0f, 0, 0, 1000f, 1000f),
            ),
            visible
        )
    }
}