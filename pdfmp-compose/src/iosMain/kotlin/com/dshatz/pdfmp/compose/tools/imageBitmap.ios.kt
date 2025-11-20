package com.dshatz.pdfmp.compose.tools

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.decodeToImageBitmap
import com.dshatz.pdfmp.compose.CurrentImage

@Composable
internal actual fun CurrentImage.toImageBitmap(): RecyclableBitmap {
    val imageBitmap = remember(this) { buffer.byteArray.decodeToImageBitmap() }
    return RecyclableBitmap(imageBitmap, {})
}