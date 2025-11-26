package com.dshatz.pdfmp

import android.os.Bundle
import android.os.Environment
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import com.dshatz.pdfmp.compose.PdfView
import com.dshatz.pdfmp.compose.source.pdfResource
import com.dshatz.pdfmp.compose.state.rememberPdfState
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.dialogs.init
import io.github.vinceglb.filekit.utils.toKotlinxPath
import kotlinx.io.files.Path

class MainActivity: ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        NativeLibLoader.load()
        setContent {
            MaterialTheme {
                /*val pdf by asyncPdfResource {
                    val dir = File("/sdcard/Download")
                    SystemFileSystem.source(dir?.resolve("sample.pdf")!!.toKotlinxPath()).buffered().use {
                        it.readByteArray()
                    }
                }*/
                FileKit.init(this)
                val res = pdfResource(Path(Environment.getExternalStorageDirectory().toKotlinxPath(), "Download", "sample.pdf"))
                val pdf = rememberPdfState(res)
                PdfView(pdf, modifier = Modifier.fillMaxSize())
            }
        }
    }
}