package com.dshatz.pdfmp.compose.tools

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateCentroid
import androidx.compose.foundation.gestures.calculateCentroidSize
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateRotation
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.util.fastAny
import androidx.compose.ui.util.fastForEach
import com.dshatz.pdfmp.compose.state.PdfPageState
import kotlin.math.PI
import kotlin.math.abs

actual fun Modifier.platformPageTransformModifier(
    pageState: PdfPageState,
    enablePan: Boolean
): Modifier = this