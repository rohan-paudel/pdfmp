# PDF Viewer library for Kotlin Multiplatform and Compose Multiplatform.

This library wraps [libpdfium](https://pdfium.googlesource.com/pdfium/) (the Chromium PDF engine). The pre-compiled pdfium files are taken from [here](https://github.com/bblanchon/pdfium-binaries) and bundled into this library.

## Supported targets
- Android
- JVM Desktop
    - Linux x64, ARM64
    - Windows X64
    - MacOS X64, ARM64
- iOS **(needs work, see #17)**
    - x64 (simulator)
    - arm64
    - arm64Simulator

 ## Usage
 ### Add the dependency
 
 ```kotlin
 implementation("com.dshatz.pdfmp:pdfmp-compose:1.0.7")
```

```toml
pdfmp = { module = "com.dshatz.pdfmp:pdfmp-compose", version = "1.0.7" }
```

### Load the pdf
#### From bytes
```kotlin
  val pdf = PdfSource.PdfBytes(byteArray)
```

#### From path
```kotlin
val pdf = PdfSource.PdfPath(Path("~/Download/sample.pdf")
```

#### From Compose Resources
You can use this helper.
```kotlin
val bytes: PdfBytes? by asyncPdfResource { 
    Res.readBytes("files/doc.pdf")
}
```

### Display the document.
To create a `PdfState`, you need only an instance of `PdfSource`.

Additionally, you can pass some optional parameters:
 - `pageRange: IntRange` - range of pages to include. **Zero-indexed, inclusive range.**
 - `pageSpacing: Dp` - spacing between pages, **in DP**.

```kotlin

@Composable
fun ShowDoc(state: PdfState) {
    val state = rememberPdfState(pdf, pageSpacing = 100.dp)
    PdfView(state, Modifier.fillMaxSize())
}
```

#### Observing document state
Use `val displayState by state.displayState` to get the document state.

```kotlin
sealed class DisplayState {
    data object Initializing: DisplayState()
    data object Active: DisplayState()
    data class Error(val error: Throwable): DisplayState()
}
```

You can use this to display a loading indicator or an error dialog.


#### Controlling the document view
You can read and change various view parameters.
```kotlin
val layoutInfo by state.layoutInfo()
// It will be null if the document did not load yet.
layoutInfo?.apply {
    // Below is some of the available state

    val visiblePages: State<List<VisiblePageInfo>>
    val mostVisiblePage: State<VisiblePageInfo?>
    val totalPages: State<Int>
    val pageRange: State<IntRange>
    val documentHeight: State<Float>

    // Scroll related
    var offsetY: Float
    fun scrollTo(pageIdx: Int)
    suspend fun animateScrollTo(pageIdx: Int)

    // Zoom related
    val zoom: State<Float>
    fun setZoom(newZoom: Float)
    suspend fun animateSetZoom(newZoom: Float)
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

