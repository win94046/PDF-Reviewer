package com.yukai.pdfrevieweryukai.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recent_pdfs")
data class RecentPdfEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val fileName: String,
    val fileUri: String,
    val lastOpenedTime: Long,
    val fileSize: Long,
    val totalPages: Int = 0
)