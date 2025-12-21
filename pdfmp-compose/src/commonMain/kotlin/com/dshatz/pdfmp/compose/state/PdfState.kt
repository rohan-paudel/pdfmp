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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.dshatz.pdfmp.ConsumerBuffer
import com.dshatz.pdfmp.ConsumerBufferPool
import com.dshatz.pdfmp.InitLib
import com.dshatz.pdfmp.model.PageTransform
import com.dshatz.pdfmp.PdfRenderer
import com.dshatz.pdfmp.e
import com.dshatz.pdfmp.model.RenderRequest
import com.dshatz.pdfmp.model.RenderResponse
import com.dshatz.pdfmp.source.PdfSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.math.min
import kotlin.time.ExperimentalTime

@Composable
fun rememberPdfState(
    pdfSource: PdfSource,
    pageRange: IntRange = 0..Int.MAX_VALUE,
    pageSpacing: Dp = 0.dp
): PdfState {
    val scope = rememberCoroutineScope()
    val pageSpacingPx = with (LocalDensity.current) { pageSpacing.toPx().toInt() }
    val state = remember { PdfState(pdfSource, pageRange = pageRange, pageSpacing = pageSpacingPx, scope = scope) }
    state.OpenDisposableDocument()
    return state
}

@OptIn(ExperimentalTime::class)
data class PdfState(
    val pdfSource: PdfSource,
    val pageRange: IntRange = 0..Int.MAX_VALUE,
    val scale: MutableState<Float> = mutableFloatStateOf(1f),
    val viewport: MutableState<Size> = mutableStateOf(Size(1f, 1f)),
    val pageSpacing: Int = 0,
    val scope: CoroutineScope
) {
    private lateinit var renderer: PdfRenderer
    private lateinit var listState: LazyListState
    private lateinit var horizontalScrollState: ScrollState

    private lateinit var bufferPool: ConsumerBufferPool
    internal lateinit var pages: LinkedHashMap<Int, PdfPageState>

    private val renderingY = mutableFloatStateOf(0f)
    private val renderingX = mutableFloatStateOf(0f)

    private val _displayState = mutableStateOf<DisplayState>(DisplayState.Idle)
    val displayState: State<DisplayState> = _displayState
    internal val isReady = derivedStateOf {
        _displayState.value is DisplayState.Ready || _displayState.value is DisplayState.Displaying
    }

    internal fun bind(
        listState: LazyListState,
        horizontalScrollState: ScrollState,
    ) {
        this.listState = listState
        this.horizontalScrollState = horizontalScrollState
    }

    internal val visiblePages: State<List<VisiblePageInfo>> = derivedStateOf<List<VisiblePageInfo>> {
        calculateVisiblePages().also {
            _displayState.value = DisplayState.Displaying(it.map { page -> page.page })
        }
    }

    private fun scaledPageWidth(viewport: MutableState<Size>, scale: State<Float>): Float {
        return viewport.value.width * scale.value
    }

    private fun scaledPageHeight(pageIdx: Int, scaledWidth: Float = scaledPageWidth(viewport, scale)): Float {
        return scaledWidth / (pages[pageIdx]!!.aspectRatio)
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
    internal fun rememberScaledPageSize(page: Int): State<DpSize> {
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

    internal fun reportHorizontalOffset(offset: Int) {
        horizontalScrollOffset.value = offset
    }

    internal fun onScroll(delta: Offset): Offset {
        val currentX = renderingX.floatValue
        val currentY = renderingY.floatValue

        val maxX = (viewport.value.width * scale.value - viewport.value.width).coerceAtLeast(0f)
        var totalContentHeight = 0f
        for (i in pages.keys) {
            totalContentHeight += scaledPageHeight(i)
        }

        //Account for page spacings
        totalContentHeight += scaledPageSpacing() * (pages.size - 1)

        val maxY = (totalContentHeight - viewport.value.height).coerceAtLeast(0f)
        val newX = (currentX - delta.x).coerceIn(0f, maxX)
        val newY = (currentY - delta.y).coerceIn(0f, maxY)

        renderingX.floatValue = newX
        renderingY.floatValue = newY
        reportHorizontalOffset(newX.toInt())

        updateUiScrollPosition(newX, newY, scale.value)

        return delta
    }

    internal fun zoom(zoomFactor: Float, centroid: Offset) {
        val currentScale = scale.value
        val newScale = (currentScale * zoomFactor).coerceIn(1f, 5.0f)
        if (currentScale == newScale) return

        val scalingRatio = newScale / currentScale

        val mouseAbsY = renderingY.floatValue + centroid.y
        val newRenderingY = (mouseAbsY * scalingRatio) - centroid.y

        val mouseAbsX = renderingX.floatValue + centroid.x
        val newRenderingX = if (newScale == 1f) 0f else (mouseAbsX * scalingRatio) - centroid.x

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
        if (!this::pages.isInitialized || pages.isEmpty()) return emptyList()

        val currentY = renderingY.floatValue
        val currentX = renderingX.floatValue
        val viewportHeight = viewport.value.height
        val viewportWidth = viewport.value.width
        val currentScale = scale.value
        val currentScaledWidth = viewportWidth * currentScale

        val verticalSpacing = scaledPageSpacing()

        val visiblePages = mutableListOf<VisiblePageInfo>()
        var accumulatedHeight = 0f

        for ((i, _) in pages) {
            val pageHeight = scaledPageHeight(i, currentScaledWidth)
            val pageTop = accumulatedHeight
            val pageBottom = pageTop + pageHeight

            if (pageBottom > currentY && pageTop < currentY + viewportHeight) {
                val topCutoff = (currentY - pageTop).coerceAtLeast(0f)
                val bottomCutoff = (pageBottom - (currentY + viewportHeight)).coerceAtLeast(0f)
                val leftCutoff = currentX.coerceIn(0f, currentScaledWidth)
                val rightCutoff = (currentScaledWidth - (currentX + viewportWidth)).coerceAtLeast(0f)

                val topGap = if (visiblePages.isEmpty()) (pageTop - currentY).coerceAtLeast(0f).toInt()
                else verticalSpacing

                visiblePages.add(VisiblePageInfo(
                    page = i,
                    topCutoff = topCutoff,
                    bottomCutoff = bottomCutoff,
                    leftCutoff = leftCutoff.toInt(),
                    rightCutoff = rightCutoff.toInt(),
                    scaledWidth = currentScaledWidth,
                    scaledHeight = pageHeight,
                    topGap = topGap
                ))
            }
            accumulatedHeight += pageHeight + verticalSpacing
            if (accumulatedHeight > currentY + viewportHeight) break
        }
        return visiblePages
    }

    private fun getPageAndOffsetForAbsoluteY(absY: Float, s: Float): Pair<Int, Int> {
        var acc = 0f
        val w = viewport.value.width * s
        val spacing = pageSpacing.toFloat()

        for (i in pages.keys) {
            val h = scaledPageHeight(i, w)
            if (acc + h + spacing > absY) {
                val offset = (absY - acc).toInt()
                return i to minOf(offset, h.toInt())
            }
            acc += h + spacing
        }
        return 0 to 0
    }

    internal fun scaledPageSpacing(): Int {
        return (pageSpacing * scale.value).toInt()
    }

    internal fun renderViewport(transforms: List<PageTransform>): Pair<RenderResponse, ConsumerBuffer>? {
        // We can just grab the offset of the first visible page.
        // Subsequent pages are automatically spaced by the native renderer.
        val topOffset = visiblePages.value.firstOrNull()?.topGap ?: 0

        val buffer = bufferPool.getBufferViewport(transforms)
        return buffer.withAddress {
            val response = renderer.render(
                RenderRequest(
                    transforms,
                    pageSpacing,
                    topOffset,
                    it
                )
            )
            response.map { resp ->
                resp to buffer
            }.onFailure { error ->
                _displayState.value = DisplayState.Error(error)
                e("Failed to render viewport", error)
            }.getOrNull()
        }
    }

    internal fun renderFullPage(transform: PageTransform): Pair<RenderResponse, ConsumerBuffer>? {
        val buffer = bufferPool.getBufferPage(transform)
        return buffer.withAddress {
            val response = renderer.render(
                RenderRequest(
                    listOf(transform),
                    0,
                    0,
                    it,
                )
            )
            response.map { resp ->
                resp to buffer
            }.onFailure { error ->
                _displayState.value = DisplayState.Error(error)
                e("Failed to render pages", error)
            }.getOrNull()
        }
    }

    @Composable
    internal fun produceImageTransforms(): State<List<PageTransform>> {
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
                    topGap = it.topGap,
                    scale = scale.value
                )
            }
        }
    }

    fun initPages(renderer: PdfRenderer): Result<Unit> {
        return renderer.getPageRatios().mapCatching { allRatios ->
            val truncated = allRatios.withIndex()
                .drop(pageRange.first).take(min(pageRange.last - pageRange.first, allRatios.size - pageRange.first) + 1)
            val map = linkedMapOf<Int, PdfPageState>()
            truncated.forEach { (pageIdx, ratio) ->
                map[pageIdx] = PdfPageState(
                    pageIdx,
                    aspectRatio = ratio
                )
            }
            pages = map
        }
    }
    @Composable
    fun OpenDisposableDocument() {
        _displayState.value = DisplayState.Initializing
        InitLib().init()
        DisposableEffect(pdfSource) {
            renderer = PdfRenderer(pdfSource)
            bufferPool = ConsumerBufferPool()
            val initResult = initPages(renderer)
            (initResult.exceptionOrNull()?.let(DisplayState::Error) ?: DisplayState.Ready).let { _displayState.value = it }
            onDispose {
                _displayState.value = DisplayState.Idle
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
    val scaledHeight: Float,
    /**
     * distance from viewport top (0 if page overlaps top edge)
     */
    val topGap: Int = 0
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
