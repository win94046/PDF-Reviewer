package com.yukai.pdfrevieweryukai.data.repository

import com.yukai.pdfrevieweryukai.data.database.RecentPdfDao
import com.yukai.pdfrevieweryukai.data.database.RecentPdfEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PdfRepository @Inject constructor(
    private val recentPdfDao: RecentPdfDao
) {
    
    fun getRecentPdfs(): Flow<List<RecentPdfEntity>> = recentPdfDao.getRecentPdfs()
    
    suspend fun addRecentPdf(
        fileName: String,
        fileUri: String,
        fileSize: Long,
        totalPages: Int = 0
    ) {
        val existingPdf = recentPdfDao.getPdfByUri(fileUri)
        
        if (existingPdf != null) {
            // 更新現有記錄的開啟時間
            recentPdfDao.updatePdf(
                existingPdf.copy(
                    lastOpenedTime = System.currentTimeMillis(),
                    totalPages = totalPages
                )
            )
        } else {
            // 新增記錄
            val newPdf = RecentPdfEntity(
                fileName = fileName,
                fileUri = fileUri,
                lastOpenedTime = System.currentTimeMillis(),
                fileSize = fileSize,
                totalPages = totalPages
            )
            recentPdfDao.insertPdf(newPdf)
        }
        
        // 確保只保留最新的 5 筆記錄
        recentPdfDao.deleteOldPdfs()
    }
    
    suspend fun removePdf(pdf: RecentPdfEntity) {
        recentPdfDao.deletePdf(pdf)
    }
}