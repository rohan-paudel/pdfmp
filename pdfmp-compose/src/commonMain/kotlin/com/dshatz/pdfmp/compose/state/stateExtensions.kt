package com.dshatz.pdfmp.compose.state

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import kotlin.math.roundToInt

/**
 * Current document zoom factor expressed as integer percentage (e.g. 100, 150, 200).
 */
@Composable
fun PdfState.zoomPercents(): State<Int> {
    val state by layoutInfo()
    return derivedStateOf {
        state?.zoom?.value?.let {
            (it * 100).roundToInt()
        } ?: 100
    }
}
