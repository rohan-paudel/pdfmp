package com.dshatz.pdfmp.compose.state

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.DpSize
import com.dshatz.pdfmp.ConsumerBuffer
import com.dshatz.pdfmp.ConsumerBufferPool
import com.dshatz.pdfmp.model.PageTransform
import com.dshatz.pdfmp.PdfRenderer
import com.dshatz.pdfmp.model.RenderRequest
import com.dshatz.pdfmp.model.RenderResponse
import com.dshatz.pdfmp.source.PdfSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@Composable
fun rememberPdfState(pdfSource: PdfSource): PdfState {
    val scope = rememberCoroutineScope()
    val state = remember { PdfState(pdfSource, scope = scope) }
    state.OpenDisposableDocument()
    return state
}

@OptIn(ExperimentalTime::class)
data class PdfState(
    val pdfSource: PdfSource,
    val scale: MutableState<Float> = mutableFloatStateOf(1f),
    val viewport: MutableState<Size> = mutableStateOf(Size(1f, 1f)),
    val scope: CoroutineScope
) {
    lateinit var renderer: PdfRenderer
    lateinit var listState: LazyListState
    lateinit var horizontalScrollState: ScrollState

    private lateinit var bufferPool: ConsumerBufferPool
    lateinit var pages: List<PdfPageState>

    private val renderingY = mutableFloatStateOf(0f)
    private val renderingX = mutableFloatStateOf(0f)
    private var lastGestureTime = 0L

    fun bind(
        listState: LazyListState,
        horizontalScrollState: ScrollState,
    ) {
        this.listState = listState
        this.horizontalScrollState = horizontalScrollState
    }

    internal val visiblePages: State<List<VisiblePageInfo>> = derivedStateOf<List<VisiblePageInfo>> {
        calculateVisiblePages()
    }

    private fun scaledPageWidth(viewport: MutableState<Size>, scale: State<Float>): Float {
        return viewport.value.width * scale.value
    }

    private fun scaledPageHeight(pageIdx: Int, scaledWidth: Float = scaledPageWidth(viewport, scale)): Float {
        return scaledWidth / pages[pageIdx].aspectRatio
    }

    @Composable
    internal fun rememberScaledPageWidth(page: Int): State<Float> {
        return remember(page, scale.value, viewport.value) {
            derivedStateOf {
                scaledPageWidth(viewport, scale)
            }
        }
    }

    @Composable
    internal fun rememberScaledPageHeight(page: Int): State<Float> {
        val scaledWidth by rememberScaledPageWidth(page)
        return remember(page, scaledWidth) {
            derivedStateOf {
                scaledPageHeight(page, scaledWidth)
            }
        }
    }

    @Composable
    fun rememberScaledPageSize(page: Int): State<DpSize> {
        val density = LocalDensity.current
        val width by rememberScaledPageWidth(page)
        val height by rememberScaledPageHeight(page)
        return derivedStateOf {
            with(density) {
                DpSize(
                    width.toDp(),
                    height.toDp()
                )
            }
        }
    }

    private val horizontalScrollOffset = mutableStateOf(0)

    fun reportHorizontalOffset(offset: Int) {
        horizontalScrollOffset.value = offset
    }

    fun syncFromLayout(index: Int, offset: Int, scrollX: Int) {
        if (Clock.System.now().toEpochMilliseconds() - lastGestureTime > 200) {
            val absoluteY = calculateAbsoluteYFromIndex(index, offset)
            renderingY.floatValue = absoluteY
            renderingX.floatValue = scrollX.toFloat()
        }
    }

    fun onScroll(delta: Offset): Offset {
        lastGestureTime = Clock.System.now().toEpochMilliseconds()

        val currentX = renderingX.floatValue
        val currentY = renderingY.floatValue

        val maxX = (viewport.value.width * scale.value - viewport.value.width).coerceAtLeast(0f)
        val newX = (currentX - delta.x).coerceIn(0f, maxX)
        val newY = (currentY - delta.y).coerceAtLeast(0f)

        renderingX.floatValue = newX
        renderingY.floatValue = newY
        reportHorizontalOffset(newX.toInt())

        updateUiScrollPosition(newX, newY, scale.value)

        return delta
    }

    fun zoom(zoomFactor: Float, centroid: Offset) {
        lastGestureTime = Clock.System.now().toEpochMilliseconds()

        val currentScale = scale.value
        val newScale = (currentScale * zoomFactor).coerceIn(1f, 5.0f)
        if (currentScale == newScale) return

        val scalingRatio = newScale / currentScale

        val mouseAbsY = renderingY.floatValue + centroid.y
        val newRenderingY = (mouseAbsY * scalingRatio) - centroid.y

        val mouseAbsX = renderingX.floatValue + centroid.x
        val newRenderingX = (mouseAbsX * scalingRatio) - centroid.x

        scale.value = newScale
        renderingY.floatValue = newRenderingY.coerceAtLeast(0f)
        renderingX.floatValue = newRenderingX.coerceAtLeast(0f)
        reportHorizontalOffset(newRenderingX.toInt())

        updateUiScrollPosition(newRenderingX.coerceAtLeast(0f), newRenderingY.coerceAtLeast(0f), newScale)
    }

    private fun updateUiScrollPosition(x: Float, y: Float, s: Float) {
        val (targetIndex, targetOffset) = getPageAndOffsetForAbsoluteY(y, s)
        scope.launch {
            listState.scrollToItem(targetIndex, targetOffset)
            horizontalScrollState.scrollTo(x.toInt())
        }
    }

    private fun calculateVisiblePages(): List<VisiblePageInfo> {
        if (!isInitialized.value || pages.isEmpty()) return emptyList()

        val currentY = renderingY.floatValue
        val currentX = renderingX.floatValue
        val viewportHeight = viewport.value.height
        val viewportWidth = viewport.value.width
        val currentScale = scale.value
        val currentScaledWidth = viewportWidth * currentScale

        val visiblePages = mutableListOf<VisiblePageInfo>()
        var accumulatedHeight = 0f

        for (i in pages.indices) {
            val pageHeight = scaledPageHeight(i, currentScaledWidth)
            val pageTop = accumulatedHeight
            val pageBottom = pageTop + pageHeight

            if (pageBottom > currentY && pageTop < currentY + viewportHeight) {
                val topCutoff = (currentY - pageTop).coerceAtLeast(0f)
                val bottomCutoff = (pageBottom - (currentY + viewportHeight)).coerceAtLeast(0f)
                val leftCutoff = currentX.coerceIn(0f, currentScaledWidth)
                val rightCutoff = (currentScaledWidth - (currentX + viewportWidth)).coerceAtLeast(0f)

                visiblePages.add(VisiblePageInfo(
                    i, topCutoff, bottomCutoff, leftCutoff.toInt(), rightCutoff.toInt(),
                    currentScaledWidth, pageHeight
                ))
            }
            accumulatedHeight += pageHeight
            if (accumulatedHeight > currentY + viewportHeight) break
        }
        return visiblePages
    }

    private fun calculateAbsoluteYFromIndex(index: Int, offset: Int): Float {
        var y = 0f
        val currentScaledWidth = viewport.value.width * scale.value
        for (i in 0 until index) {
            if (i < pages.size) y += scaledPageHeight(i, currentScaledWidth)
        }
        return y + offset
    }

    private fun getPageAndOffsetForAbsoluteY(absY: Float, s: Float): Pair<Int, Int> {
        var acc = 0f
        val w = viewport.value.width * s
        for(i in pages.indices) {
            val h = scaledPageHeight(i, w)
            if (acc + h > absY) return i to (absY - acc).toInt()
            acc += h
        }
        return 0 to 0
    }

    fun renderViewport(transforms: List<PageTransform>): Pair<RenderResponse, ConsumerBuffer> {
        val buffer = bufferPool.getBufferViewport(transforms)
        return buffer.withAddress {
            val response = renderer.render(
                RenderRequest(
                    transforms,
                    it
                )
            )
            response to buffer
        }
    }

    fun renderFullPage(transform: PageTransform): Pair<RenderResponse, ConsumerBuffer> {
        val buffer = bufferPool.getBufferPage(transform)
        return buffer.withAddress {
            val response = renderer.render(
                RenderRequest(
                    listOf(transform),
                    it
                )
            )
            response to buffer
        }
    }

    @Composable
    fun produceImageTransforms(): State<List<PageTransform>> {
        return derivedStateOf {
            visiblePages.value.map {
                PageTransform(
                    pageIndex = it.page,
                    topCutoff = it.topCutoff.toInt(),
                    bottomCutoff = it.bottomCutoff.toInt(),
                    leftCutoff = it.leftCutoff,
                    rightCutoff = it.rightCutoff,
                    scaledWidth = it.scaledWidth.toInt(),
                    scaledHeight = it.scaledHeight.toInt(),
                    scale = scale.value
                )
            }
        }
    }

    val isInitialized: MutableState<Boolean> = mutableStateOf(false)

    fun initPages(renderer: PdfRenderer) {
        pages = renderer.getPageRatios().mapIndexed { pageIdx, ratio ->
            PdfPageState(
                pageIdx,
                aspectRatio = ratio
            )
        }
    }
    @Composable
    fun OpenDisposableDocument() {
        DisposableEffect(pdfSource) {
            renderer = PdfRenderer(pdfSource)
            bufferPool = ConsumerBufferPool()
            initPages(renderer)
            isInitialized.value = true
            onDispose {
                isInitialized.value = false
                renderer.close()
            }
        }
    }

    fun setScale(value: Float) {
        scale.value = value
    }

    fun setViewport(size: Size) {
        viewport.value = size
    }
}

data class VisiblePageInfo(
    val page: Int,
    val topCutoff: Float,
    val bottomCutoff: Float,
    val leftCutoff: Int,
    val rightCutoff: Int,
    val scaledWidth: Float,
    val scaledHeight: Float
) {
    init {
        if (scaledHeight.toInt() - topCutoff.toInt() - bottomCutoff.toInt() < 0) error("Invalid parameters $this")
    }
}

/*
@Composable
fun rememberPageRenderState(docState: PdfDocumentState, key: Any = ""): PdfPageState {
    val offset = remember { mutableFloatStateOf(0f) }
    return remember(key, docState) {
        PdfPageState(
            docState = docState,
            verticalOffset = offset,
        )
    }
}*/
