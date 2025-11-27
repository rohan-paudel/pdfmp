# PDF Viewer library for Kotlin Multiplatform and Compose Multiplatform.

This library wraps [libpdfium](https://pdfium.googlesource.com/pdfium/) (the Chromium PDF engine). The pre-compiled pdfium files are taken from [here](https://github.com/bblanchon/pdfium-binaries) and bundled into this library.

## Supported targets
- Android
- JVM Desktop
    - Linux x64, ARM64
    - Windows X64
    - MacOS X64, ARM64
- iOS **(untested)**
    - x64 (simulator)
    - arm64
    - arm64Simulator

 ## Usage
 ### Add the dependency
 
 ```kotlin
 implementation("com.dshatz.pdfmp:pdfmp-compose:1.0.4")
```

```toml
pdfmp = { module = "com.dshatz.pdfmp:pdfmp-compose", version = "1.0.4" }
```

### Load the pdf
#### From bytes
```kotlin
  val state = rememberPdfState(PdfSource.PdfBytes(byteArray))
```

#### From path
```kotlin
val state = rememberPdfState(PdfSource.PdfPath(Path("~/Download/sample.pdf"))
```

#### From Compose Resources
You can use this helper.
```kotlin
val bytes: PdfBytes? by asyncPdfResource { 
    Res.readBytes("files/doc.pdf")
}
```

### Display the document.
```kotlin

@Composable
fun ShowDoc(state: PdfState) {
    PdfView(state, Modifier.fillMaxSize())
}
```

## Features
| Feature                        | Supported | Notes                                                                                                                           |
| ------------------------------ | --------- | ------------------------------------------------------------------------------------------------------------------------------- |
| Loading from bytes             | ✅        |                                                                                                                                 |
| Loading from path              | ✅        | May not work on Android, depending on permissions and path. Please report.                                                      |
| Loading from compose resources | ❌        | Does not work directly because native code does not have access to it. See above for how to do it.                              |
| Zooming and scrolling          | ✅        | Loading the high-res image is debounced by 100ms. On desktop, horizontal scrolling can only be done with Shift+Vertical scroll. |
| Filtering pages                | ✅        | Pass page range to rememberPdfState                                                                                             |

