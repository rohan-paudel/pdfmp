package com.dshatz.pdfmp.compose.state

sealed class DisplayState {
    data object Idle: DisplayState()
    data class Displaying(val visiblePages: List<Int>): DisplayState()
    data object Initializing: DisplayState()
    data object Ready: DisplayState()
    data class Error(val error: Throwable): DisplayState()
}