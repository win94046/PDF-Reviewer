package com.yukai.pdfrevieweryukai.ui.screens.pdfviewer

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yukai.pdfrevieweryukai.R
import com.yukai.pdfrevieweryukai.ui.components.PdfViewer

@Composable
fun PdfViewerScreen(
    uri: String,
    onNavigateBack: () -> Unit,
    viewModel: PdfViewerViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    LaunchedEffect(uri) {
        viewModel.onEvent(PdfViewerUiEvent.LoadPdf(uri))
    }
    
    LaunchedEffect(viewModel.uiEffect) {
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                PdfViewerUiEffect.NavigateBack -> onNavigateBack()
            }
        }
    }
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // 頂部工具列
        TopAppBar(
            title = {
                Column {
                    Text(
                        text = uiState.fileName.ifEmpty { "PDF Viewer" },
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    if (uiState.totalPages > 0) {
                        Text(
                            text = stringResource(
                                R.string.page_info,
                                uiState.currentPage + 1,
                                uiState.totalPages
                            ),
                            fontSize = 12.sp,
                            color = MaterialTheme.colors.onPrimary.copy(alpha = 0.8f)
                        )
                    }
                }
            },
            navigationIcon = {
                IconButton(
                    onClick = { viewModel.onEvent(PdfViewerUiEvent.NavigateBack) }
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "返回"
                    )
                }
            },
            backgroundColor = MaterialTheme.colors.primary,
            contentColor = MaterialTheme.colors.onPrimary,
            elevation = 4.dp
        )
        
        // PDF 內容區域
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Gray.copy(alpha = 0.1f))
        ) {
            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(48.dp),
                                color = MaterialTheme.colors.primary
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = stringResource(R.string.loading),
                                style = MaterialTheme.typography.body2,
                                color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
                
                uiState.errorMessage != null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Card(
                            modifier = Modifier.padding(16.dp),
                            elevation = 4.dp
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "載入失敗",
                                    style = MaterialTheme.typography.h6,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colors.error
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = uiState.errorMessage!!,
                                    style = MaterialTheme.typography.body2,
                                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = { viewModel.onEvent(PdfViewerUiEvent.NavigateBack) }
                                ) {
                                    Text("返回")
                                }
                            }
                        }
                    }
                }
                
                uiState.pdfUri.isNotEmpty() -> {
                    PdfViewer(
                        uri = uiState.pdfUri,
                        onPageChanged = { page ->
                            viewModel.onEvent(PdfViewerUiEvent.PageChanged(page))
                        },
                        onLoadComplete = { totalPages ->
                            viewModel.onEvent(PdfViewerUiEvent.TotalPagesLoaded(totalPages))
                        },
                        onError = { error ->
                            viewModel.onEvent(PdfViewerUiEvent.DismissError)
                            // 這裡可以顯示錯誤訊息
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
    
    // 錯誤對話框
    uiState.errorMessage?.let { message ->
        AlertDialog(
            onDismissRequest = { viewModel.onEvent(PdfViewerUiEvent.DismissError) },
            title = { Text("錯誤") },
            text = { Text(message) },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.onEvent(PdfViewerUiEvent.DismissError) }
                ) {
                    Text("確定")
                }
            }
        )
    }
}