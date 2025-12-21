package com.dshatz.pdfmp.error

/**
 * Error as reported by PDFIUM's `FPDF_GetLastError()`
 *
 * [Error constants](https://pdfium.googlesource.com/pdfium/+/f79a69c2513a9ee1431bd36c340e4b66bc2fd2d8/fpdfsdk/include/fpdfview.h#233)
 */
abstract class PdfiumException(message: String): Exception("PDFIUM error: $message") {
    companion object {
        fun getError(errorCode: Byte): PdfiumException? {
            return when (errorCode.toInt()) {
                1 -> UnknownError()
                2 -> FileError()
                3 -> FormatError()
                4 -> PasswordError()
                5 -> UnsupportedSecuritySchemeError()
                6 -> ContentError()
                else -> null
            }
        }
    }
}

