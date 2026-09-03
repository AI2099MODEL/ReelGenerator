package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.data.LedgerRepository
import com.example.data.model.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

data class BackupRestoreResult(
    val success: Boolean,
    val message: String,
    val threadsCount: Int = 0,
    val messagesCount: Int = 0,
    val contactsCount: Int = 0,
    val diaryCount: Int = 0,
    val eventsCount: Int = 0,
    val vaultCount: Int = 0,
    val tasksCount: Int = 0
)

object BackupManager {

    suspend fun createBackupJson(repository: LedgerRepository): String {
        val root = JSONObject()
        val meta = JSONObject()
        val now = System.currentTimeMillis()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)

        meta.put("appName", "MyLyfe Organizer")
        meta.put("backupType", "Google Drive Cloud Archive")
        meta.put("version", 2)
        meta.put("timestamp", now)
        meta.put("formattedDate", dateFormat.format(Date(now)))
        root.put("metadata", meta)

        // Threads
        val threads = repository.getAllThreadsSnapshot()
        val threadsArray = JSONArray()
        for (t in threads) {
            val obj = JSONObject()
            obj.put("threadKey", t.threadKey)
            obj.put("name", t.name)
            obj.put("category", t.category)
            obj.put("iconEmoji", t.iconEmoji)
            obj.put("lastMessagePreview", t.lastMessagePreview)
            obj.put("lastMessageTimestamp", t.lastMessageTimestamp)
            threadsArray.put(obj)
        }
        root.put("chatThreads", threadsArray)

        // Messages
        val messages = repository.getAllMessagesSnapshot()
        val messagesArray = JSONArray()
        for (m in messages) {
            val obj = JSONObject()
            obj.put("threadKey", m.threadKey)
            obj.put("content", m.content)
            obj.put("isSentByUser", m.isSentByUser)
            obj.put("senderName", m.senderName)
            obj.put("timestamp", m.timestamp)
            obj.put("isArchived", m.isArchived)
            if (m.syncedTimestamp != null) obj.put("syncedTimestamp", m.syncedTimestamp)
            if (m.attachmentUri != null) obj.put("attachmentUri", m.attachmentUri)
            if (m.attachmentName != null) obj.put("attachmentName", m.attachmentName)
            if (m.attachmentType != null) obj.put("attachmentType", m.attachmentType)
            obj.put("attachmentSizeBytes", m.attachmentSizeBytes)
            messagesArray.put(obj)
        }
        root.put("chatMessages", messagesArray)

        // Category Contacts (Mobile Numbers)
        val contacts = repository.getAllContactsSnapshot()
        val contactsArray = JSONArray()
        for (c in contacts) {
            val obj = JSONObject()
            obj.put("category", c.category)
            obj.put("name", c.name)
            obj.put("phoneNumber", c.phoneNumber)
            obj.put("note", c.note)
            obj.put("addedTimestamp", c.addedTimestamp)
            contactsArray.put(obj)
        }
        root.put("categoryContacts", contactsArray)

        // Diary
        val diary = repository.getAllDiaryEntriesSnapshot()
        val diaryArray = JSONArray()
        for (d in diary) {
            val obj = JSONObject()
            obj.put("title", d.title)
            obj.put("body", d.body)
            obj.put("dateTimestamp", d.dateTimestamp)
            obj.put("moodOrTag", d.moodOrTag)
            obj.put("isPinned", d.isPinned)
            obj.put("notifyMe", d.notifyMe)
            diaryArray.put(obj)
        }
        root.put("diaryEntries", diaryArray)

        // Events
        val events = repository.getAllEventsSnapshot()
        val eventsArray = JSONArray()
        for (e in events) {
            val obj = JSONObject()
            obj.put("title", e.title)
            obj.put("locationOrNote", e.locationOrNote)
            obj.put("eventTimestamp", e.eventTimestamp)
            obj.put("notifyMe", e.notifyMe)
            obj.put("category", e.category)
            eventsArray.put(obj)
        }
        root.put("events", eventsArray)

        // Vault
        val vault = repository.getAllVaultDocsSnapshot()
        val vaultArray = JSONArray()
        for (v in vault) {
            val obj = JSONObject()
            obj.put("title", v.title)
            obj.put("originalFileName", v.originalFileName)
            obj.put("uriString", v.uriString)
            obj.put("fileType", v.fileType)
            obj.put("category", v.category)
            obj.put("dateAddedTimestamp", v.dateAddedTimestamp)
            obj.put("fileSizeBytes", v.fileSizeBytes)
            obj.put("notes", v.notes)
            vaultArray.put(obj)
        }
        root.put("vaultDocuments", vaultArray)

        // Tasks
        val tasks = repository.getAllTasksSnapshot()
        val tasksArray = JSONArray()
        for (tk in tasks) {
            val obj = JSONObject()
            obj.put("title", tk.title)
            obj.put("description", tk.description)
            if (tk.scheduledTimestamp != null) obj.put("scheduledTimestamp", tk.scheduledTimestamp)
            obj.put("category", tk.category)
            obj.put("isCompleted", tk.isCompleted)
            if (tk.completedTimestamp != null) obj.put("completedTimestamp", tk.completedTimestamp)
            obj.put("notifyMe", tk.notifyMe)
            obj.put("attachmentUris", tk.attachmentUris)
            tasksArray.put(obj)
        }
        root.put("tasks", tasksArray)

        return root.toString(2)
    }

    suspend fun restoreFromJson(jsonString: String, repository: LedgerRepository): BackupRestoreResult {
        return try {
            val root = JSONObject(jsonString)

            var threadsRestored = 0
            if (root.has("chatThreads")) {
                val threadsArray = root.getJSONArray("chatThreads")
                val list = mutableListOf<ChatThreadEntity>()
                for (i in 0 until threadsArray.length()) {
                    val obj = threadsArray.getJSONObject(i)
                    list.add(
                        ChatThreadEntity(
                            threadKey = obj.getString("threadKey"),
                            name = obj.getString("name"),
                            category = obj.optString("category", "General"),
                            iconEmoji = obj.optString("iconEmoji", "💬"),
                            lastMessagePreview = obj.optString("lastMessagePreview", ""),
                            lastMessageTimestamp = obj.optLong("lastMessageTimestamp", System.currentTimeMillis())
                        )
                    )
                }
                repository.insertThreads(list)
                threadsRestored = list.size
            }

            var messagesRestored = 0
            if (root.has("chatMessages")) {
                val messagesArray = root.getJSONArray("chatMessages")
                val list = mutableListOf<ChatMessageEntity>()
                for (i in 0 until messagesArray.length()) {
                    val obj = messagesArray.getJSONObject(i)
                    list.add(
                        ChatMessageEntity(
                            threadKey = obj.getString("threadKey"),
                            content = obj.getString("content"),
                            isSentByUser = obj.getBoolean("isSentByUser"),
                            senderName = obj.optString("senderName", "Sender"),
                            timestamp = obj.optLong("timestamp", System.currentTimeMillis()),
                            isArchived = obj.optBoolean("isArchived", false),
                            syncedTimestamp = if (obj.has("syncedTimestamp")) obj.getLong("syncedTimestamp") else null,
                            attachmentUri = if (obj.has("attachmentUri")) obj.getString("attachmentUri") else null,
                            attachmentName = if (obj.has("attachmentName")) obj.getString("attachmentName") else null,
                            attachmentType = if (obj.has("attachmentType")) obj.getString("attachmentType") else null,
                            attachmentSizeBytes = obj.optLong("attachmentSizeBytes", 0L)
                        )
                    )
                }
                repository.insertMessages(list)
                messagesRestored = list.size
            }

            var contactsRestored = 0
            if (root.has("categoryContacts")) {
                val contactsArray = root.getJSONArray("categoryContacts")
                val list = mutableListOf<CategoryContactEntity>()
                for (i in 0 until contactsArray.length()) {
                    val obj = contactsArray.getJSONObject(i)
                    list.add(
                        CategoryContactEntity(
                            category = obj.getString("category"),
                            name = obj.getString("name"),
                            phoneNumber = obj.getString("phoneNumber"),
                            note = obj.optString("note", ""),
                            addedTimestamp = obj.optLong("addedTimestamp", System.currentTimeMillis())
                        )
                    )
                }
                repository.insertContacts(list)
                contactsRestored = list.size
            }

            var diaryRestored = 0
            if (root.has("diaryEntries")) {
                val diaryArray = root.getJSONArray("diaryEntries")
                val list = mutableListOf<DiaryEntryEntity>()
                for (i in 0 until diaryArray.length()) {
                    val obj = diaryArray.getJSONObject(i)
                    list.add(
                        DiaryEntryEntity(
                            title = obj.getString("title"),
                            body = obj.getString("body"),
                            dateTimestamp = obj.optLong("dateTimestamp", System.currentTimeMillis()),
                            moodOrTag = obj.optString("moodOrTag", "Reflection"),
                            isPinned = obj.optBoolean("isPinned", false),
                            notifyMe = obj.optBoolean("notifyMe", false)
                        )
                    )
                }
                repository.insertDiaryEntries(list)
                diaryRestored = list.size
            }

            var eventsRestored = 0
            if (root.has("events")) {
                val eventsArray = root.getJSONArray("events")
                val list = mutableListOf<EventEntity>()
                for (i in 0 until eventsArray.length()) {
                    val obj = eventsArray.getJSONObject(i)
                    list.add(
                        EventEntity(
                            title = obj.getString("title"),
                            locationOrNote = obj.optString("locationOrNote", ""),
                            eventTimestamp = obj.getLong("eventTimestamp"),
                            notifyMe = obj.optBoolean("notifyMe", false),
                            category = obj.optString("category", "General")
                        )
                    )
                }
                repository.insertEvents(list)
                eventsRestored = list.size
            }

            var vaultRestored = 0
            if (root.has("vaultDocuments")) {
                val vaultArray = root.getJSONArray("vaultDocuments")
                val list = mutableListOf<VaultDocumentEntity>()
                for (i in 0 until vaultArray.length()) {
                    val obj = vaultArray.getJSONObject(i)
                    list.add(
                        VaultDocumentEntity(
                            title = obj.getString("title"),
                            originalFileName = obj.optString("originalFileName", "doc.pdf"),
                            uriString = obj.optString("uriString", ""),
                            fileType = obj.optString("fileType", "PDF"),
                            category = obj.optString("category", "Personal"),
                            dateAddedTimestamp = obj.optLong("dateAddedTimestamp", System.currentTimeMillis()),
                            fileSizeBytes = obj.optLong("fileSizeBytes", 0L),
                            notes = obj.optString("notes", "")
                        )
                    )
                }
                repository.insertVaultDocuments(list)
                vaultRestored = list.size
            }

            var tasksRestored = 0
            if (root.has("tasks")) {
                val tasksArray = root.getJSONArray("tasks")
                val list = mutableListOf<TaskEntity>()
                for (i in 0 until tasksArray.length()) {
                    val obj = tasksArray.getJSONObject(i)
                    list.add(
                        TaskEntity(
                            title = obj.getString("title"),
                            description = obj.optString("description", ""),
                            scheduledTimestamp = if (obj.has("scheduledTimestamp")) obj.getLong("scheduledTimestamp") else null,
                            category = obj.optString("category", "General"),
                            isCompleted = obj.optBoolean("isCompleted", false),
                            completedTimestamp = if (obj.has("completedTimestamp")) obj.getLong("completedTimestamp") else null,
                            notifyMe = obj.optBoolean("notifyMe", false),
                            attachmentUris = obj.optString("attachmentUris", "")
                        )
                    )
                }
                repository.insertTasks(list)
                tasksRestored = list.size
            }

            BackupRestoreResult(
                success = true,
                message = "Archive restored successfully!",
                threadsCount = threadsRestored,
                messagesCount = messagesRestored,
                contactsCount = contactsRestored,
                diaryCount = diaryRestored,
                eventsCount = eventsRestored,
                vaultCount = vaultRestored,
                tasksCount = tasksRestored
            )
        } catch (e: Exception) {
            BackupRestoreResult(
                success = false,
                message = "Failed to parse backup JSON: ${e.localizedMessage ?: e.message}"
            )
        }
    }

    fun shareToGoogleDrive(context: Context, backupJson: String) {
        try {
            val fileName = "mylyfe_google_drive_backup_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())}.json"
            val backupDir = File(context.cacheDir, "backups").apply { mkdirs() }
            val backupFile = File(backupDir, fileName)
            FileOutputStream(backupFile).use { out ->
                out.write(backupJson.toByteArray(Charsets.UTF_8))
            }

            val fileUri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                backupFile
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/json"
                putExtra(Intent.EXTRA_STREAM, fileUri)
                putExtra(Intent.EXTRA_SUBJECT, "MyLyfe Archive Google Drive Backup")
                putExtra(Intent.EXTRA_TEXT, "MyLyfe Personal Organizer Google Drive Backup Archive. Created: ${Date()}")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                // Hint Google Drive package if installed
                `package` = "com.google.android.apps.docs"
            }

            val isDriveAvailable = context.packageManager.resolveActivity(shareIntent, 0) != null
            val finalIntent = if (isDriveAvailable) {
                shareIntent
            } else {
                Intent(Intent.ACTION_SEND).apply {
                    type = "application/json"
                    putExtra(Intent.EXTRA_STREAM, fileUri)
                    putExtra(Intent.EXTRA_SUBJECT, "MyLyfe Archive Google Drive Backup")
                    putExtra(Intent.EXTRA_TEXT, "MyLyfe Personal Organizer Backup Archive JSON")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
            }

            val chooser = Intent.createChooser(finalIntent, "Save or Upload Backup to Google Drive")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            chooser.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            context.startActivity(chooser)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
