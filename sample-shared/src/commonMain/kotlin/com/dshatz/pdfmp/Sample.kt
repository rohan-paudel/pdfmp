package com.dshatz.pdfmp

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.dshatz.pdfmp.compose.PdfView
import com.dshatz.pdfmp.compose.source.asyncPdfResource
import com.dshatz.pdfmp.compose.state.rememberPdfState
import pdf_multiplatform.sample_shared.generated.resources.Res

@Composable
fun Sample() {
    Column(Modifier.fillMaxSize().background(Color.Gray)) {
        var selected by remember { mutableStateOf(1) }
        val truncatedRange = 2..3
        PrimaryTabRow(
            selected,
            modifier = Modifier.height(60.dp)
        ) {
            Tab(
                selected = selected == 0,
                onClick = { selected = 0 },
                content = {
                    Text("Full document")
                }
            )

            Tab(
                selected = selected == 1,
                onClick = { selected = 1 },
                content = {
                    Text("Pages $truncatedRange")
                }
            )

            Tab(
                selected = selected == 2,
                onClick = { selected = 2 },
                content = {
                    Text("Landscape")
                }
            )

            Tab(
                selected = selected == 3,
                onClick = { selected = 3 },
                content = {
                    Text("Colorful")
                }
            )
        }
        AnimatedContent(selected, modifier = Modifier.weight(1f)) {
            if (it == 0) {
                FullDoc()
            } else if (it == 1) {
                TruncatedDoc(truncatedRange)
            } else if (it == 2) {
                LandscapeDoc()
            } else {
                ColoredDoc()
            }
        }

    }
}

@Composable
private fun FullDoc() {
    val res by asyncPdfResource {
        Res.readBytes("files/sample.pdf")
    }

    res?.let {
        val pdf = rememberPdfState(it, pageSpacing = 100.dp)
        PdfView(
            pdf,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
private fun TruncatedDoc(range: IntRange) {
    val res by asyncPdfResource {
        Res.readBytes("files/sample.pdf")
    }

    res?.let {
        val pdf = rememberPdfState(it, range)
        PdfView(
            pdf,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
private fun LandscapeDoc() {
    val res by asyncPdfResource {
        Res.readBytes("files/landscape.pdf")
    }

    res?.let {
        val pdf = rememberPdfState(it)
        PdfView(
            pdf,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
private fun ColoredDoc() {
    val res by asyncPdfResource {
        Res.readBytes("files/sample3.pdf")
    }

    res?.let {
        val pdf = rememberPdfState(it)
        PdfView(
            pdf,
            modifier = Modifier.fillMaxSize()
        )
    }
}