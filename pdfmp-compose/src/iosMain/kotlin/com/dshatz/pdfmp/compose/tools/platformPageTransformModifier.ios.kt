package com.dshatz.pdfmp.compose.tools

import androidx.compose.ui.Modifier
import com.dshatz.pdfmp.compose.state.PdfPageState

actual fun Modifier.platformPageTransformModifier(
    pageState: PdfPageState,
    enablePan: Boolean
): Modifier = this