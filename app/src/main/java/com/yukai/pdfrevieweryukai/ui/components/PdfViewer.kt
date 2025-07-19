package com.yukai.pdfrevieweryukai.ui.components

import android.net.Uri
import android.util.Log
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
    
    // 記住 PDFView 實例，避免重複創建
    val pdfViewInstance = remember {
        PDFView(context, null).apply {
            setBackgroundColor(android.graphics.Color.WHITE)
        }
    }

    AndroidView(
        modifier = modifier.fillMaxSize(),
        factory = { _ ->
            pdfViewInstance
        },
        update = { pdfView ->
            // 避免在 update 中重複載入相同的 URI
            if (pdfView.tag != uri) {
                try {
                    // 只有當 PDFView 處於有效狀態時才進行清理
                    if (pdfView.isRecycled.not()) {
                        pdfView.recycle()
                    }

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
                        .scrollHandle(null) // 移除 ScrollHandle 避免 Context 問題
                        .enableAntialiasing(true)
                        .spacing(0)
                        .autoSpacing(false) // true 代表UI一次只能顯示一頁
                        .pageFitPolicy(FitPolicy.BOTH)
                        .pageSnap(false) // 是否讓頁面自動對齊到畫面邊界
                        .pageFling(true)
                        .nightMode(false)
                        .onLoad { nbPages ->
                            pdfView.tag = uri // 標記已載入的 URI
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
        }
    )
    
    // 清理資源
    DisposableEffect(Unit) {
        onDispose {
            try {
                if (pdfViewInstance.isRecycled.not()) {
                    pdfViewInstance.recycle()
                }
            } catch (e: Exception) {
                Log.w("PdfViewer", "Error during cleanup", e)
            }
        }
    }
}

