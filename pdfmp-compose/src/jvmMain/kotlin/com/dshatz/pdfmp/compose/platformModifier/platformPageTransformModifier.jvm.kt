package com.dshatz.pdfmp.compose.platformModifier

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.isCtrlPressed
import androidx.compose.ui.input.pointer.onPointerEvent
import com.dshatz.pdfmp.compose.state.PdfState

actual fun Modifier.platformPageTransformModifier(state: PdfState): Modifier = composed {
    desktopZoom(state)
}

@OptIn(ExperimentalComposeUiApi::class)
private fun Modifier.desktopZoom(
    state: PdfState
): Modifier = composed {
    onPointerEvent(PointerEventType.Companion.Scroll, PointerEventPass.Initial) { event ->
        val eventChange = event.changes.first()

        if (event.keyboardModifiers.isCtrlPressed) {
            eventChange.consume()

            val scrollDelta = eventChange.scrollDelta
            val zoomFactor = if (scrollDelta.y < 0) 1.1f else 0.9f

            state.zoomBy(
                zoomFactor = zoomFactor,
                centroid = eventChange.position
            )
        }
    }
}