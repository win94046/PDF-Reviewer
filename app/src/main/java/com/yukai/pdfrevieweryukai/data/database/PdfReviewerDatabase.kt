package com.yukai.pdfrevieweryukai.data.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context

@Database(
    entities = [RecentPdfEntity::class],
    version = 1,
    exportSchema = false
)
abstract class PdfReviewerDatabase : RoomDatabase() {
    
    abstract fun recentPdfDao(): RecentPdfDao
    
    companion object {
        const val DATABASE_NAME = "pdf_reviewer_database"
    }
}