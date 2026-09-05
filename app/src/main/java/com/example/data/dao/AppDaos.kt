package com.example.data.dao

import androidx.room.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatDao {
    @Query("SELECT * FROM chat_threads ORDER BY lastMessageTimestamp DESC")
    fun getAllThreads(): Flow<List<ChatThreadEntity>>

    @Query("SELECT * FROM chat_threads")
    suspend fun getAllThreadsSnapshot(): List<ChatThreadEntity>

    @Query("SELECT * FROM chat_threads WHERE threadKey = :key LIMIT 1")
    suspend fun getThreadByKey(key: String): ChatThreadEntity?

    @Query("SELECT * FROM chat_messages WHERE threadKey = :threadKey ORDER BY timestamp DESC")
    fun getMessagesForThread(threadKey: String): Flow<List<ChatMessageEntity>>

    @Query("SELECT * FROM chat_messages WHERE threadKey = :threadKey AND isArchived = 0 ORDER BY timestamp DESC")
    fun getActiveMessagesForThread(threadKey: String): Flow<List<ChatMessageEntity>>

    @Query("SELECT * FROM chat_messages WHERE threadKey = :threadKey AND isArchived = 1 ORDER BY timestamp DESC")
    fun getArchivedMessagesForThread(threadKey: String): Flow<List<ChatMessageEntity>>

    @Query("SELECT * FROM chat_messages WHERE isArchived = 1 ORDER BY timestamp DESC")
    fun getAllArchivedMessages(): Flow<List<ChatMessageEntity>>

    @Query("SELECT COUNT(*) FROM chat_messages WHERE isArchived = 1")
    fun getArchivedCount(): Flow<Int>

    @Query("SELECT * FROM chat_messages WHERE isPendingSync = 1 ORDER BY timestamp ASC")
    fun getPendingSyncMessages(): Flow<List<ChatMessageEntity>>

    @Query("SELECT COUNT(*) FROM chat_messages WHERE isPendingSync = 1")
    fun getPendingSyncCount(): Flow<Int>

    @Query("SELECT * FROM chat_messages WHERE isPendingSync = 1 ORDER BY timestamp ASC")
    suspend fun getPendingSyncMessagesSnapshot(): List<ChatMessageEntity>

    @Query("UPDATE chat_messages SET isPendingSync = 0, syncedTimestamp = :syncTime WHERE id = :messageId")
    suspend fun markMessageSynced(messageId: Long, syncTime: Long)

    @Query("UPDATE chat_messages SET isPendingSync = 0, syncedTimestamp = :syncTime WHERE isPendingSync = 1")
    suspend fun markAllPendingSynced(syncTime: Long)

    @Query("SELECT * FROM chat_messages WHERE id = :messageId LIMIT 1")
    suspend fun getMessageById(messageId: Long): ChatMessageEntity?

    @Query("SELECT * FROM chat_messages WHERE threadKey = :threadKey ORDER BY timestamp DESC")
    suspend fun getMessagesForThreadSnapshot(threadKey: String): List<ChatMessageEntity>

    @Query("SELECT * FROM chat_messages ORDER BY timestamp ASC")
    suspend fun getAllMessagesSnapshot(): List<ChatMessageEntity>

    @Query("SELECT * FROM chat_messages WHERE isArchived = 0 AND timestamp < :thresholdTimestamp ORDER BY timestamp ASC")
    suspend fun getUnarchivedMessagesOlderThan(thresholdTimestamp: Long): List<ChatMessageEntity>

    @Query("SELECT * FROM chat_messages WHERE isArchived = 1 AND threadKey = :threadKey ORDER BY timestamp DESC")
    suspend fun getArchivedMessagesForThreadSnapshot(threadKey: String): List<ChatMessageEntity>

    @Query("UPDATE chat_messages SET isArchived = 1, syncedTimestamp = :syncTime WHERE isArchived = 0 AND timestamp < :thresholdTimestamp")
    suspend fun autoSyncAndArchiveOldMessages(thresholdTimestamp: Long, syncTime: Long): Int

    @Query("UPDATE chat_messages SET isArchived = 0 WHERE id = :messageId")
    suspend fun restoreArchivedMessage(messageId: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertThread(thread: ChatThreadEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertThreads(threads: List<ChatThreadEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessageEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessages(messages: List<ChatMessageEntity>)

    @Query("UPDATE chat_threads SET lastMessagePreview = :preview, lastMessageTimestamp = :timestamp WHERE threadKey = :threadKey")
    suspend fun updateThreadLastMessage(threadKey: String, preview: String, timestamp: Long)

    @Query("UPDATE chat_messages SET content = :newContent WHERE id = :messageId")
    suspend fun updateMessageContent(messageId: Long, newContent: String)

    @Query("UPDATE chat_threads SET name = :newName, iconEmoji = :newEmoji WHERE threadKey = :threadKey")
    suspend fun updateThreadDetails(threadKey: String, newName: String, newEmoji: String)

    @Query("UPDATE chat_threads SET category = :newCategory WHERE category = :oldCategory")
    suspend fun updateThreadCategoryName(oldCategory: String, newCategory: String)

    @Query("UPDATE chat_messages SET threadKey = :newThreadKey WHERE threadKey = :oldThreadKey")
    suspend fun updateMessagesThreadKey(oldThreadKey: String, newThreadKey: String)

    @Query("UPDATE chat_threads SET threadKey = :newThreadKey, name = :newName, category = :newCategory, iconEmoji = :newEmoji WHERE threadKey = :oldThreadKey")
    suspend fun renameThreadAndKey(oldThreadKey: String, newThreadKey: String, newName: String, newCategory: String, newEmoji: String)

    @Delete
    suspend fun deleteMessage(message: ChatMessageEntity)

    @Query("DELETE FROM chat_threads WHERE threadKey = :threadKey")
    suspend fun deleteThread(threadKey: String)

    @Query("DELETE FROM chat_messages WHERE threadKey = :threadKey")
    suspend fun clearMessagesForThread(threadKey: String)

    @Query("SELECT COUNT(*) FROM chat_threads")
    suspend fun getThreadCount(): Int
}

@Dao
interface CategoryContactDao {
    @Query("SELECT * FROM category_contacts WHERE category = :category ORDER BY name ASC")
    fun getContactsForCategory(category: String): Flow<List<CategoryContactEntity>>

    @Query("SELECT * FROM category_contacts ORDER BY category ASC, name ASC")
    fun getAllContacts(): Flow<List<CategoryContactEntity>>

    @Query("SELECT * FROM category_contacts")
    suspend fun getAllContactsSnapshot(): List<CategoryContactEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContact(contact: CategoryContactEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContacts(contacts: List<CategoryContactEntity>)

    @Delete
    suspend fun deleteContact(contact: CategoryContactEntity)

    @Query("DELETE FROM category_contacts WHERE id = :id")
    suspend fun deleteContactById(id: Long)

    @Query("SELECT COUNT(*) FROM category_contacts WHERE category = :category")
    suspend fun getContactCountForCategory(category: String): Int

    @Query("UPDATE category_contacts SET category = :newCategory WHERE category = :oldCategory")
    suspend fun updateCategoryName(oldCategory: String, newCategory: String)

    @Query("SELECT COUNT(*) FROM category_contacts")
    suspend fun getContactCount(): Int
}

@Dao
interface DiaryDao {
    @Query("SELECT * FROM diary_entries ORDER BY isPinned DESC, dateTimestamp DESC")
    fun getAllEntries(): Flow<List<DiaryEntryEntity>>

    @Query("SELECT * FROM diary_entries")
    suspend fun getAllEntriesSnapshot(): List<DiaryEntryEntity>

    @Query("SELECT * FROM diary_entries WHERE id = :id LIMIT 1")
    suspend fun getEntryById(id: Long): DiaryEntryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: DiaryEntryEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntries(entries: List<DiaryEntryEntity>)

    @Update
    suspend fun updateEntry(entry: DiaryEntryEntity)

    @Delete
    suspend fun deleteEntry(entry: DiaryEntryEntity)

    @Query("SELECT COUNT(*) FROM diary_entries")
    suspend fun getEntryCount(): Int
}

@Dao
interface EventDao {
    @Query("SELECT * FROM events ORDER BY eventTimestamp ASC")
    fun getAllEvents(): Flow<List<EventEntity>>

    @Query("SELECT * FROM events")
    suspend fun getAllEventsSnapshot(): List<EventEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: EventEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvents(events: List<EventEntity>)

    @Update
    suspend fun updateEvent(event: EventEntity)

    @Delete
    suspend fun deleteEvent(event: EventEntity)

    @Query("SELECT COUNT(*) FROM events")
    suspend fun getEventCount(): Int
}

@Dao
interface VaultDao {
    @Query("SELECT * FROM vault_documents ORDER BY dateAddedTimestamp DESC")
    fun getAllDocuments(): Flow<List<VaultDocumentEntity>>

    @Query("SELECT * FROM vault_documents")
    suspend fun getAllDocumentsSnapshot(): List<VaultDocumentEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDocument(document: VaultDocumentEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDocuments(documents: List<VaultDocumentEntity>)

    @Update
    suspend fun updateDocument(document: VaultDocumentEntity)

    @Delete
    suspend fun deleteDocument(document: VaultDocumentEntity)

    @Query("SELECT COUNT(*) FROM vault_documents")
    suspend fun getDocumentCount(): Int
}

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks ORDER BY isCompleted ASC, CASE WHEN scheduledTimestamp IS NULL THEN 1 ELSE 0 END, scheduledTimestamp ASC, id DESC")
    fun getAllTasks(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks")
    suspend fun getAllTasksSnapshot(): List<TaskEntity>

    @Query("SELECT * FROM tasks WHERE id = :id LIMIT 1")
    suspend fun getTaskById(id: Long): TaskEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTasks(tasks: List<TaskEntity>)

    @Update
    suspend fun updateTask(task: TaskEntity)

    @Delete
    suspend fun deleteTask(task: TaskEntity)

    @Query("SELECT COUNT(*) FROM tasks")
    suspend fun getTaskCount(): Int
}

@Dao
interface MusicDao {
    @Query("SELECT * FROM music_tracks ORDER BY addedTimestamp DESC")
    fun getAllTracks(): Flow<List<MusicTrackEntity>>

    @Query("SELECT * FROM music_tracks")
    suspend fun getAllTracksSnapshot(): List<MusicTrackEntity>

    @Query("SELECT * FROM music_tracks WHERE id = :id LIMIT 1")
    suspend fun getTrackById(id: Long): MusicTrackEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrack(track: MusicTrackEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTracks(tracks: List<MusicTrackEntity>)

    @Update
    suspend fun updateTrack(track: MusicTrackEntity)

    @Delete
    suspend fun deleteTrack(track: MusicTrackEntity)

    @Query("UPDATE music_tracks SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun updateFavorite(id: Long, isFavorite: Boolean)

    @Query("SELECT COUNT(*) FROM music_tracks")
    suspend fun getTrackCount(): Int
}

@Dao
interface VideoDao {
    @Query("SELECT * FROM downloaded_videos ORDER BY downloadTimestamp DESC")
    fun getAllVideos(): Flow<List<DownloadedVideoEntity>>

    @Query("SELECT * FROM downloaded_videos WHERE id = :id LIMIT 1")
    suspend fun getVideoById(id: Long): DownloadedVideoEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVideo(video: DownloadedVideoEntity): Long

    @Update
    suspend fun updateVideo(video: DownloadedVideoEntity)

    @Delete
    suspend fun deleteVideo(video: DownloadedVideoEntity)

    @Query("DELETE FROM downloaded_videos WHERE id = :id")
    suspend fun deleteVideoById(id: Long)

    @Query("SELECT COUNT(*) FROM downloaded_videos")
    suspend fun getVideoCount(): Int
}

@Dao
interface DailyScheduleDao {
    @Query("SELECT * FROM daily_schedules ORDER BY timestamp ASC")
    fun getAllSchedules(): Flow<List<DailyScheduleEntity>>

    @Query("SELECT * FROM daily_schedules WHERE (timestamp >= :startOfDay AND timestamp <= :endOfDay) OR (isMultiDay = 1 AND timestamp <= :endOfDay AND (endTimestamp IS NULL OR endTimestamp >= :startOfDay)) ORDER BY isCompleted ASC, timestamp ASC")
    fun getSchedulesForDateRange(startOfDay: Long, endOfDay: Long): Flow<List<DailyScheduleEntity>>

    @Query("SELECT * FROM daily_schedules WHERE recurrence = :recurrence ORDER BY timestamp ASC")
    fun getSchedulesByRecurrence(recurrence: String): Flow<List<DailyScheduleEntity>>

    @Query("SELECT * FROM daily_schedules")
    suspend fun getAllSchedulesSnapshot(): List<DailyScheduleEntity>

    @Query("SELECT * FROM daily_schedules WHERE id = :id LIMIT 1")
    suspend fun getScheduleById(id: Long): DailyScheduleEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedule(schedule: DailyScheduleEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedules(schedules: List<DailyScheduleEntity>)

    @Update
    suspend fun updateSchedule(schedule: DailyScheduleEntity)

    @Query("UPDATE daily_schedules SET isCompleted = :isCompleted WHERE id = :id")
    suspend fun updateScheduleCompletion(id: Long, isCompleted: Boolean)

    @Delete
    suspend fun deleteSchedule(schedule: DailyScheduleEntity)

    @Query("DELETE FROM daily_schedules WHERE id = :id")
    suspend fun deleteScheduleById(id: Long)

    @Query("SELECT COUNT(*) FROM daily_schedules")
    suspend fun getScheduleCount(): Int
}


