package com.yukai.pdfrevieweryukai.ui.screens.pdfviewer

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yukai.pdfrevieweryukai.data.repository.PdfRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PdfViewerViewModel @Inject constructor(
    private val pdfRepository: PdfRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(PdfViewerUiState())
    val uiState: StateFlow<PdfViewerUiState> = _uiState.asStateFlow()
    
    private val _uiEffect = MutableSharedFlow<PdfViewerUiEffect>()
    val uiEffect: SharedFlow<PdfViewerUiEffect> = _uiEffect.asSharedFlow()
    
    fun onEvent(event: PdfViewerUiEvent) {
        when (event) {
            is PdfViewerUiEvent.LoadPdf -> {
                loadPdf(event.uri)
            }
            
            is PdfViewerUiEvent.PageChanged -> {
                _uiState.value = _uiState.value.copy(currentPage = event.page)
            }
            
            is PdfViewerUiEvent.TotalPagesLoaded -> {
                val currentState = _uiState.value
                _uiState.value = currentState.copy(
                    totalPages = event.totalPages,
                    isLoading = false
                )
                
                // 更新資料庫中的頁數資訊
                updatePdfInfo(currentState.pdfUri, event.totalPages)
            }
            
            PdfViewerUiEvent.DismissError -> {
                _uiState.value = _uiState.value.copy(errorMessage = null)
            }
            
            PdfViewerUiEvent.NavigateBack -> {
                viewModelScope.launch {
                    _uiEffect.emit(PdfViewerUiEffect.NavigateBack)
                }
            }
        }
    }

    private fun loadPdf(uri: String) {
        _uiState.value = _uiState.value.copy(
            isLoading = true,
            pdfUri = uri,
            errorMessage = null
        )

        try {
            val parsedUri = Uri.parse(uri)
            Log.i("PdfViewerViewModel", "loadPdf parsedUri: $parsedUri")
            // ✅ 用 openInputStream 嘗試打開
            context.contentResolver.openInputStream(parsedUri)?.use {
                // 確實有檔案可以開啟，不處理內容也沒關係
            } ?: run {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "無法開啟檔案，可能已被移除或未授權"
                )
                return
            }

            _uiState.value = _uiState.value.copy(
                pdfUri = uri,
                fileName = getFileNameFromUri(parsedUri),
                isLoading = false
            )

        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                errorMessage = "載入 PDF 時發生錯誤: ${e.message}"
            )
        }
    }
    
    private fun updatePdfInfo(uri: String, totalPages: Int) {
        viewModelScope.launch {
            try {
                val parsedUri = Uri.parse(uri)
                val fileName = getFileNameFromUri(parsedUri)
                val fileSize = getFileSizeFromUri(parsedUri)
                
                pdfRepository.addRecentPdf(
                    fileName = fileName,
                    fileUri = uri,
                    fileSize = fileSize,
                    totalPages = totalPages
                )
            } catch (e: Exception) {
                // 忽略更新錯誤，不影響閱讀功能
            }
        }
    }
    
    private fun getFileNameFromUri(uri: Uri): String {
        return try {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val nameIndex = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                    if (nameIndex >= 0) {
                        return it.getString(nameIndex) ?: "Unknown"
                    }
                }
            }
            "Unknown"
        } catch (e: Exception) {
            "Unknown"
        }
    }
    
    private fun getFileSizeFromUri(uri: Uri): Long {
        return try {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val sizeIndex = it.getColumnIndex(android.provider.OpenableColumns.SIZE)
                    if (sizeIndex >= 0) {
                        return it.getLong(sizeIndex)
                    }
                }
            }
            0L
        } catch (e: Exception) {
            0L
        }
    }
}

data class PdfViewerUiState(
    val pdfUri: String = "",
    val fileName: String = "",
    val currentPage: Int = 0,
    val totalPages: Int = 0,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

sealed class PdfViewerUiEvent {
    data class LoadPdf(val uri: String) : PdfViewerUiEvent()
    data class PageChanged(val page: Int) : PdfViewerUiEvent()
    data class TotalPagesLoaded(val totalPages: Int) : PdfViewerUiEvent()
    object DismissError : PdfViewerUiEvent()
    object NavigateBack : PdfViewerUiEvent()
}

sealed class PdfViewerUiEffect {
    object NavigateBack : PdfViewerUiEffect()
}