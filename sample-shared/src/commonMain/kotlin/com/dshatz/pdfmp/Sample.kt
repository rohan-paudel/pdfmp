package com.dshatz.pdfmp

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.dshatz.pdfmp.compose.PdfView
import com.dshatz.pdfmp.compose.source.asyncPdfResource
import com.dshatz.pdfmp.compose.state.rememberPdfState
import pdf_multiplatform.sample_shared.generated.resources.Res

@Composable
fun Sample() {
    val res by asyncPdfResource {
        Res.readBytes("files/sample.pdf")
    }

    res?.let {
        val pdf = rememberPdfState(it)
        PdfView(
            pdf,
            modifier = Modifier.fillMaxSize()
        )
    }
}