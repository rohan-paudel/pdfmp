package com.dshatz.pdfmp.compose.tools

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import com.dshatz.pdfmp.PdfRenderer
import com.dshatz.pdfmp.compose.CurrentImage
import org.jetbrains.skia.Bitmap
import org.jetbrains.skiko.toImage
import java.awt.image.BandedSampleModel
import java.awt.image.BufferedImage
import java.awt.image.ComponentSampleModel
import java.awt.image.DataBuffer
import java.awt.image.DataBufferByte
import java.awt.image.DataBufferInt
import java.awt.image.PixelInterleavedSampleModel
import java.awt.image.Raster
import java.awt.image.SampleModel

@Composable
internal actual fun CurrentImage.toImageBitmap(): RecyclableBitmap {
    val imageBitmap = remember(loadedTransform, buffer) {
        val buffer = buffer.buffer
        buffer.rewind()
        val (width, height) = loadedTransform.size()
        if (width == 0 || height == 0) {
            // Return an empty bitmap if dimensions are invalid
            RecyclableBitmap(ImageBitmap(1, 1), {})
        } else {
            // 1. Create a Java AWT BufferedImage
            val bufferedImage = BufferedImage(
                width,
                height,
                BufferedImage.TYPE_INT_BGR
            )

            val targetPixels = (bufferedImage.raster.dataBuffer as DataBufferInt).data
            buffer.asIntBuffer().get(targetPixels, 0, width * height)
            RecyclableBitmap(bufferedImage.toComposeImageBitmap(), {})
        }
    }
    return imageBitmap
}