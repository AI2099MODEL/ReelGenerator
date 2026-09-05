package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat_threads")
data class ChatThreadEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val threadKey: String,
    val name: String,
    val category: String,
    val iconEmoji: String = "💬",
    val lastMessagePreview: String = "",
    val lastMessageTimestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val threadKey: String,
    val content: String,
    val isSentByUser: Boolean,
    val senderName: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isArchived: Boolean = false,
    val syncedTimestamp: Long? = null,
    val isPendingSync: Boolean = false,
    val attachmentUri: String? = null,
    val attachmentName: String? = null,
    val attachmentType: String? = null, // "IMAGE", "DOC", "AUDIO", "FILE"
    val attachmentSizeBytes: Long = 0L
)

@Entity(tableName = "diary_entries")
data class DiaryEntryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val body: String,
    val dateTimestamp: Long = System.currentTimeMillis(),
    val moodOrTag: String = "Reflection",
    val isPinned: Boolean = false,
    val notifyMe: Boolean = false,
    val notificationScheduledId: Int = 0,
    val imageUri: String? = null
)

@Entity(tableName = "daily_schedules")
data class DailyScheduleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val note: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val endTimestamp: Long? = null,
    val isMultiDay: Boolean = false,
    val recurrence: String = "DAILY", // "DAILY", "WEEK", "MONTH", "ANNUAL"
    val timeSlot: String = "09:00 AM",
    val category: String = "General", // "Morning", "Work", "Personal", "Health", "Evening", "Diary" or Custom
    val notifyMe: Boolean = true,
    val notificationScheduledId: Int = 0,
    val isCompleted: Boolean = false,
    val colorHex: String = "#F59E0B"
)

@Entity(tableName = "events")
data class EventEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val locationOrNote: String = "",
    val eventTimestamp: Long,
    val notifyMe: Boolean = true,
    val notificationScheduledId: Int = 0,
    val category: String = "General",
    val includeYear: Boolean = true,
    val isAllDay: Boolean = false,
    val imageUri: String? = null,
    val isCompleted: Boolean = false,
    val eventType: String = "IMPORTANT_DATE"
)

@Entity(tableName = "vault_documents")
data class VaultDocumentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val originalFileName: String,
    val uriString: String,
    val fileType: String = "PDF", // PDF, IMAGE, DOC, CERTIFICATE, RECEIPT
    val category: String = "Personal", // ID, Finance, Legal, Insurance, Health, Personal
    val dateAddedTimestamp: Long = System.currentTimeMillis(),
    val fileSizeBytes: Long = 0L,
    val notes: String = ""
)

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val scheduledTimestamp: Long? = null,
    val category: String = "General",
    val isCompleted: Boolean = false,
    val completedTimestamp: Long? = null,
    val notifyMe: Boolean = false,
    val notificationScheduledId: Int = 0,
    val attachmentUris: String = ""
)

@Entity(tableName = "category_contacts")
data class CategoryContactEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val category: String, // "Family", "Work", "Personal", "Utility"
    val name: String,
    val phoneNumber: String,
    val note: String = "",
    val addedTimestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "music_tracks")
data class MusicTrackEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val songName: String,
    val albumName: String,
    val category: String, // Melody, Classical, Pop, Jazz, Ambient, Acoustic, Devotional, etc.
    val artist: String = "",
    val uriString: String = "",
    val fileName: String = "",
    val fileSizeBytes: Long = 0L,
    val durationMs: Long = 0L,
    val isFavorite: Boolean = false,
    val sourceType: String = "LOCAL_STORAGE", // "GOOGLE_DRIVE", "LOCAL_STORAGE", "INTERNAL"
    val notes: String = "",
    val addedTimestamp: Long = System.currentTimeMillis()
)

data class ArchiveMessagesReport(
    val identifiedCount: Int,
    val archivedCount: Int,
    val archivedMessages: List<ChatMessageEntity>,
    val affectedThreads: List<String>,
    val thresholdDays: Int = 5,
    val cutoffTimestamp: Long
)

@Entity(tableName = "downloaded_videos")
data class DownloadedVideoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val originalUrl: String,
    val platform: String, // "INSTAGRAM", "FACEBOOK", "WEB"
    val localFilePath: String = "",
    val thumbnailUri: String = "",
    val fileSizeBytes: Long = 0L,
    val durationSeconds: Int = 0,
    val downloadTimestamp: Long = System.currentTimeMillis(),
    val status: String = "COMPLETED", // "DOWNLOADING", "COMPLETED", "FAILED"
    val authorName: String = "",
    val quality: String = "HD 720p"
)


