package com.dshatz.pdfmp.compose.platformModifier

import androidx.compose.ui.Modifier
import com.dshatz.pdfmp.compose.state.PdfState

/**
 * Platform-specific modifier for gestures.
 */
expect fun Modifier.platformPageTransformModifier(
    state: PdfState,
): Modifier