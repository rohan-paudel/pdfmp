package com.dshatz.pdfmp.compose

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.layout.LazyLayoutCacheWindow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.dshatz.pdfmp.ConsumerBufferPool
import com.dshatz.pdfmp.PdfRenderer
import com.dshatz.pdfmp.compose.state.rememberDocumentState
import com.dshatz.pdfmp.compose.state.rememberPageRenderState
import com.dshatz.pdfmp.source.PdfSource

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PdfPageColumn(source: PdfSource, modifier: Modifier = Modifier) {
    val renderer = remember(source) { PdfRenderer(source) }
    val bufferPool = remember(source) { ConsumerBufferPool() }
    DisposableEffect(Unit) {
        onDispose {
            renderer.close()
        }
    }
    val pageCount by produceState<Int?>(null, renderer) {
        value = renderer.getPageCount()
        println("Page count $value")
    }
    val pageRatios by produceState<List<Float>>(emptyList(), source) {
        value = renderer.getPageRatios()
    }
    BoxWithConstraints(modifier) {
        pageRatios.takeUnless { it.isEmpty() }?.let { ratios ->
            val containerWidth = constraints.maxWidth
            val density = LocalDensity.current

            // Cache LazyList items approximately one page in both direction.
            val firstPageHeight = with (density) { (containerWidth / ratios[0]).toDp() }
            val listState = rememberLazyListState(cacheWindow = LazyLayoutCacheWindow(firstPageHeight, firstPageHeight))

            val docState = rememberDocumentState()

            LazyColumn(Modifier.matchParentSize(), state = listState) {
                items(ratios.size) { pageIdx ->
                    val pageState = rememberPageRenderState(docState, pageIdx)
                    BoxWithConstraints(Modifier.fillMaxWidth()) {
                        val widthPx = constraints.maxWidth.toFloat() * docState.scale.value

                        val heightPx = kotlin.math.ceil(widthPx / ratios[pageIdx]).toInt()

                        val heightDp = with(LocalDensity.current) { heightPx.toDp() }
                        Column(Modifier.height(heightDp)) {
                            InternalPdfPage(renderer, bufferPool, pageState, pageIdx, Modifier.fillMaxSize(), scrollable = false)
                        }
                    }
                }
            }
        }
    }
}