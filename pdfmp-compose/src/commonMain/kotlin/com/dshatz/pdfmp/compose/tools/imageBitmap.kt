package com.dshatz.pdfmp.compose.tools

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ImageBitmap
import com.dshatz.pdfmp.compose.CurrentImage

@Composable
internal expect fun CurrentImage.toImageBitmap(): RecyclableBitmap

data class RecyclableBitmap(
    val imageBitmap: ImageBitmap,
    private val onRecycle: () -> Unit,
    val colorFilter: ColorFilter? = null
) {
    fun free() {
        onRecycle()
    }
}