package com.example.data

import android.content.Context
import android.net.Uri
import com.example.ai.ChatAi
import com.example.ai.ChatAi.Message
import com.example.data.dao.*
import com.example.data.model.*
import com.example.util.NotificationHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.util.Calendar
import java.io.File

class LedgerRepository(
    private val database: AppDatabase,
    private val context: Context
) {
    private val chatDao: ChatDao = database.chatDao()
    private val categoryContactDao: CategoryContactDao = database.categoryContactDao()
    private val diaryDao: DiaryDao = database.diaryDao()
    private val dailyScheduleDao: DailyScheduleDao = database.dailyScheduleDao()
    private val eventDao: EventDao = database.eventDao()
    private val vaultDao: VaultDao = database.vaultDao()
    private val taskDao: TaskDao = database.taskDao()
    private val musicDao: MusicDao = database.musicDao()
    private val videoDao: VideoDao = database.videoDao()

    // ----------------------------------------------------
    // CHAT
    // ----------------------------------------------------
    val allChatThreads: Flow<List<ChatThreadEntity>> = chatDao.getAllThreads()

    fun getMessagesForThread(threadKey: String): Flow<List<ChatMessageEntity>> =
        chatDao.getActiveMessagesForThread(threadKey)

    fun getAllMessagesForThread(threadKey: String): Flow<List<ChatMessageEntity>> =
        chatDao.getMessagesForThread(threadKey)

    fun getArchivedMessagesForThread(threadKey: String): Flow<List<ChatMessageEntity>> =
        chatDao.getArchivedMessagesForThread(threadKey)

    val allArchivedMessages: Flow<List<ChatMessageEntity>> =
        chatDao.getAllArchivedMessages()

    val archivedMessageCount: Flow<Int> =
        chatDao.getArchivedCount()

    val pendingSyncMessages: Flow<List<ChatMessageEntity>> =
        chatDao.getPendingSyncMessages()

    val pendingSyncCount: Flow<Int> =
        chatDao.getPendingSyncCount()

    /**
     * Identifies chat messages older than [days] (default 5 days) and moves them
     * to the hidden Vault Archive section, ensuring the active chat view remains
     * clean, uncluttered, and focused strictly on recent activity.
     */
    suspend fun identifyAndArchiveOldChatMessages(days: Int = 5): ArchiveMessagesReport = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        val cutoffTimestamp = now - (days.toLong() * 24L * 60L * 60L * 1000L)
        
        // 1. Identify unarchived messages older than cutoff
        val oldMessages = chatDao.getUnarchivedMessagesOlderThan(cutoffTimestamp)
        
        if (oldMessages.isEmpty()) {
            return@withContext ArchiveMessagesReport(
                identifiedCount = 0,
                archivedCount = 0,
                archivedMessages = emptyList(),
                affectedThreads = emptyList(),
                thresholdDays = days,
                cutoffTimestamp = cutoffTimestamp
            )
        }

        // 2. Move them to Archive (set isArchived = 1, syncedTimestamp = now)
        val archivedRows = chatDao.autoSyncAndArchiveOldMessages(thresholdTimestamp = cutoffTimestamp, syncTime = now)
        val affectedThreads = oldMessages.map { it.threadKey }.distinct()

        // Move attachments of archived messages into Google Drive Cloud Archive
        try {
            val gDriveDir = File(context.filesDir, "google_drive_archived_attachments").apply { mkdirs() }
            for (msg in oldMessages) {
                if (!msg.attachmentUri.isNullOrBlank()) {
                    val srcUri = Uri.parse(msg.attachmentUri)
                    if (srcUri.scheme == "file") {
                        val srcFile = File(srcUri.path ?: "")
                        if (srcFile.exists()) {
                            val destFile = File(gDriveDir, srcFile.name)
                            srcFile.copyTo(destFile, overwrite = true)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // 3. Update thread preview to reflect remaining active messages
        for (threadKey in affectedThreads) {
            val latestActive = chatDao.getMessagesForThreadSnapshot(threadKey).firstOrNull { !it.isArchived }
            if (latestActive != null) {
                val prefix = if (latestActive.isPendingSync) "⏳ [Queued] " else ""
                val preview = if (latestActive.content.isNotBlank()) "$prefix${latestActive.content}" else (latestActive.attachmentName?.let { "$prefix📎 $it" } ?: "$prefix📎 Attachment")
                chatDao.updateThreadLastMessage(threadKey, preview, latestActive.timestamp)
            } else {
                val thread = chatDao.getThreadByKey(threadKey)
                chatDao.updateThreadLastMessage(threadKey, "No active messages", thread?.lastMessageTimestamp ?: System.currentTimeMillis())
            }
        }

        ArchiveMessagesReport(
            identifiedCount = oldMessages.size,
            archivedCount = archivedRows,
            archivedMessages = oldMessages,
            affectedThreads = affectedThreads,
            thresholdDays = days,
            cutoffTimestamp = cutoffTimestamp
        )
    }

    suspend fun runAutoSyncAndArchive(): Int = withContext(Dispatchers.IO) {
        val report = identifyAndArchiveOldChatMessages(days = 5)
        report.archivedCount
    }

    suspend fun syncQueuedMessages(): Int = withContext(Dispatchers.IO) {
        val pending = chatDao.getPendingSyncMessagesSnapshot()
        if (pending.isEmpty()) return@withContext 0

        val now = System.currentTimeMillis()
        chatDao.markAllPendingSynced(now)

        // Generate AI responses for any user messages that were pending sync
        val userMessages = pending.filter { it.isSentByUser }
        val affectedThreads = userMessages.map { it.threadKey }.distinct()
        for (threadKey in affectedThreads) {
            try {
                generateAndStoreReply(threadKey)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        pending.size
    }

    private suspend fun refreshThreadPreview(threadKey: String) {
        val latestActive = chatDao.getMessagesForThreadSnapshot(threadKey).firstOrNull { !it.isArchived }
        if (latestActive != null) {
            val prefix = if (latestActive.isPendingSync) "⏳ [Queued] " else ""
            val preview = if (latestActive.content.isNotBlank()) "$prefix${latestActive.content}" else (latestActive.attachmentName?.let { "$prefix📎 $it" } ?: "$prefix📎 Attachment")
            chatDao.updateThreadLastMessage(threadKey, preview, latestActive.timestamp)
        } else {
            val thread = chatDao.getThreadByKey(threadKey)
            chatDao.updateThreadLastMessage(threadKey, "No active messages", thread?.lastMessageTimestamp ?: System.currentTimeMillis())
        }
    }

    suspend fun restoreArchivedMessage(messageId: Long) = withContext(Dispatchers.IO) {
        val msg = chatDao.getMessageById(messageId)
        chatDao.restoreArchivedMessage(messageId)
        if (msg != null) {
            refreshThreadPreview(msg.threadKey)
        }
    }

    suspend fun deleteMessage(message: ChatMessageEntity) = withContext(Dispatchers.IO) {
        chatDao.deleteMessage(message)
        refreshThreadPreview(message.threadKey)
    }

    suspend fun sendMessage(
        threadKey: String,
        content: String,
        isSentByUser: Boolean = true,
        senderName: String = "You",
        isPendingSync: Boolean = false,
        attachmentUri: String? = null,
        attachmentName: String? = null,
        attachmentType: String? = null,
        attachmentSizeBytes: Long = 0L
    ) = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        val message = ChatMessageEntity(
            threadKey = threadKey,
            content = content,
            isSentByUser = isSentByUser,
            senderName = senderName,
            timestamp = now,
            isArchived = false,
            syncedTimestamp = if (!isPendingSync) now else null,
            isPendingSync = isPendingSync,
            attachmentUri = attachmentUri,
            attachmentName = attachmentName,
            attachmentType = attachmentType,
            attachmentSizeBytes = attachmentSizeBytes
        )
        chatDao.insertMessage(message)
        val prefix = if (isPendingSync) "⏳ [Queued] " else ""
        val preview = if (content.isNotBlank()) "$prefix$content" else (attachmentName?.let { "$prefix📎 $it" } ?: "Attachment")
        chatDao.updateThreadLastMessage(threadKey, preview, now)

        if (isSentByUser && !isPendingSync) {
            generateAndStoreReply(threadKey)
        }
    }

    /**
     * Generates a "MyLyfe Desk" assistant reply for the given thread and persists it.
     * Runs as a sub-coroutine so the user's message is already stored first. Failures
     * are swallowed (a fallback reply is inserted) so the Chat thread is never left
     * without a response.
     */
    private suspend fun generateAndStoreReply(threadKey: String) = withContext(Dispatchers.IO) {
        // Pull existing messages to build conversation context for the assistant.
        val prior = chatDao.getMessagesForThreadSnapshot(threadKey)
        val chatHistory = prior.reversed().map { Message(isUser = it.isSentByUser, text = it.content) }
        val lastUser = prior.firstOrNull { it.isSentByUser }?.content.orEmpty()

        val replyText = try {
            ChatAi.generateReply(chatHistory, lastUser)
        } catch (e: Exception) {
            "I'm here, but I can't reach the assistant right now. Your message is saved in this thread."
        }

        val replyNow = System.currentTimeMillis()
        chatDao.insertMessage(
            ChatMessageEntity(
                threadKey = threadKey,
                content = replyText,
                isSentByUser = false,
                senderName = "MyLyfe Desk",
                timestamp = replyNow
            )
        )
        chatDao.updateThreadLastMessage(threadKey, replyText, replyNow)
    }

    suspend fun createThread(name: String, category: String, iconEmoji: String): String = withContext(Dispatchers.IO) {
        val key = name.trim().lowercase().replace(" ", "_") + "_" + (System.currentTimeMillis() % 1000)
        val thread = ChatThreadEntity(
            threadKey = key,
            name = name.trim(),
            category = category,
            iconEmoji = iconEmoji,
            lastMessagePreview = "Thread created",
            lastMessageTimestamp = System.currentTimeMillis()
        )
        chatDao.insertThread(thread)
        key
    }

    suspend fun editMessage(messageId: Long, newContent: String) = withContext(Dispatchers.IO) {
        val msg = chatDao.getMessageById(messageId)
        chatDao.updateMessageContent(messageId, newContent.trim())
        if (msg != null) {
            refreshThreadPreview(msg.threadKey)
        }
    }

    suspend fun updateThreadDetails(threadKey: String, newName: String, newEmoji: String) = withContext(Dispatchers.IO) {
        chatDao.updateThreadDetails(threadKey, newName.trim(), newEmoji.trim())
    }

    suspend fun renameCategoryAcrossApp(oldCategory: String, newCategory: String) = withContext(Dispatchers.IO) {
        if (oldCategory.isBlank() || newCategory.isBlank() || oldCategory.equals(newCategory, ignoreCase = true)) return@withContext
        val trimmedNew = newCategory.trim()
        val trimmedOld = oldCategory.trim()
        // Update chat thread categories
        chatDao.updateThreadCategoryName(trimmedOld, trimmedNew)
        // Also update contacts associated with that category
        categoryContactDao.updateCategoryName(trimmedOld, trimmedNew)
    }

    suspend fun deleteThread(threadKey: String) = withContext(Dispatchers.IO) {
        chatDao.clearMessagesForThread(threadKey)
        chatDao.deleteThread(threadKey)
    }

    // ----------------------------------------------------
    // CATEGORY CONTACTS / MOBILE NUMBERS
    // ----------------------------------------------------
    fun getContactsForCategory(category: String): Flow<List<CategoryContactEntity>> =
        categoryContactDao.getContactsForCategory(category)

    val allCategoryContacts: Flow<List<CategoryContactEntity>> =
        categoryContactDao.getAllContacts()

    suspend fun getContactCountForCategory(category: String): Int = withContext(Dispatchers.IO) {
        categoryContactDao.getContactCountForCategory(category.trim())
    }

    suspend fun addCategoryContact(category: String, name: String, phoneNumber: String, note: String = ""): Boolean = withContext(Dispatchers.IO) {
        val currentCount = categoryContactDao.getContactCountForCategory(category.trim())
        if (currentCount >= 10) {
            return@withContext false // Maximum 10 numbers reached for this category
        }
        val contact = CategoryContactEntity(
            category = category.trim(),
            name = name.trim(),
            phoneNumber = phoneNumber.trim(),
            note = note.trim(),
            addedTimestamp = System.currentTimeMillis()
        )
        categoryContactDao.insertContact(contact)
        true
    }

    suspend fun deleteCategoryContact(id: Long) = withContext(Dispatchers.IO) {
        categoryContactDao.deleteContactById(id)
    }

    suspend fun deleteCategoryContact(contact: CategoryContactEntity) = withContext(Dispatchers.IO) {
        categoryContactDao.deleteContact(contact)
    }

    suspend fun getAllContactsSnapshot(): List<CategoryContactEntity> = withContext(Dispatchers.IO) {
        categoryContactDao.getAllContactsSnapshot()
    }

    suspend fun insertContacts(contacts: List<CategoryContactEntity>) = withContext(Dispatchers.IO) {
        categoryContactDao.insertContacts(contacts)
    }

    // ----------------------------------------------------
    // BACKUP & RESTORE SNAPSHOTS
    // ----------------------------------------------------
    suspend fun getAllThreadsSnapshot(): List<ChatThreadEntity> = withContext(Dispatchers.IO) {
        chatDao.getAllThreadsSnapshot()
    }

    suspend fun getAllMessagesSnapshot(): List<ChatMessageEntity> = withContext(Dispatchers.IO) {
        chatDao.getAllMessagesSnapshot()
    }

    suspend fun getAllDiaryEntriesSnapshot(): List<DiaryEntryEntity> = withContext(Dispatchers.IO) {
        diaryDao.getAllEntriesSnapshot()
    }

    suspend fun getAllEventsSnapshot(): List<EventEntity> = withContext(Dispatchers.IO) {
        eventDao.getAllEventsSnapshot()
    }

    suspend fun getAllVaultDocsSnapshot(): List<VaultDocumentEntity> = withContext(Dispatchers.IO) {
        vaultDao.getAllDocumentsSnapshot()
    }

    suspend fun getAllTasksSnapshot(): List<TaskEntity> = withContext(Dispatchers.IO) {
        taskDao.getAllTasksSnapshot()
    }

    suspend fun insertThreads(threads: List<ChatThreadEntity>) = withContext(Dispatchers.IO) {
        chatDao.insertThreads(threads)
    }

    suspend fun insertMessages(messages: List<ChatMessageEntity>) = withContext(Dispatchers.IO) {
        chatDao.insertMessages(messages)
    }

    suspend fun insertDiaryEntries(entries: List<DiaryEntryEntity>) = withContext(Dispatchers.IO) {
        diaryDao.insertEntries(entries)
    }

    suspend fun insertEvents(events: List<EventEntity>) = withContext(Dispatchers.IO) {
        eventDao.insertEvents(events)
    }

    suspend fun insertVaultDocuments(docs: List<VaultDocumentEntity>) = withContext(Dispatchers.IO) {
        vaultDao.insertDocuments(docs)
    }

    suspend fun insertTasks(tasks: List<TaskEntity>) = withContext(Dispatchers.IO) {
        taskDao.insertTasks(tasks)
    }

    // ----------------------------------------------------
    // DIARY
    // ----------------------------------------------------
    val allDiaryEntries: Flow<List<DiaryEntryEntity>> = diaryDao.getAllEntries()

    suspend fun addDiaryEntry(title: String, body: String, moodOrTag: String, isPinned: Boolean, notifyMe: Boolean, timestamp: Long = System.currentTimeMillis(), imageUri: String? = null) =
        withContext(Dispatchers.IO) {
            val notificationId = (System.currentTimeMillis() % 100000).toInt()
            val entry = DiaryEntryEntity(
                title = title.trim(),
                body = body.trim(),
                dateTimestamp = timestamp,
                moodOrTag = moodOrTag.trim().ifEmpty { "Reflection" },
                isPinned = isPinned,
                notifyMe = notifyMe,
                notificationScheduledId = if (notifyMe) notificationId else 0,
                imageUri = imageUri
            )
            val id = diaryDao.insertEntry(entry)

            if (notifyMe && timestamp > System.currentTimeMillis()) {
                NotificationHelper.scheduleReminder(
                    context = context,
                    notificationId = notificationId,
                    title = title.trim(),
                    message = "Diary entry scheduled: ${title.trim()}",
                    timestampMillis = timestamp,
                    type = "DIARY"
                )
            }
            id
        }

    suspend fun updateDiaryEntry(entry: DiaryEntryEntity) = withContext(Dispatchers.IO) {
        // Fetch existing entry to compare notifyMe and timestamp
        val existing = diaryDao.getEntryById(entry.id) ?: return@withContext diaryDao.updateEntry(entry)

        val notifyChanged = existing.notifyMe != entry.notifyMe
        val timeChanged = existing.dateTimestamp != entry.dateTimestamp

        // If notification was scheduled and now needs to be cancelled or rescheduled
        if (existing.notifyMe && existing.notificationScheduledId != 0) {
            NotificationHelper.cancelReminder(context, existing.notificationScheduledId)
        }

        // Update the entry first
        diaryDao.updateEntry(entry)

        // If notifyMe is true and timestamp is in the future, schedule new reminder
        if (entry.notifyMe && entry.dateTimestamp > System.currentTimeMillis()) {
            val notificationId = if (notifyChanged || timeChanged) {
                (System.currentTimeMillis() % 100000).toInt()
            } else {
                existing.notificationScheduledId
            }
            // Update the entry with new notificationId if changed
            if (notifyChanged || timeChanged) {
                val updatedWithId = entry.copy(notificationScheduledId = notificationId)
                diaryDao.updateEntry(updatedWithId)
            }
            NotificationHelper.scheduleReminder(
                context = context,
                notificationId = notificationId,
                title = entry.title,
                message = "Diary entry scheduled: ${entry.title}",
                timestampMillis = entry.dateTimestamp,
                type = "DIARY"
            )
        }
    }

    suspend fun deleteDiaryEntry(entry: DiaryEntryEntity) = withContext(Dispatchers.IO) {
        diaryDao.deleteEntry(entry)
    }

    // ----------------------------------------------------
    // EVENTS
    // ----------------------------------------------------
    val allEvents: Flow<List<EventEntity>> = eventDao.getAllEvents()

    suspend fun addEvent(
        title: String,
        locationOrNote: String,
        eventTimestamp: Long,
        notifyMe: Boolean,
        category: String = "General",
        includeYear: Boolean = true,
        isAllDay: Boolean = false,
        imageUri: String? = null,
        eventType: String = "IMPORTANT_DATE",
        isCompleted: Boolean = false
    ) = withContext(Dispatchers.IO) {
        val notificationId = (System.currentTimeMillis() % 100000).toInt()
        val event = EventEntity(
            title = title.trim(),
            locationOrNote = locationOrNote.trim(),
            eventTimestamp = eventTimestamp,
            notifyMe = notifyMe,
            notificationScheduledId = notificationId,
            category = category,
            includeYear = includeYear,
            isAllDay = isAllDay,
            imageUri = imageUri,
            isCompleted = isCompleted,
            eventType = eventType
        )
        val id = eventDao.insertEvent(event)

        if (notifyMe) {
            NotificationHelper.scheduleReminder(
                context = context,
                notificationId = notificationId,
                title = title.trim(),
                message = if (locationOrNote.isNotBlank()) "Note: $locationOrNote" else "Upcoming reminder in MyLyfe",
                timestampMillis = eventTimestamp,
                type = "EVENT"
            )
        }
        id
    }

    suspend fun toggleEventCompleted(event: EventEntity) = withContext(Dispatchers.IO) {
        val updated = event.copy(isCompleted = !event.isCompleted)
        eventDao.updateEvent(updated)
    }

    suspend fun updateEvent(event: EventEntity) = withContext(Dispatchers.IO) {
        eventDao.updateEvent(event)
        if (event.notifyMe) {
            NotificationHelper.scheduleReminder(
                context = context,
                notificationId = event.notificationScheduledId,
                title = event.title,
                message = if (event.locationOrNote.isNotBlank()) "Location: ${event.locationOrNote}" else "Scheduled event in MyLyfe",
                timestampMillis = event.eventTimestamp,
                type = "EVENT"
            )
        } else {
            NotificationHelper.cancelReminder(context, event.notificationScheduledId)
        }
    }

    suspend fun deleteEvent(event: EventEntity) = withContext(Dispatchers.IO) {
        if (event.notifyMe && event.notificationScheduledId != 0) {
            NotificationHelper.cancelReminder(context, event.notificationScheduledId)
        }
        eventDao.deleteEvent(event)
    }

    suspend fun toggleEventNotification(event: EventEntity) = withContext(Dispatchers.IO) {
        val newNotify = !event.notifyMe
        val notifId = if (event.notificationScheduledId != 0) event.notificationScheduledId else (System.currentTimeMillis() % 100000).toInt()
        val updated = event.copy(notifyMe = newNotify, notificationScheduledId = notifId)
        eventDao.updateEvent(updated)

        if (newNotify) {
            NotificationHelper.scheduleReminder(
                context = context,
                notificationId = notifId,
                title = event.title,
                message = if (event.locationOrNote.isNotBlank()) "Location: ${event.locationOrNote}" else "Scheduled event in MyLyfe",
                timestampMillis = event.eventTimestamp,
                type = "EVENT"
            )
        } else {
            NotificationHelper.cancelReminder(context, notifId)
        }
    }

    // ----------------------------------------------------
    // DAILY SCHEDULE & DIARY
    // ----------------------------------------------------
    val allDailySchedules: Flow<List<DailyScheduleEntity>> = dailyScheduleDao.getAllSchedules()

    suspend fun seedDefaultSampleSchedulesIfEmpty() = withContext(Dispatchers.IO) {
        val existing = dailyScheduleDao.getAllSchedulesSnapshot()
        if (existing.isEmpty()) {
            loadSampleRoutine()
        }
    }

    suspend fun loadSampleRoutine() = withContext(Dispatchers.IO) {
        val cal = Calendar.getInstance()

        // 1. 8:00 AM - Yoga & Tea
        cal.set(Calendar.HOUR_OF_DAY, 8)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        dailyScheduleDao.insertSchedule(
            DailyScheduleEntity(
                title = "Yoga & Tea",
                note = "Morning mindfulness and herbal tea routine",
                timestamp = cal.timeInMillis,
                timeSlot = "8:00 AM",
                category = "Health",
                notifyMe = true,
                colorHex = "#10B981"
            )
        )

        // 2. 10:00 AM - Work / Projects
        cal.set(Calendar.HOUR_OF_DAY, 10)
        cal.set(Calendar.MINUTE, 0)
        dailyScheduleDao.insertSchedule(
            DailyScheduleEntity(
                title = "Work / Projects",
                note = "Core focus session and project review",
                timestamp = cal.timeInMillis,
                timeSlot = "10:00 AM",
                category = "Work",
                notifyMe = true,
                colorHex = "#2563EB"
            )
        )

        // 3. 1:00 PM - Lunch Break
        cal.set(Calendar.HOUR_OF_DAY, 13)
        cal.set(Calendar.MINUTE, 0)
        dailyScheduleDao.insertSchedule(
            DailyScheduleEntity(
                title = "Lunch Break",
                note = "Healthy lunch and short relaxation walk",
                timestamp = cal.timeInMillis,
                timeSlot = "1:00 PM",
                category = "Meal",
                notifyMe = true,
                colorHex = "#E11D48"
            )
        )

        // 4. 7:00 PM - Reading
        cal.set(Calendar.HOUR_OF_DAY, 19)
        cal.set(Calendar.MINUTE, 0)
        dailyScheduleDao.insertSchedule(
            DailyScheduleEntity(
                title = "Reading",
                note = "Personal development and reading session",
                timestamp = cal.timeInMillis,
                timeSlot = "7:00 PM",
                category = "Personal",
                notifyMe = true,
                colorHex = "#9333EA"
            )
        )
    }

    fun getSchedulesForDateRange(startOfDay: Long, endOfDay: Long): Flow<List<DailyScheduleEntity>> =
        dailyScheduleDao.getSchedulesForDateRange(startOfDay, endOfDay)

    fun getSchedulesByRecurrence(recurrence: String): Flow<List<DailyScheduleEntity>> =
        dailyScheduleDao.getSchedulesByRecurrence(recurrence)

    suspend fun addDailySchedule(
        title: String,
        note: String = "",
        timestamp: Long = System.currentTimeMillis(),
        endTimestamp: Long? = null,
        isMultiDay: Boolean = false,
        recurrence: String = "DAILY",
        timeSlot: String = "09:00 AM",
        category: String = "General",
        notifyMe: Boolean = true,
        colorHex: String = "#F59E0B"
    ) = withContext(Dispatchers.IO) {
        val notifId = (System.currentTimeMillis() % 100000).toInt()
        val schedule = DailyScheduleEntity(
            title = title.trim(),
            note = note.trim(),
            timestamp = timestamp,
            endTimestamp = endTimestamp,
            isMultiDay = isMultiDay,
            recurrence = recurrence,
            timeSlot = timeSlot,
            category = category,
            notifyMe = notifyMe,
            notificationScheduledId = notifId,
            isCompleted = false,
            colorHex = colorHex
        )
        dailyScheduleDao.insertSchedule(schedule)

        if (notifyMe) {
            val recurrenceLabel = when (recurrence) {
                "WEEK" -> "Weekly Schedule"
                "MONTH" -> "Monthly Schedule"
                "ANNUAL" -> "Annual Schedule"
                else -> "Daily Schedule"
            }
            NotificationHelper.scheduleReminder(
                context = context,
                notificationId = notifId,
                title = "[$recurrenceLabel] $title",
                message = if (note.isNotBlank()) note else "Reminder for your scheduled entry at $timeSlot",
                timestampMillis = timestamp,
                type = "TASK"
            )
        }
    }

    suspend fun toggleDailyScheduleComplete(schedule: DailyScheduleEntity) = withContext(Dispatchers.IO) {
        dailyScheduleDao.updateScheduleCompletion(schedule.id, !schedule.isCompleted)
    }

    suspend fun updateDailySchedule(schedule: DailyScheduleEntity) = withContext(Dispatchers.IO) {
        dailyScheduleDao.updateSchedule(schedule)
        if (schedule.notifyMe) {
            val recurrenceLabel = when (schedule.recurrence) {
                "WEEK" -> "Weekly Schedule"
                "MONTH" -> "Monthly Schedule"
                "ANNUAL" -> "Annual Schedule"
                else -> "Daily Schedule"
            }
            NotificationHelper.scheduleReminder(
                context = context,
                notificationId = schedule.notificationScheduledId,
                title = "[$recurrenceLabel] ${schedule.title}",
                message = if (schedule.note.isNotBlank()) schedule.note else "Reminder for your scheduled entry at ${schedule.timeSlot}",
                timestampMillis = schedule.timestamp,
                type = "TASK"
            )
        } else {
            NotificationHelper.cancelReminder(context, schedule.notificationScheduledId)
        }
    }

    suspend fun deleteDailySchedule(schedule: DailyScheduleEntity) = withContext(Dispatchers.IO) {
        if (schedule.notifyMe && schedule.notificationScheduledId != 0) {
            NotificationHelper.cancelReminder(context, schedule.notificationScheduledId)
        }
        dailyScheduleDao.deleteSchedule(schedule)
    }

    // ----------------------------------------------------
    // VAULT
    // ----------------------------------------------------
    val allVaultDocuments: Flow<List<VaultDocumentEntity>> = vaultDao.getAllDocuments()

    suspend fun addVaultDocument(
        title: String,
        originalFileName: String,
        uriString: String,
        fileType: String,
        category: String,
        fileSizeBytes: Long = 0L,
        notes: String = ""
    ) = withContext(Dispatchers.IO) {
        val doc = VaultDocumentEntity(
            title = title.trim(),
            originalFileName = originalFileName.trim().ifEmpty { "attachment" },
            uriString = uriString,
            fileType = fileType,
            category = category,
            dateAddedTimestamp = System.currentTimeMillis(),
            fileSizeBytes = fileSizeBytes,
            notes = notes.trim()
        )
        vaultDao.insertDocument(doc)
    }

    suspend fun deleteVaultDocument(document: VaultDocumentEntity) = withContext(Dispatchers.IO) {
        vaultDao.deleteDocument(document)
    }

    // ----------------------------------------------------
    // TASKS
    // ----------------------------------------------------
    val allTasks: Flow<List<TaskEntity>> = taskDao.getAllTasks()

    suspend fun addTask(
        title: String,
        description: String,
        scheduledTimestamp: Long?,
        category: String,
        notifyMe: Boolean,
        attachmentUris: String
    ) = withContext(Dispatchers.IO) {
        val notifId = (System.currentTimeMillis() % 100000).toInt()
        val task = TaskEntity(
            title = title.trim(),
            description = description.trim(),
            scheduledTimestamp = scheduledTimestamp,
            category = category.trim().ifEmpty { "General" },
            isCompleted = false,
            notifyMe = notifyMe,
            notificationScheduledId = notifId,
            attachmentUris = attachmentUris
        )
        val id = taskDao.insertTask(task)

        if (notifyMe && scheduledTimestamp != null) {
            NotificationHelper.scheduleReminder(
                context = context,
                notificationId = notifId,
                title = title.trim(),
                message = if (description.isNotBlank()) description else "Task due in My Checklist",
                timestampMillis = scheduledTimestamp,
                type = "TASK"
            )
            com.example.util.WorkManagerHelper.scheduleTaskReminder(context, id, scheduledTimestamp)
        }
        id
    }

    suspend fun toggleTaskComplete(task: TaskEntity) = withContext(Dispatchers.IO) {
        val newStatus = !task.isCompleted
        val updated = task.copy(
            isCompleted = newStatus,
            completedTimestamp = if (newStatus) System.currentTimeMillis() else null
        )
        taskDao.updateTask(updated)

        // Cancel notification if completed
        if (newStatus && task.notifyMe && task.notificationScheduledId != 0) {
            NotificationHelper.cancelReminder(context, task.notificationScheduledId)
            com.example.util.WorkManagerHelper.cancelTaskReminder(context, task.id)
        }
    }

    suspend fun deleteTask(task: TaskEntity) = withContext(Dispatchers.IO) {
        if (task.notifyMe && task.notificationScheduledId != 0) {
            NotificationHelper.cancelReminder(context, task.notificationScheduledId)
            com.example.util.WorkManagerHelper.cancelTaskReminder(context, task.id)
        }
        taskDao.deleteTask(task)
    }

    suspend fun updateTask(task: TaskEntity) = withContext(Dispatchers.IO) {
        // Fetch existing entry to compare notifyMe and scheduledTimestamp
        val existing = taskDao.getTaskById(task.id) ?: return@withContext taskDao.updateTask(task)

        val notifyChanged = existing.notifyMe != task.notifyMe
        val timeChanged = existing.scheduledTimestamp != task.scheduledTimestamp

        // If notification was scheduled and now needs to be cancelled or rescheduled
        if (existing.notifyMe && existing.notificationScheduledId != 0) {
            NotificationHelper.cancelReminder(context, existing.notificationScheduledId)
            com.example.util.WorkManagerHelper.cancelTaskReminder(context, existing.id)
        }

        // Update the task
        taskDao.updateTask(task)

        // If notifyMe is true and timestamp is in the future, schedule new reminder
        if (task.notifyMe && task.scheduledTimestamp != null && task.scheduledTimestamp > System.currentTimeMillis()) {
            val notificationId = if (notifyChanged || timeChanged) {
                (System.currentTimeMillis() % 100000).toInt()
            } else {
                existing.notificationScheduledId
            }

            // Update the task with new notificationId if changed
            if (notifyChanged || timeChanged) {
                val updatedWithId = task.copy(notificationScheduledId = notificationId)
                taskDao.updateTask(updatedWithId)
            }

            NotificationHelper.scheduleReminder(
                context = context,
                notificationId = notificationId,
                title = task.title,
                message = if (task.description.isNotBlank()) task.description else "Task due in My Checklist",
                timestampMillis = task.scheduledTimestamp,
                type = "TASK"
            )
            com.example.util.WorkManagerHelper.scheduleTaskReminder(context, task.id, task.scheduledTimestamp)
        }
    }

    // ----------------------------------------------------
    // MUSIC & SCORE CARDS
    // ----------------------------------------------------
    val allMusicTracks: Flow<List<MusicTrackEntity>> = musicDao.getAllTracks()

    suspend fun addMusicTrack(
        songName: String,
        albumName: String,
        category: String,
        artist: String = "",
        uriString: String = "",
        fileName: String = "",
        fileSizeBytes: Long = 0L,
        durationMs: Long = 0L,
        sourceType: String = "LOCAL_STORAGE",
        notes: String = ""
    ): Long = withContext(Dispatchers.IO) {
        val track = MusicTrackEntity(
            songName = songName.trim(),
            albumName = albumName.trim(),
            category = category.trim(),
            artist = artist.trim(),
            uriString = uriString.trim(),
            fileName = fileName.trim(),
            fileSizeBytes = fileSizeBytes,
            durationMs = durationMs,
            sourceType = sourceType,
            notes = notes.trim()
        )
        musicDao.insertTrack(track)
    }

    suspend fun updateMusicTrack(track: MusicTrackEntity) = withContext(Dispatchers.IO) {
        musicDao.updateTrack(track)
    }

    suspend fun deleteMusicTrack(track: MusicTrackEntity) = withContext(Dispatchers.IO) {
        musicDao.deleteTrack(track)
    }

    suspend fun toggleMusicFavorite(trackId: Long, isFavorite: Boolean) = withContext(Dispatchers.IO) {
        musicDao.updateFavorite(trackId, isFavorite)
    }

    // ----------------------------------------------------
    // DOWNLOADED VIDEOS (FACEBOOK & INSTAGRAM)
    // ----------------------------------------------------
    val allDownloadedVideos: Flow<List<DownloadedVideoEntity>> = videoDao.getAllVideos()

    suspend fun addDownloadedVideo(video: DownloadedVideoEntity): Long = withContext(Dispatchers.IO) {
        videoDao.insertVideo(video)
    }

    suspend fun updateDownloadedVideo(video: DownloadedVideoEntity) = withContext(Dispatchers.IO) {
        videoDao.updateVideo(video)
    }

    suspend fun deleteDownloadedVideo(video: DownloadedVideoEntity) = withContext(Dispatchers.IO) {
        videoDao.deleteVideo(video)
    }

    suspend fun deleteDownloadedVideoById(id: Long) = withContext(Dispatchers.IO) {
        videoDao.deleteVideoById(id)
    }

    // ----------------------------------------------------
    // SEED INITIAL DATA IF FRESH INSTALL
    // ----------------------------------------------------
    suspend fun seedInitialDataIfNeeded() = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()

        // Enforce exactly the 4 required categories: Family, Work, Personal, Utility
        val standardThreads = listOf(
            ChatThreadEntity(
                threadKey = "family",
                name = "Family",
                category = "Family",
                iconEmoji = "🏡",
                lastMessagePreview = "I will bring fresh sourdough!",
                lastMessageTimestamp = now - 1000 * 60 * 20
            ),
            ChatThreadEntity(
                threadKey = "work",
                name = "Work",
                category = "Work",
                iconEmoji = "💼",
                lastMessagePreview = "Thanks, reviewing the requirements today.",
                lastMessageTimestamp = now - 1000 * 60 * 60
            ),
            ChatThreadEntity(
                threadKey = "personal",
                name = "Personal",
                category = "Personal",
                iconEmoji = "🌿",
                lastMessagePreview = "Idea: Check weekend mountain hiking trail guidebook.",
                lastMessageTimestamp = now - 1000 * 60 * 120
            ),
            ChatThreadEntity(
                threadKey = "utility",
                name = "Utility",
                category = "Utility",
                iconEmoji = "⚡",
                lastMessagePreview = "Electric bill paid on 15th for $84.20",
                lastMessageTimestamp = now - 1000 * 60 * 150
            )
        )

        // Ensure all 4 standard threads are present and clean up any old legacy threads
        for (thread in standardThreads) {
            val existing = chatDao.getThreadByKey(thread.threadKey)
            if (existing == null) {
                chatDao.insertThread(thread)
            }
        }
        // Remove legacy 'general' thread if present
        chatDao.deleteThread("general")
        chatDao.clearMessagesForThread("general")

        // Seed messages removed per user request


        // Seed Category Mobile Contacts
        if (categoryContactDao.getContactCount() == 0) {
            val initialContacts = listOf(
                // Family
                CategoryContactEntity(category = "Family", name = "Mom", phoneNumber = "+1 (555) 234-5678", note = "Home & Emergency", addedTimestamp = now - 1000 * 60 * 60 * 24 * 3),
                CategoryContactEntity(category = "Family", name = "Dad", phoneNumber = "+1 (555) 345-6789", note = "Mobile", addedTimestamp = now - 1000 * 60 * 60 * 24 * 3),
                CategoryContactEntity(category = "Family", name = "Sarah (Sister)", phoneNumber = "+1 (555) 456-7890", note = "Personal", addedTimestamp = now - 1000 * 60 * 60 * 24 * 2),

                // Work
                CategoryContactEntity(category = "Work", name = "Jordan (PM)", phoneNumber = "+1 (555) 678-9012", note = "Project Manager", addedTimestamp = now - 1000 * 60 * 60 * 24 * 4),
                CategoryContactEntity(category = "Work", name = "Office Desk / Reception", phoneNumber = "+1 (555) 789-0123", note = "Main Line", addedTimestamp = now - 1000 * 60 * 60 * 24 * 4),

                // Personal
                CategoryContactEntity(category = "Personal", name = "Dr. Bradley Clinic", phoneNumber = "+1 (555) 890-1234", note = "Primary Care Physician", addedTimestamp = now - 1000 * 60 * 60 * 24 * 5),
                CategoryContactEntity(category = "Personal", name = "Dave's Auto Garage", phoneNumber = "+1 (555) 901-2345", note = "Mechanic & Service", addedTimestamp = now - 1000 * 60 * 60 * 24 * 5),

                // Utility
                CategoryContactEntity(category = "Utility", name = "Power & Electric Grid", phoneNumber = "+1 (800) 555-0199", note = "24/7 Outage & Repair", addedTimestamp = now - 1000 * 60 * 60 * 24 * 6),
                CategoryContactEntity(category = "Utility", name = "City Water & Sewer", phoneNumber = "+1 (800) 555-0122", note = "Municipal Helpdesk", addedTimestamp = now - 1000 * 60 * 60 * 24 * 6),
                CategoryContactEntity(category = "Utility", name = "Fiber ISP Internet", phoneNumber = "+1 (800) 555-0177", note = "Tech Support & Billing", addedTimestamp = now - 1000 * 60 * 60 * 24 * 6)
            )
            categoryContactDao.insertContacts(initialContacts)
        }

        if (diaryDao.getEntryCount() == 0) {
            // Empty initially per user instruction
        }

        if (eventDao.getEventCount() == 0) {
            // Empty initially per user instruction
        }

        if (vaultDao.getDocumentCount() == 0) {
            val now = System.currentTimeMillis()
            val docs = listOf(
                VaultDocumentEntity(
                    title = "Passport & International ID Scan",
                    originalFileName = "passport_scan_2026.pdf",
                    uriString = "ledger://sample/passport_scan_2026.pdf",
                    fileType = "PDF",
                    category = "ID",
                    dateAddedTimestamp = now - 1000 * 60 * 60 * 24 * 5,
                    fileSizeBytes = 2450000L,
                    notes = "High-resolution color scan of photo identification and visa pages."
                ),
                VaultDocumentEntity(
                    title = "Residential Lease Agreement 2026",
                    originalFileName = "lease_agreement_signed.pdf",
                    uriString = "ledger://sample/lease_agreement_signed.pdf",
                    fileType = "PDF",
                    category = "Legal",
                    dateAddedTimestamp = now - 1000 * 60 * 60 * 24 * 12,
                    fileSizeBytes = 4120000L,
                    notes = "Counter-signed lease contract and building tenancy rules."
                ),
                VaultDocumentEntity(
                    title = "Vehicle Comprehensive Insurance Policy",
                    originalFileName = "auto_policy_card_2026.pdf",
                    uriString = "ledger://sample/auto_policy_card_2026.pdf",
                    fileType = "PDF",
                    category = "Insurance",
                    dateAddedTimestamp = now - 1000 * 60 * 60 * 24 * 18,
                    fileSizeBytes = 1890000L,
                    notes = "Roadside assistance contact and proof of insurance."
                ),
                VaultDocumentEntity(
                    title = "Medical Vaccination & Health Card",
                    originalFileName = "health_card_front_back.png",
                    uriString = "ledger://sample/health_card_front_back.png",
                    fileType = "IMAGE",
                    category = "Health",
                    dateAddedTimestamp = now - 1000 * 60 * 60 * 24 * 25,
                    fileSizeBytes = 3200000L,
                    notes = "Primary physician info & emergency blood type record."
                )
            )
            vaultDao.insertDocuments(docs)
        }

        if (taskDao.getTaskCount() == 0) {
            // Empty initially per user instruction
        }

        if (musicDao.getTrackCount() == 0) {
            val sampleTracks = listOf(
                MusicTrackEntity(
                    songName = "Classic Marimba (English)",
                    albumName = "Standard Ringtones",
                    category = "Ringtone",
                    artist = "System",
                    uriString = "",
                    fileName = "marimba_classic.mp3",
                    fileSizeBytes = 300000L,
                    durationMs = 15000L,
                    isFavorite = true,
                    sourceType = "LOCAL_STORAGE",
                    notes = "Classic phone marimba ringtone."
                ),
                MusicTrackEntity(
                    songName = "Bollywood Romance Flute (Hindi)",
                    albumName = "Desi Melodies",
                    category = "Ringtone",
                    artist = "Rhythm & Flute",
                    uriString = "",
                    fileName = "bollywood_flute.mp3",
                    fileSizeBytes = 450000L,
                    durationMs = 25000L,
                    isFavorite = false,
                    sourceType = "LOCAL_STORAGE",
                    notes = "Soothing Hindi flute melody ringtone."
                ),
                MusicTrackEntity(
                    songName = "Digital Bell (English)",
                    albumName = "Standard Ringtones",
                    category = "Ringtone",
                    artist = "System",
                    uriString = "",
                    fileName = "digital_bell.mp3",
                    fileSizeBytes = 250000L,
                    durationMs = 12000L,
                    isFavorite = false,
                    sourceType = "LOCAL_STORAGE",
                    notes = "Modern digital alert ringtone."
                ),
                MusicTrackEntity(
                    songName = "Desi Beats (Hindi)",
                    albumName = "Desi Melodies",
                    category = "Ringtone",
                    artist = "Dholak Masters",
                    uriString = "",
                    fileName = "desi_beats.mp3",
                    fileSizeBytes = 500000L,
                    durationMs = 20000L,
                    isFavorite = true,
                    sourceType = "LOCAL_STORAGE",
                    notes = "Upbeat Hindi dholak beat ringtone."
                )
            )
            musicDao.insertTracks(sampleTracks)
        }

        // Run auto-sync and archive for any chats older than 5 days
        runAutoSyncAndArchive()
    }
}
