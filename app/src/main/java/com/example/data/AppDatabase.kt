package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.dao.*
import com.example.data.model.*

@Database(
    entities = [
        ChatThreadEntity::class,
        ChatMessageEntity::class,
        DiaryEntryEntity::class,
        EventEntity::class,
        VaultDocumentEntity::class,
        TaskEntity::class,
        CategoryContactEntity::class,
        MusicTrackEntity::class,
        DownloadedVideoEntity::class
    ],
    version = 7,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun chatDao(): ChatDao
    abstract fun categoryContactDao(): CategoryContactDao
    abstract fun diaryDao(): DiaryDao
    abstract fun eventDao(): EventDao
    abstract fun vaultDao(): VaultDao
    abstract fun taskDao(): TaskDao
    abstract fun musicDao(): MusicDao
    abstract fun videoDao(): VideoDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "ledger_organizer.db"
                ).fallbackToDestructiveMigration(true).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
