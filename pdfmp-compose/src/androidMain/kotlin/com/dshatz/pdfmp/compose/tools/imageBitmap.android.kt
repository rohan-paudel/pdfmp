package com.dshatz.pdfmp.compose.tools

import android.graphics.Bitmap
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.ColorMatrixColorFilter
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import com.dshatz.pdfmp.compose.CurrentImage
import com.dshatz.pdfmp.model.calculateSize


private val BgrToRgbMatrix = ColorMatrix(
    floatArrayOf(
        0f, 0f, 1f, 0f, 0f,
        0f, 1f, 0f, 0f, 0f,
        1f, 0f, 0f, 0f, 0f,
        0f, 0f, 0f, 1f, 0f
    )
)

@Composable
internal actual fun CurrentImage.toImageBitmap(): RecyclableBitmap {
    val imageBitmap = remember(this.loadedTransforms, buffer) {
        val (width, height) = loadedTransforms.calculateSize()

        val buffer = buffer.buffer
        buffer.rewind()
        if (width == 0 || height == 0) {
            RecyclableBitmap(ImageBitmap(1, 1), {})
        } else {
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            bitmap.copyPixelsFromBuffer(buffer)
            RecyclableBitmap(
                bitmap.asImageBitmap(),
                { bitmap.recycle() },
                colorFilter = ColorMatrixColorFilter(BgrToRgbMatrix)
            )
        }
    }
    return imageBitmap
}
