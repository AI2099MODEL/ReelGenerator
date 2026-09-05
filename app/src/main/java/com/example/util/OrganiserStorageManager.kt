package com.example.util

import android.content.Context
import android.net.Uri
import android.os.Environment
import android.util.Log
import com.example.data.model.DiaryEntryEntity
import com.example.data.model.EventEntity
import com.example.data.model.TaskEntity
import com.example.data.model.VaultDocumentEntity
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object OrganiserStorageManager {
    private const val TAG = "OrganiserStorage"
    const val ROOT_FOLDER_NAME = "My Organiser"

    // Subdirectories organized by tabs and libraries
    const val SUBDIR_DOCUMENTS = "Documents_and_Libraries"
    const val SUBDIR_DIARY = "Personal_Diary_Entries"
    const val SUBDIR_TASKS = "Tasks_and_Plans"
    const val SUBDIR_EVENTS = "Event_Dates"
    const val SUBDIR_MEDIA = "Media_Files"
    const val SUBDIR_BACKUPS = "Deleted_Archive" // Safety folder: retained on deletion

    /**
     * Ensures all directories under 'My Organiser' exist in device's local storage.
     * Accessible via device's public Documents directory so user file managers and PC connections can view it.
     */
    fun initOrganiserStorage(context: Context): File {
        val rootDir = getOrganiserRootDir(context)
        if (!rootDir.exists()) {
            val created = rootDir.mkdirs()
            Log.d(TAG, "My Organiser directory created: $created at ${rootDir.absolutePath}")
        }

        // Subfolders for tabs and libraries
        val subdirs = listOf(
            SUBDIR_DOCUMENTS,
            SUBDIR_DIARY,
            SUBDIR_TASKS,
            SUBDIR_EVENTS,
            SUBDIR_MEDIA,
            SUBDIR_BACKUPS,
            "$SUBDIR_DOCUMENTS/Personal Library",
            "$SUBDIR_DOCUMENTS/Finance_and_Legal",
            "$SUBDIR_DOCUMENTS/Certificates_and_IDs",
            "$SUBDIR_MEDIA/Collages",
            "$SUBDIR_MEDIA/Attachments"
        )

        for (dir in subdirs) {
            val folder = File(rootDir, dir)
            if (!folder.exists()) {
                folder.mkdirs()
            }
        }

        // Create a README file inside to explain to the user
        val readme = File(rootDir, "README_MY_ORGANISER.txt")
        if (!readme.exists()) {
            readme.writeText(
                "My Organiser Local Directory\n" +
                "============================\n" +
                "This folder stores your personal documents, libraries, diary reflections,\n" +
                "tasks, and media locally on your device.\n\n" +
                "Structure:\n" +
                "- Documents_and_Libraries/ : Vault and reference documents categorized by library\n" +
                "- Personal_Diary_Entries/   : Markdown and text exports of diary entries\n" +
                "- Tasks_and_Plans/          : Actionable tasks and checklists\n" +
                "- Event_Dates/              : Important anniversaries and events\n" +
                "- Media_Files/              : Photo collages, images, and attachments\n" +
                "- Deleted_Archive/          : Permanent backup snapshot of deleted items so no data is ever lost!\n"
            )
        }

        return rootDir
    }

    fun getOrganiserRootDir(context: Context): File {
        val publicDocs = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
        val dir = File(publicDocs, ROOT_FOLDER_NAME)
        if (dir.exists() || dir.mkdirs()) {
            return dir
        }
        // Fallback to app's external files dir if public Documents is restricted
        val ext = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS) ?: context.filesDir
        val fallbackDir = File(ext, ROOT_FOLDER_NAME)
        if (!fallbackDir.exists()) fallbackDir.mkdirs()
        return fallbackDir
    }

    fun getSubDir(context: Context, subDirName: String): File {
        val root = getOrganiserRootDir(context)
        val sub = File(root, subDirName)
        if (!sub.exists()) sub.mkdirs()
        return sub
    }

    /**
     * Persists a Vault document to local My Organiser folder
     */
    fun persistVaultDocument(context: Context, doc: VaultDocumentEntity) {
        try {
            val safeCategory = doc.category.replace(Regex("[^a-zA-Z0-9_-]"), "_")
            val libFolder = File(getSubDir(context, SUBDIR_DOCUMENTS), safeCategory)
            if (!libFolder.exists()) libFolder.mkdirs()

            val safeTitle = doc.title.replace(Regex("[^a-zA-Z0-9_-]"), "_")
            val targetFile = File(libFolder, "${safeTitle}_${doc.id}.txt")

            val content = buildString {
                appendLine("Document Title: ${doc.title}")
                appendLine("Library / Category: ${doc.category}")
                appendLine("File Type: ${doc.fileType}")
                appendLine("Original File Name: ${doc.originalFileName}")
                appendLine("File Size: ${doc.fileSizeBytes} bytes")
                appendLine("Added: ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(doc.dateAddedTimestamp))}")
                if (doc.notes.isNotBlank()) {
                    appendLine("Notes: ${doc.notes}")
                }
                appendLine("Local Uri: ${doc.uriString}")
            }
            targetFile.writeText(content)
        } catch (e: Exception) {
            Log.e(TAG, "Error persisting vault document", e)
        }
    }

    /**
     * Preserves a document into Deleted_Archive when deleted from the app
     */
    fun archiveDeletedVaultDocument(context: Context, doc: VaultDocumentEntity) {
        try {
            val archiveFolder = getSubDir(context, "$SUBDIR_BACKUPS/Documents")
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val safeTitle = doc.title.replace(Regex("[^a-zA-Z0-9_-]"), "_")
            val targetFile = File(archiveFolder, "DELETED_${safeTitle}_$timestamp.txt")

            val content = buildString {
                appendLine("=== DELETED VAULT DOCUMENT (SAFETY ARCHIVE) ===")
                appendLine("Deleted At: $timestamp")
                appendLine("Original ID: ${doc.id}")
                appendLine("Title: ${doc.title}")
                appendLine("Library: ${doc.category}")
                appendLine("File Type: ${doc.fileType}")
                appendLine("Original File Name: ${doc.originalFileName}")
                appendLine("Original Uri: ${doc.uriString}")
                appendLine("Notes: ${doc.notes}")
            }
            targetFile.writeText(content)
        } catch (e: Exception) {
            Log.e(TAG, "Error archiving deleted document", e)
        }
    }

    /**
     * Persists a Diary entry to local My Organiser folder
     */
    fun persistDiaryEntry(context: Context, entry: DiaryEntryEntity) {
        try {
            val diaryFolder = getSubDir(context, SUBDIR_DIARY)
            val safeTitle = (if (entry.title.isNotBlank()) entry.title else "Diary_${entry.id}")
                .replace(Regex("[^a-zA-Z0-9_-]"), "_")
            val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(entry.dateTimestamp))
            val targetFile = File(diaryFolder, "${dateStr}_$safeTitle.txt")

            val content = buildString {
                appendLine("# ${entry.title}")
                appendLine("Date: ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(entry.dateTimestamp))}")
                appendLine("Mood / Tag: ${entry.moodOrTag}")
                if (entry.imageUri != null) {
                    appendLine("Image Attachment: ${entry.imageUri}")
                }
                appendLine()
                appendLine(entry.body)
            }
            targetFile.writeText(content)
        } catch (e: Exception) {
            Log.e(TAG, "Error persisting diary entry", e)
        }
    }

    /**
     * Preserves a diary entry into Deleted_Archive when deleted from the app
     */
    fun archiveDeletedDiaryEntry(context: Context, entry: DiaryEntryEntity) {
        try {
            val archiveFolder = getSubDir(context, "$SUBDIR_BACKUPS/Diary")
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val safeTitle = (if (entry.title.isNotBlank()) entry.title else "Entry_${entry.id}")
                .replace(Regex("[^a-zA-Z0-9_-]"), "_")
            val targetFile = File(archiveFolder, "DELETED_${safeTitle}_$timestamp.txt")

            val content = buildString {
                appendLine("=== DELETED DIARY ENTRY (SAFETY ARCHIVE) ===")
                appendLine("Deleted At: $timestamp")
                appendLine("Title: ${entry.title}")
                appendLine("Created: ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(entry.dateTimestamp))}")
                appendLine("Tag: ${entry.moodOrTag}")
                appendLine("Image: ${entry.imageUri ?: "None"}")
                appendLine()
                appendLine(entry.body)
            }
            targetFile.writeText(content)
        } catch (e: Exception) {
            Log.e(TAG, "Error archiving deleted diary entry", e)
        }
    }

    /**
     * Persists Task
     */
    fun persistTask(context: Context, task: TaskEntity) {
        try {
            val tasksFolder = getSubDir(context, SUBDIR_TASKS)
            val safeTitle = task.title.replace(Regex("[^a-zA-Z0-9_-]"), "_")
            val file = File(tasksFolder, "Task_${task.id}_$safeTitle.txt")
            val content = buildString {
                appendLine("Task: ${task.title}")
                appendLine("Status: ${if (task.isCompleted) "Completed" else "Pending"}")
                appendLine("Category: ${task.category}")
                if (task.scheduledTimestamp != null) {
                    appendLine("Due Date: ${SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(task.scheduledTimestamp))}")
                }
                appendLine("Description: ${task.description}")
            }
            file.writeText(content)
        } catch (e: Exception) {
            Log.e(TAG, "Error persisting task", e)
        }
    }

    fun archiveDeletedTask(context: Context, task: TaskEntity) {
        try {
            val archiveFolder = getSubDir(context, "$SUBDIR_BACKUPS/Tasks")
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val safeTitle = task.title.replace(Regex("[^a-zA-Z0-9_-]"), "_")
            val targetFile = File(archiveFolder, "DELETED_TASK_${safeTitle}_$timestamp.txt")
            targetFile.writeText("DELETED TASK: ${task.title}\nCategory: ${task.category}\nDetails: ${task.description}\nDeleted At: $timestamp\n")
        } catch (e: Exception) {
            Log.e(TAG, "Error archiving deleted task", e)
        }
    }

    /**
     * Persists Event
     */
    fun persistEvent(context: Context, event: EventEntity) {
        try {
            val eventsFolder = getSubDir(context, SUBDIR_EVENTS)
            val safeTitle = event.title.replace(Regex("[^a-zA-Z0-9_-]"), "_")
            val file = File(eventsFolder, "Event_${event.id}_$safeTitle.txt")
            val content = buildString {
                appendLine("Event: ${event.title}")
                appendLine("Date: ${SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(event.eventTimestamp))}")
                appendLine("Location / Note: ${event.locationOrNote}")
                appendLine("Category: ${event.category}")
            }
            file.writeText(content)
        } catch (e: Exception) {
            Log.e(TAG, "Error persisting event", e)
        }
    }

    fun archiveDeletedEvent(context: Context, event: EventEntity) {
        try {
            val archiveFolder = getSubDir(context, "$SUBDIR_BACKUPS/Events")
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val safeTitle = event.title.replace(Regex("[^a-zA-Z0-9_-]"), "_")
            val targetFile = File(archiveFolder, "DELETED_EVENT_${safeTitle}_$timestamp.txt")
            targetFile.writeText("DELETED EVENT: ${event.title}\nDate: ${event.eventTimestamp}\nNote: ${event.locationOrNote}\nDeleted At: $timestamp\n")
        } catch (e: Exception) {
            Log.e(TAG, "Error archiving deleted event", e)
        }
    }

    /**
     * Copies a media file (image/video/pdf) into My Organiser/Media_Files
     */
    fun persistMediaFile(context: Context, uri: Uri, targetSubdir: String = SUBDIR_MEDIA, fileName: String? = null): File? {
        return try {
            val destDir = getSubDir(context, targetSubdir)
            val name = fileName ?: "media_${System.currentTimeMillis()}.jpg"
            val destFile = File(destDir, name)

            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(destFile).use { output ->
                    input.copyTo(output)
                }
            }
            destFile
        } catch (e: Exception) {
            Log.e(TAG, "Error saving media file to organiser", e)
            null
        }
    }

    /**
     * Archive media file on deletion
     */
    fun archiveDeletedMedia(context: Context, file: File) {
        try {
            if (file.exists()) {
                val archiveDir = getSubDir(context, "$SUBDIR_BACKUPS/Media")
                val target = File(archiveDir, "DELETED_${file.name}")
                file.copyTo(target, overwrite = true)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error archiving deleted media", e)
        }
    }
}
