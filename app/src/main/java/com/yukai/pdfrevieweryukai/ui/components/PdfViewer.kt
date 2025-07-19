package com.yukai.pdfrevieweryukai.ui.components

import android.net.Uri
import android.util.Log
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.github.barteksc.pdfviewer.PDFView
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle
import com.github.barteksc.pdfviewer.util.FitPolicy

@Composable
fun PdfViewer(
    uri: String,
    onPageChanged: (Int) -> Unit,
    onLoadComplete: (Int) -> Unit,
    onError: (Throwable) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val parsedUri = remember(uri) { Uri.parse(uri) }

    AndroidView(
        modifier = modifier.fillMaxSize(),
        factory = { ctx ->
            PDFView(ctx, null).apply {
                setBackgroundColor(android.graphics.Color.WHITE)
            }
        },
        update = { pdfView ->
            try {
                // 重新載入 PDFView（避免 Compose 多次重組造成 UI 異常）
                pdfView.recycle()  // ✅ 重要：重用 View 前先清掉舊資料

                val inputStream = context.contentResolver.openInputStream(parsedUri)
                if (inputStream == null) {
                    onError(IllegalArgumentException("InputStream is null for URI: $uri"))
                    return@AndroidView
                }

                pdfView.fromStream(inputStream)
                    .defaultPage(0)
                    .enableSwipe(true)
                    .swipeHorizontal(false)
                    .enableDoubletap(true)
                    .enableAnnotationRendering(false)
                    .password(null)
                    .scrollHandle(DefaultScrollHandle(context))
                    .enableAntialiasing(true)
                    .spacing(10)
                    .autoSpacing(false)
                    .pageFitPolicy(FitPolicy.WIDTH)
                    .pageSnap(true)
                    .pageFling(false)
                    .nightMode(false)
                    .onLoad { nbPages ->
                        onLoadComplete(nbPages)
                    }
                    .onPageChange { page, _ ->
                        onPageChanged(page)
                    }
                    .onError { throwable ->
                        Log.e("PdfViewer", "PDF loading error", throwable)
                        onError(throwable)
                    }
                    .load()

            } catch (e: Exception) {
                Log.e("PdfViewer", "PDF init error", e)
                onError(e)
            }
        }
    )
}

