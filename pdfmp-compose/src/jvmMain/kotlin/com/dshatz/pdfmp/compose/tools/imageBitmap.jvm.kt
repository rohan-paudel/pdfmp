package com.dshatz.pdfmp.compose.tools

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import com.dshatz.pdfmp.compose.CurrentImage
import com.dshatz.pdfmp.model.calculateSize
import java.awt.image.BufferedImage
import java.awt.image.DataBufferInt

@Composable
internal actual fun CurrentImage.toImageBitmap(): RecyclableBitmap {
    val imageBitmap = remember(loadedTransforms, buffer) {
        val buffer = buffer.buffer
        buffer.rewind()
        val (width, height) = loadedTransforms.calculateSize()
        if (width == 0 || height == 0) {
            // Return an empty bitmap if dimensions are invalid
            RecyclableBitmap(ImageBitmap(1, 1), {})
        } else {
            val bufferedImage = BufferedImage(
                width,
                height,
                BufferedImage.TYPE_INT_ARGB
            )

            val targetPixels = (bufferedImage.raster.dataBuffer as DataBufferInt).data
            buffer.asIntBuffer().get(targetPixels, 0, width * height)
            RecyclableBitmap(bufferedImage.toComposeImageBitmap(), {})
        }
    }
    return imageBitmap
}