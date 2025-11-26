package com.dshatz.pdfmp

actual class InitLib {
    actual fun init() {
        System.loadLibrary("pdfium")
        System.loadLibrary("pdfmp")
        PDFBridge.initNative()
    }
}