package com.dshatz.pdfmp.compose

//val ImageTransform.viewport get() = IntSize(viewportWidth, viewportHeight)

/*
@Composable
internal fun InternalPdfPage(
    state: PdfPageState,
    page: Int,
    modifier: Modifier = Modifier,
    scrollable: Boolean = true
) {
    val docState = state.docState
    */
/**
     * The best image we have now.
     *//*

    var displayedImage by remember { mutableStateOf<CurrentImage?>(null) }

    val requestedTransform by state.requestedTransform

    val ratio by produceState(1f, docState, page) {
        value = renderer.getAspectRatio(page)
    }

    LaunchedEffect(requestedTransform) {
        // Only re-render if the requested transform has changed
        if (requestedTransform != displayedImage?.loadedTransform) {
            if (displayedImage != null) delay(100)

            withContext(Dispatchers.IO) {
                val (renderResult, buffer) = withRenderRequest(page, requestedTransform, bufferPool) {
                    renderer.render(it)
                }
                displayedImage = CurrentImage(
                    requestedTransform,
                    renderResult.transform,
                    renderResult.pageSize,
                    buffer
                )
            }
        }
    }

    val viewportSize by state.docState.viewport
    val density = LocalDensity.current


    val imageSize by remember(viewportSize, requestedTransform.scale) {
        derivedStateOf {
            val width = viewportSize.width * requestedTransform.scale
            val height = width / ratio
            Size(
                width = width,
                height = height
            )
        }
    }


    val displaySize by remember(imageSize, viewportSize) {
        derivedStateOf {
            Size(
                width = min(imageSize.width, viewportSize.width),
                height = min(imageSize.height, viewportSize.height)
            )
        }
    }

    val baseImage by produceState<CurrentImage?>(null, renderer, page, viewportSize, ratio) {
        delay(50)
        val transform = ImageTransform(
            1f,
            0f,
            0f,
            viewportSize.width.toInt(),
            (viewportSize.width / ratio).toInt()
        )
        val (renderResult, buffer) = withContext(Dispatchers.IO) {
            withRenderRequest(page, transform, bufferPool) {
                renderer.render(it)
            }
        }
        value = CurrentImage(
            transform,
            transform,
            renderResult.pageSize,
            buffer
        )
    }


    val scrollState = rememberScrollableState {
        state.dispatchScroll(Offset(0f, it)).y
    }
    val draggableVertical = rememberDraggableState {
        val unconsumed = state.dispatchScroll(Offset(0f, it))
    }

    val draggableHorizontal = rememberDraggableState {
        state.dispatchScroll(Offset(it, 0f))
    }

    val imageSizeDp by remember(imageSize, displaySize) {
        derivedStateOf {
            with(density) { imageSize.toDpSize() }
        }
    }

    LaunchedEffect(requestedTransform.scale) {
        if (requestedTransform.scale == 1f) {
            state.docState.globalHorizontalOffset.value = 0f
        }
    }




    // The viewport - sized by the incoming modifier.
    Box(
        modifier.then(Modifier.onGloballyPositioned {
            state.docState.setViewport(
                Size(
                    it.size.width.toFloat(),
                    it.size.height.toFloat()
                )
            )
        }
            .clipToBounds()
            .scrollable(scrollState, Orientation.Vertical, enabled = scrollable)
            .draggable(draggableHorizontal, orientation = Orientation.Horizontal)
            .draggable(draggableVertical, orientation = Orientation.Vertical, enabled = scrollable)
            .pageTransformModifier(state, scrollable))
    ) {

        baseImage?.takeIf { it.loadedTransform != requestedTransform }?.let { baseImg ->
            val image = baseImg.toImageBitmap()
            DisposableEffect(image) {
                onDispose {
                    image.onRecycle()
                }
            }
            PartialBitmapRenderer(
                bitmap = baseImg.toImageBitmap().imageBitmap,
                srcOffset = requestedTransform.offset.let {
                    IntOffset(
                        x = (-it.x / requestedTransform.scale).toInt(),
                        y = (-it.y / requestedTransform.scale).toInt()
                    )
                },
                scale = requestedTransform.scale,
                dstSize = displaySize,
                modifier = Modifier.requiredSize(with(density) { displaySize.toDpSize() }).clipToBounds()
            )
        }
        Box(
            Modifier
                .requiredSize(imageSizeDp)
                .border(Dp.Hairline, Color.Black)
        ) {}


        // The image itself.
        displayedImage?.let { currentImg ->
            val loadedTransform = currentImg.loadedTransform

            val relativeScale =
                if (loadedTransform.scale != requestedTransform.scale) requestedTransform.scale / loadedTransform.scale else 1f

            val relativeX = requestedTransform.offsetX - (loadedTransform.offsetX * relativeScale)
            val relativeY = requestedTransform.offsetY - (loadedTransform.offsetY * relativeScale)

            LaunchedEffect(imageSize) {
                state.setImageSize(imageSize)
            }

            val image = currentImg.toImageBitmap()
            DisposableEffect(image) {
                onDispose {
                    image.onRecycle()
                }
            }

            Image(
                bitmap = image.imageBitmap,
                contentDescription = null,
                contentScale = ContentScale.None,
                alignment = Alignment.TopStart,
                modifier = Modifier.requiredSize(with(density) { viewportSize.toDpSize() })
                    .graphicsLayer {
                        scaleX = relativeScale
                        scaleY = relativeScale
                        translationX = relativeX
                        translationY = relativeY
                        transformOrigin = TransformOrigin(0f, 0f)
                    }
            )
        }

        */
/*Column(
            Modifier.align(Alignment.TopCenter).padding(top = 10.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            DebugBadge("Viewport: ${viewportSize.width} x ${viewportSize.height}")
            DebugBadge("Image size: $imageSize")
        }*//*

    }


}

@Composable
private fun ColumnScope.DebugBadge(text: String) {
    ElevatedCard {
        Text(text, modifier = Modifier.padding(10.dp))
    }
}

val ImageTransform.offset get() = Offset(offsetX, offsetY)

*/
