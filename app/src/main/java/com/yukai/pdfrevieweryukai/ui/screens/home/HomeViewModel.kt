package com.yukai.pdfrevieweryukai.ui.screens.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yukai.pdfrevieweryukai.data.database.RecentPdfEntity
import com.yukai.pdfrevieweryukai.data.repository.PdfRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val pdfRepository: PdfRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    
    private val _uiEffect = MutableSharedFlow<HomeUiEffect>()
    val uiEffect: SharedFlow<HomeUiEffect> = _uiEffect.asSharedFlow()
    
    init {
        loadRecentPdfs()
    }
    
    fun onEvent(event: HomeUiEvent) {
        when (event) {
            is HomeUiEvent.SelectPdfFile -> {
                _uiState.value = _uiState.value.copy(isLoading = true)
                viewModelScope.launch {
                    _uiEffect.emit(HomeUiEffect.OpenFilePicker)
                }
            }
            
            is HomeUiEvent.OpenRecentPdf -> {
                viewModelScope.launch {
                    _uiEffect.emit(HomeUiEffect.NavigateToPdfViewer(event.pdf.fileUri))
                }
            }
            
            is HomeUiEvent.RemoveRecentPdf -> {
                viewModelScope.launch {
                    pdfRepository.removePdf(event.pdf)
                }
            }
            
            is HomeUiEvent.PdfFileSelected -> {
                handleSelectedPdf(event.uri, event.fileName, event.fileSize)
            }
            
            HomeUiEvent.DismissError -> {
                _uiState.value = _uiState.value.copy(errorMessage = null)
            }
        }
    }
    
    private fun loadRecentPdfs() {
        viewModelScope.launch {
            pdfRepository.getRecentPdfs()
                .catch { error ->
                    _uiState.value = _uiState.value.copy(
                        errorMessage = "載入最近檔案時發生錯誤: ${error.message}"
                    )
                }
                .collect { pdfs ->
                    _uiState.value = _uiState.value.copy(
                        recentPdfs = pdfs,
                        isLoading = false
                    )
                }
        }
    }
    
    private fun handleSelectedPdf(uri: String, fileName: String, fileSize: Long) {
        viewModelScope.launch {
            try {
                // 新增到最近開啟清單
                pdfRepository.addRecentPdf(
                    fileName = fileName,
                    fileUri = uri,
                    fileSize = fileSize
                )
                
                // 導航到 PDF 閱讀器
                _uiEffect.emit(HomeUiEffect.NavigateToPdfViewer(uri))
                
            } catch (error: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "開啟檔案時發生錯誤: ${error.message}",
                    isLoading = false
                )
            }
        }
    }
}

data class HomeUiState(
    val recentPdfs: List<RecentPdfEntity> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

sealed class HomeUiEvent {
    object SelectPdfFile : HomeUiEvent()
    data class OpenRecentPdf(val pdf: RecentPdfEntity) : HomeUiEvent()
    data class RemoveRecentPdf(val pdf: RecentPdfEntity) : HomeUiEvent()
    data class PdfFileSelected(val uri: String, val fileName: String, val fileSize: Long) : HomeUiEvent()
    object DismissError : HomeUiEvent()
}

sealed class HomeUiEffect {
    object OpenFilePicker : HomeUiEffect()
    data class NavigateToPdfViewer(val uri: String) : HomeUiEffect()
}