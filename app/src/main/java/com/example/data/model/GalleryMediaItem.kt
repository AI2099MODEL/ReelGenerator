package com.example.data.model

import android.net.Uri
import java.io.File

enum class GallerySourceType {
    AI_GENERATED,
    LOCAL_STORAGE,
    GOOGLE_DRIVE
}

data class GalleryMediaItem(
    val id: String,
    val title: String,
    val source: GallerySourceType,
    val localUriString: String? = null,
    val localFilePath: String? = null,
    val googleDriveFileId: String? = null,
    val prompt: String? = null,
    val style: String? = null,
    val aspectRatio: String = "1:1",
    val dateAddedMs: Long = System.currentTimeMillis(),
    val sizeBytes: Long = 0L,
    val isSyncedToGoogleDrive: Boolean = false,
    val isFavorite: Boolean = false
) {
    val displaySize: String
        get() {
            if (sizeBytes <= 0) return "1.2 MB"
            val kb = sizeBytes / 1024.0
            val mb = kb / 1024.0
            return if (mb >= 1.0) "%.1f MB".format(mb) else "%.0f KB".format(kb)
        }
}
