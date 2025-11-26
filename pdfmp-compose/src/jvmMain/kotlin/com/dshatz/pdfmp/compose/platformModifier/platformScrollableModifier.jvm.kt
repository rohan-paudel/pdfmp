package com.dshatz.pdfmp.compose.platformModifier

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.rememberScrollableState
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import com.dshatz.pdfmp.compose.state.PdfState

actual fun Modifier.platformScrollableModifier(state: PdfState): Modifier = composed {
    val verticalState = rememberScrollableState {
        state.onScroll(Offset(0f, it))
        it
    }
    val horizontalState = rememberScrollableState {
        state.onScroll(Offset(it, 0f))
        it
    }
    scrollable(verticalState, orientation = Orientation.Vertical)
        .scrollable(horizontalState, orientation = Orientation.Horizontal)
}