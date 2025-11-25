package com.dshatz.pdfmp.compose

import androidx.compose.foundation.Image
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.toSize
import androidx.compose.ui.zIndex
import com.dshatz.pdfmp.compose.state.PdfState
import com.dshatz.pdfmp.compose.tools.PartialBitmapRenderer
import com.dshatz.pdfmp.compose.tools.pageTransformModifier
import com.dshatz.pdfmp.compose.tools.platformPageTransformModifier
import com.dshatz.pdfmp.compose.tools.toImageBitmap
import kotlinx.coroutines.delay

@Composable
fun PdfView(
    state: PdfState,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()
    val horizontalScroll = rememberScrollState()

    DisposableEffect(Unit) {
        state.bind(listState, horizontalScroll)
        onDispose { /*todo*/ }
    }

    LaunchedEffect(listState, horizontalScroll) {
        snapshotFlow {
            Triple(
                listState.firstVisibleItemIndex,
                listState.firstVisibleItemScrollOffset,
                horizontalScroll.value
            )
        }.collect { (idx, off, hVal) ->
            state.syncFromLayout(idx, off, hVal)
        }
    }
    Box(
        modifier = modifier.onGloballyPositioned {
            // Report real viewport size.
            state.setViewport(it.size.toSize())
        }.pageTransformModifier(state)
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier.matchParentSize().horizontalScroll(horizontalScroll)
        ) {
            FullDocumentBoxes(state)
        }
        BaseImage(
            state,
            modifier = Modifier.matchParentSize()
        )
        PdfViewport(
            state,
            modifier = Modifier.matchParentSize()
        )
    }
}


private fun LazyListScope.FullDocumentBoxes(state: PdfState) {
    items(state.pages.size) { pageIdx ->
        val size by state.rememberScaledPageSize(pageIdx)
        Box(Modifier.requiredSize(size), contentAlignment = Alignment.Center) {
            Text(
                pageIdx.toString(),
                fontSize = 60.sp,
                color = Color.Gray,
                modifier = Modifier.alpha(0.5f).zIndex(100f)
            )
        }
    }
}

@Composable
private fun PdfViewport(
    state: PdfState,
    modifier: Modifier = Modifier,
) {
    val transforms by state.produceImageTransforms()
    val images by produceState<List<CurrentImage>?>(null, transforms) {
        delay(1000)
        value = transforms.map { (page, transform) ->
            val (response, buffer) = state.render(page, transform)
            CurrentImage(
                requestedTransform = transform,
                loadedTransform = response.transform,
                buffer = buffer
            )
        }
    }


    LaunchedEffect(images) {
//        println("Image count: ${images?.size}")
//        println("Image transforms: ${images?.joinToString("\n") { it.loadedTransform.toString() }}")
    }
    if (transforms.values.toList() == images?.map { it.loadedTransform }) {
        Column(modifier) {
            images?.forEach {
                Image(
                    contentScale = ContentScale.FillBounds,
                    bitmap = it.toImageBitmap().imageBitmap,
                    contentDescription = null
                )
            }
        }
    }
}

@Composable
private fun BaseImage(
    state: PdfState,
    modifier: Modifier = Modifier,
) {
    val transforms by state.produceImageTransforms()
    val baseImages by produceState<List<CurrentImage>?>(null, transforms.keys) {
        value = transforms.map { (page, transform) ->
            val fullPageTransform = transform.uncut().copy(scale = 1f)
            val (response, buffer) = state.render(page, fullPageTransform)
            CurrentImage(
                requestedTransform = fullPageTransform,
                loadedTransform = response.transform,
                buffer = buffer
            )
        }
    }





    Column(modifier) {
        baseImages?.forEachIndexed { idx, image ->
            val image = image.toImageBitmap()
            DisposableEffect(image) {
                onDispose {
                    image.onRecycle()
                }
            }

            transforms[idx]?.let { originalTransform ->
                val size = Size(
                    (originalTransform.scaledWidth - originalTransform.leftCutoff - originalTransform.rightCutoff).toFloat(),
                    (originalTransform.scaledHeight - originalTransform.topCutoff - originalTransform.bottomCutoff).toFloat()
                )
                PartialBitmapRenderer(
                    image.imageBitmap,
                    srcOffset = IntOffset(originalTransform.leftCutoff, originalTransform.topCutoff),
                    scale = originalTransform.scale,
                    dstSize = size,
                    modifier = Modifier.requiredSize(with (LocalDensity.current) { size.toDpSize() })
                )
            }
            /*Image(
                contentScale = ContentScale.None,
                alignment = Alignment.TopCenter,
                bitmap = it.toImageBitmap().imageBitmap,
                contentDescription = null
            )*/
        }
    }
}