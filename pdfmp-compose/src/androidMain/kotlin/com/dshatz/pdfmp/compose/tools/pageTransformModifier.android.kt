package com.dshatz.pdfmp.compose.tools

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.ui.Modifier
import com.dshatz.pdfmp.compose.state.PdfState
import kotlinx.coroutines.CoroutineScope

actual fun Modifier.platformPageTransformModifier(
    state: PdfState,
): Modifier = this