package com.yukai.pdfrevieweryukai.data.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface RecentPdfDao {
    
    @Query("SELECT * FROM recent_pdfs ORDER BY lastOpenedTime DESC LIMIT 5")
    fun getRecentPdfs(): Flow<List<RecentPdfEntity>>
    
    @Query("SELECT * FROM recent_pdfs WHERE fileUri = :uri LIMIT 1")
    suspend fun getPdfByUri(uri: String): RecentPdfEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPdf(pdf: RecentPdfEntity): Long
    
    @Update
    suspend fun updatePdf(pdf: RecentPdfEntity)
    
    @Delete
    suspend fun deletePdf(pdf: RecentPdfEntity)
    
    @Query("DELETE FROM recent_pdfs WHERE id NOT IN (SELECT id FROM recent_pdfs ORDER BY lastOpenedTime DESC LIMIT 5)")
    suspend fun deleteOldPdfs()
    
    @Query("SELECT COUNT(*) FROM recent_pdfs")
    suspend fun getCount(): Int
}