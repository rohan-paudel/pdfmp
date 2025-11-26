package com.dshatz.pdfmp.compose.platformModifier

import androidx.compose.ui.Modifier
import com.dshatz.pdfmp.compose.state.PdfState

expect fun Modifier.platformScrollableModifier(state: PdfState): Modifier