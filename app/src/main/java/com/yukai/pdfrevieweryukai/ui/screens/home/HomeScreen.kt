package com.yukai.pdfrevieweryukai.ui.screens.home

import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yukai.pdfrevieweryukai.R
import com.yukai.pdfrevieweryukai.data.database.RecentPdfEntity
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HomeScreen(
    onNavigateToPdfViewer: (String) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let { selectedUri ->
            Log.i("PdfViewerViewModel", "selectedUri URI: $selectedUri")

            // ✅ 這段是關鍵：請求持久化權限
            context.contentResolver.takePersistableUriPermission(
                selectedUri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            )
            // 取得檔案資訊
            val cursor = context.contentResolver.query(selectedUri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val nameIndex = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                    val sizeIndex = it.getColumnIndex(android.provider.OpenableColumns.SIZE)
                    
                    val fileName = if (nameIndex >= 0) it.getString(nameIndex) else "Unknown"
                    val fileSize = if (sizeIndex >= 0) it.getLong(sizeIndex) else 0L
                    
                    viewModel.onEvent(
                        HomeUiEvent.PdfFileSelected(
                            uri = selectedUri.toString(),
                            fileName = fileName,
                            fileSize = fileSize
                        )
                    )
                }
            }
        }
    }
    
    LaunchedEffect(viewModel.uiEffect) {
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                HomeUiEffect.OpenFilePicker -> {
                    launcher.launch(arrayOf("application/pdf"))
                }
                is HomeUiEffect.NavigateToPdfViewer -> {
                    onNavigateToPdfViewer(Uri.encode(effect.uri))
                }
            }
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // App 標題
        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.h4,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 24.dp)
        )
        
        // 選擇檔案按鈕
        Button(
            onClick = { viewModel.onEvent(HomeUiEvent.SelectPdfFile) },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            enabled = !uiState.isLoading
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colors.onPrimary
                )
            } else {
                Icon(
                    imageVector = Icons.Default.FileOpen,
                    contentDescription = null,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(
                    text = stringResource(R.string.select_pdf),
                    fontSize = 16.sp
                )
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // 最近開啟的檔案
        Text(
            text = stringResource(R.string.recent_files),
            style = MaterialTheme.typography.h6,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        if (uiState.recentPdfs.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = MaterialTheme.colors.surface,
                elevation = 2.dp
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.no_recent_files),
                        style = MaterialTheme.typography.body2,
                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.recentPdfs) { pdf ->
                    RecentPdfItem(
                        pdf = pdf,
                        onItemClick = { viewModel.onEvent(HomeUiEvent.OpenRecentPdf(pdf)) },
                        onRemoveClick = { viewModel.onEvent(HomeUiEvent.RemoveRecentPdf(pdf)) }
                    )
                }
            }
        }
    }
    
    // 錯誤訊息對話框
    uiState.errorMessage?.let { message ->
        AlertDialog(
            onDismissRequest = { viewModel.onEvent(HomeUiEvent.DismissError) },
            title = { Text("錯誤") },
            text = { Text(message) },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.onEvent(HomeUiEvent.DismissError) }
                ) {
                    Text("確定")
                }
            }
        )
    }
}

@Composable
private fun RecentPdfItem(
    pdf: RecentPdfEntity,
    onItemClick: () -> Unit,
    onRemoveClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onItemClick() },
        elevation = 2.dp,
        backgroundColor = MaterialTheme.colors.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // PDF 圖示
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colors.primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PictureAsPdf,
                    contentDescription = null,
                    tint = MaterialTheme.colors.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // 檔案資訊
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = pdf.fileName,
                    style = MaterialTheme.typography.subtitle1,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = formatFileInfo(pdf),
                    style = MaterialTheme.typography.caption,
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                )
                
                Text(
                    text = formatLastOpenTime(pdf.lastOpenedTime),
                    style = MaterialTheme.typography.caption,
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.5f)
                )
            }
            
            // 移除按鈕
            IconButton(
                onClick = onRemoveClick,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "移除",
                    tint = MaterialTheme.colors.onSurface.copy(alpha = 0.5f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

private fun formatFileInfo(pdf: RecentPdfEntity): String {
    val sizeText = when {
        pdf.fileSize < 1024 -> "${pdf.fileSize} B"
        pdf.fileSize < 1024 * 1024 -> "${pdf.fileSize / 1024} KB"
        else -> "${"%.1f".format(pdf.fileSize / (1024.0 * 1024.0))} MB"
    }
    
    return if (pdf.totalPages > 0) {
        "$sizeText • ${pdf.totalPages} 頁"
    } else {
        sizeText
    }
}

private fun formatLastOpenTime(timestamp: Long): String {
    val formatter = SimpleDateFormat("MM/dd HH:mm", Locale.getDefault())
    return "最後開啟: ${formatter.format(Date(timestamp))}"
}