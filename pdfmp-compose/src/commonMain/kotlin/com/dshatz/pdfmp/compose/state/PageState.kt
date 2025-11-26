package com.dshatz.pdfmp.compose.state

data class PdfPageState(
    val pageIdx: Int,
    val aspectRatio: Float,
) {

    /*suspend fun getRatio(): Float {
        return docState.renderer.getAspectRatio(pageIdx)
    }
    val offsetState: State<Offset> = derivedStateOf {
        Offset(docState.globalHorizontalOffset.value, 0f)
    }
    val requestedTransform: State<ImageTransform> = derivedStateOf {
        ImageTransform(
            docState.scale.value,
            offsetState.value.x,
            offsetState.value.y,
            min(docState.viewport.value.width, imageSize.value.width).toInt(),
            min(docState.viewport.value.height, imageSize.value.height).toInt()
        )
    }

    fun dispatchScroll(offset: Offset): Offset {
        val coerced = requestedTransform.value.let {
            Offset(
                x = it.offsetX + offset.x,
                y = it.offsetY + offset.y
            ).coerceOffset()
        }
        docState.globalHorizontalOffset.value = coerced.x
//        verticalOffset.value = coerced.y
        return offset - coerced
    }
    private fun Offset.coerceOffset(): Offset {
        val image = imageSize.value

        val viewport = requestedTransform.value.viewport
        val maxX = 0f
        val minX = (viewport.width - image.width).coerceAtMost(0f)

        val maxY = 0f
        val minY = (viewport.height - image.height).coerceAtMost(0f)
        val coerced = Offset(
            x = x.coerceIn(minX, maxX),
            y = y.coerceIn(minY, maxY)
        )
        return coerced
    }

    fun setImageSize(size: Size) {
        this.imageSize.value = size
    }*/
}