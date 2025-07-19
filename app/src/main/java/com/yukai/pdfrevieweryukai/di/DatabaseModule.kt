package com.yukai.pdfrevieweryukai.di

import android.content.Context
import androidx.room.Room
import com.yukai.pdfrevieweryukai.data.database.PdfReviewerDatabase
import com.yukai.pdfrevieweryukai.data.database.RecentPdfDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): PdfReviewerDatabase {
        return Room.databaseBuilder(
            context,
            PdfReviewerDatabase::class.java,
            PdfReviewerDatabase.DATABASE_NAME
        ).build()
    }
    
    @Provides
    fun provideRecentPdfDao(database: PdfReviewerDatabase): RecentPdfDao {
        return database.recentPdfDao()
    }
}