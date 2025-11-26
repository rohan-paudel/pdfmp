package com.dshatz.pdfmp

import androidx.compose.ui.window.ComposeUIViewController
import platform.UIKit.UIViewController

fun SampleViewController(): UIViewController =
    ComposeUIViewController {
        Sample()
    }