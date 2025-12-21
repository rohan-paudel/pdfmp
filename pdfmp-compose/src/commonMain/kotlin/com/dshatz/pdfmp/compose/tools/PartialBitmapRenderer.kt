package com.dshatz.pdfmp.compose.tools

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.toIntSize
import com.dshatz.pdfmp.model.PageTransform
import kotlin.math.min

/*
@Composable
fun PartialBitmapRenderer(
    bitmap: ImageBitmap,
    srcOffset: IntOffset,
    scale: Float,
    dstSize: Size,
    modifier: Modifier = Modifier,
) {
    val imageBitmapSize = IntSize(bitmap.width, bitmap.height)

    Canvas(modifier = modifier) {

        val srcWidth = (size.width / scale).toInt()
        val srcHeight = (size.height / scale).toInt()


        // Ensure the source area doesn't exceed the bitmap bounds
        val finalSrcSize = IntSize(
            width = min(srcWidth, imageBitmapSize.width - srcOffset.x),
            height = min(srcHeight, imageBitmapSize.height - srcOffset.y)
        )


        // Draw the cropped/scaled image
        drawImage(
            image = bitmap,
            srcOffset = srcOffset,
            srcSize = finalSrcSize,
            dstOffset = IntOffset.Zero,
            dstSize = dstSize.toIntSize()
        )
    }
}
*/


@Composable
fun TransformedBitmapRenderer(
    bitmap: ImageBitmap,
    transform: PageTransform,
    colorFilter: ColorFilter? = null,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val srcX = (transform.leftCutoff / transform.scale).toInt()
        val srcY = (transform.topCutoff / transform.scale).toInt()

        val (scaledSliceW, scaledSliceH) = transform.sliceSize()
        val srcWidth = (scaledSliceW / transform.scale).toInt()
        val srcHeight = (scaledSliceH / transform.scale).toInt()

        // Safety check to prevent crashing if rounding errors go 1px out of bounds
        val safeSrcWidth = srcWidth.coerceAtMost(bitmap.width - srcX)
        val safeSrcHeight = srcHeight.coerceAtMost(bitmap.height - srcY)

        if (safeSrcWidth > 0 && safeSrcHeight > 0) {
            drawImage(
                image = bitmap,
                colorFilter = colorFilter,
                srcOffset = IntOffset(srcX, srcY),
                srcSize = IntSize(safeSrcWidth, safeSrcHeight),
//                dstOffset = IntOffset(0, transform.topGap),
                dstSize = IntSize(size.width.toInt(), size.height.toInt()),
                filterQuality = FilterQuality.Medium
            )
        }
    }
}