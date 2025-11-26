package com.dshatz.pdfmp.compose.tools

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateCentroid
import androidx.compose.foundation.gestures.calculateCentroidSize
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateRotation
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.util.fastAny
import androidx.compose.ui.util.fastForEach
import com.dshatz.pdfmp.compose.platformModifier.platformPageTransformModifier
import com.dshatz.pdfmp.compose.state.PdfState
import kotlin.math.PI
import kotlin.math.abs


/**
 * Common modifier for transform gestures. Includes [platformPageTransformModifier].
 */
fun Modifier.pageTransformModifier(state: PdfState): Modifier = composed {
    Modifier.platformPageTransformModifier(state)
}

suspend fun PointerInputScope.detectTransformGesturesHighPriority(
    panZoomLock: Boolean = false,
    consumePanGestures: Boolean = true,
    onGesture: (centroid: Offset, pan: Offset, zoom: Float, rotation: Float) -> Unit,
) {
    awaitEachGesture {
        var rotation = 0f
        var zoom = 1f
        var pan = Offset.Zero
        var pastTouchSlop = false
        val touchSlop = viewConfiguration.touchSlop
        var lockedToPanZoom = false

        awaitFirstDown(requireUnconsumed = false)

        do {
            val event = awaitPointerEvent(PointerEventPass.Initial)
            val canceled = event.changes.fastAny { it.isConsumed }

            // If less than 2 fingers, we dont consume.
            // We let these events pass through to the main pass so
            // the scrollable modifier can handle them.
            if (event.changes.size < 2) {
                // Reset state so if they add a 2nd finger later,
                // we treat it as a fresh zoom/pan attempt (re-check slop)
                pastTouchSlop = false
                zoom = 1f
                rotation = 0f
                pan = Offset.Zero
                continue
            }

            if (!canceled) {
                val zoomChange = event.calculateZoom()
                val rotationChange = event.calculateRotation()
                val panChange = event.calculatePan()

                if (!pastTouchSlop) {
                    zoom *= zoomChange
                    rotation += rotationChange
                    pan += panChange

                    val centroidSize = event.calculateCentroidSize(useCurrent = false)
                    val zoomMotion = abs(1 - zoom) * centroidSize
                    val rotationMotion = abs(rotation * PI.toFloat() * centroidSize / 180f)
                    val panMotion = pan.getDistance()

                    if (zoomMotion > touchSlop ||
                        rotationMotion > touchSlop ||
                        panMotion > touchSlop
                    ) {
                        pastTouchSlop = true
                        lockedToPanZoom = panZoomLock && rotationMotion < touchSlop
                    }
                }

                if (pastTouchSlop) {
                    val centroid = event.calculateCentroid(useCurrent = false)
                    val effectiveRotation = if (lockedToPanZoom) 0f else rotationChange

                    val isZoomOrRotate = effectiveRotation != 0f || zoomChange != 1f
                    val isPan = panChange != Offset.Zero

                    if (isZoomOrRotate || isPan) {
                        onGesture(centroid, panChange, zoomChange, effectiveRotation)
                    }

                    event.changes.fastForEach {
                        val shouldConsumePosition = isZoomOrRotate || (isPan && consumePanGestures)

                        if (shouldConsumePosition && it.positionChanged()) {
                            it.consume()
                        }
                    }
                }
            }
        } while (!canceled && event.changes.fastAny { it.pressed })
    }
}