package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.LedgerRepository
import com.example.data.model.*
import com.example.util.NetworkMonitor
import com.example.util.NetworkSimulationMode
import com.example.util.NetworkState
import com.example.util.OrganiserStorageManager
import java.util.Calendar
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class LedgerSection(val title: String, val tabLabel: String, val iconEmoji: String) {
    IMAGES("Text to Image", "Text to Image", "✨"),
    IMPORTANT_DATES("Important Dates", "Important Dates", "🎂"),
    REMIND_ME("Remind Me", "Remind Me", "⏰"),
    DAILY_SCHEDULE("Daily Schedule", "Daily Schedule", "📅"),
    VAULT("Vault", "Vault", "🔒")
}

data class GlobalSettingsState(
    val isLocationEnabled: Boolean = true,
    val selectedLocation: String = "San Francisco, CA (Google Location API)",
    val locationAccuracy: String = "High Precision (GPS + Wi-Fi)",
    val latitude: Double = 37.7749,
    val longitude: Double = -122.4194,
    val isAutoLocation: Boolean = true,

    val isTranslationEnabled: Boolean = true,
    val targetLanguage: String = "English (US)",
    val targetLanguageCode: String = "en",
    val translationEngine: String = "Google ML Kit (On-Device)",
    val autoTranslateContent: Boolean = true
)

@OptIn(ExperimentalCoroutinesApi::class)
class LedgerViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: LedgerRepository
    private val networkMonitor: NetworkMonitor = NetworkMonitor(application, viewModelScope)

    val networkState: StateFlow<NetworkState> = networkMonitor.networkState

    val selectedSection = MutableStateFlow(LedgerSection.DAILY_SCHEDULE)
    val activeChatThreadKey = MutableStateFlow("family")
    val globalSettings = MutableStateFlow(GlobalSettingsState())
    val selectedSocialChannels = androidx.compose.runtime.mutableStateListOf<String>()
    val connectedSocialChannels = androidx.compose.runtime.mutableStateListOf<String>()

    fun toggleSocialChannelConnection(channelId: String) {
        if (connectedSocialChannels.contains(channelId)) {
            connectedSocialChannels.remove(channelId)
            selectedSocialChannels.remove(channelId)
        } else {
            connectedSocialChannels.add(channelId)
        }
    }

    fun toggleSocialChannelSelection(channelId: String) {
        if (selectedSocialChannels.contains(channelId)) {
            selectedSocialChannels.remove(channelId)
        } else if (connectedSocialChannels.contains(channelId)) {
            selectedSocialChannels.add(channelId)
        }
    }


    val chatThreads: StateFlow<List<ChatThreadEntity>>
    val activeThreadMessages: StateFlow<List<ChatMessageEntity>>
    val activeArchivedMessages: StateFlow<List<ChatMessageEntity>>
    val allArchivedMessages: StateFlow<List<ChatMessageEntity>>
    val archivedCount: StateFlow<Int>
    val pendingSyncCount: StateFlow<Int>
    val pendingSyncMessages: StateFlow<List<ChatMessageEntity>>
    val activeCategoryContacts: StateFlow<List<CategoryContactEntity>>
    val allCategoryContacts: StateFlow<List<CategoryContactEntity>>
    val diaryEntries: StateFlow<List<DiaryEntryEntity>>
    val dailySchedules: StateFlow<List<DailyScheduleEntity>>
    val events: StateFlow<List<EventEntity>>
    val vaultDocuments: StateFlow<List<VaultDocumentEntity>>
    val tasks: StateFlow<List<TaskEntity>>
    val musicTracks: StateFlow<List<MusicTrackEntity>>
    val downloadedVideos: StateFlow<List<DownloadedVideoEntity>>

    // Biometric & Security State for Entire App
    private val securityPrefs = application.getSharedPreferences("app_security_prefs", android.content.Context.MODE_PRIVATE)
    val isAppLocked = MutableStateFlow(false)
    val isBiometricEnabled = MutableStateFlow(false)
    val appPinCode = MutableStateFlow(securityPrefs.getString("user_app_pin", "1234") ?: "1234")

    fun unlockApp() {
        isAppLocked.value = false
    }

    fun lockApp() {
        if (isBiometricEnabled.value) {
            isAppLocked.value = true
        }
    }

    fun toggleBiometricSecurity(enabled: Boolean) {
        isBiometricEnabled.value = enabled
        if (!enabled) {
            isAppLocked.value = false
        }
    }

    fun setAppPin(pin: String) {
        if (pin.length in 4..6) {
            securityPrefs.edit().putString("user_app_pin", pin).apply()
            appPinCode.value = pin
        }
    }

    init {
        val db = AppDatabase.getDatabase(application)
        repository = LedgerRepository(db, application)

        // Seed default initial data & run auto-sync for chats > 5 days
        // Initialize My Organiser directory in local storage upon app startup
        viewModelScope.launch {
            OrganiserStorageManager.initOrganiserStorage(application)
            repository.seedInitialDataIfNeeded()
            repository.runAutoSyncAndArchive()
        }

        // Auto-sync queued messages when mobile signal or online connectivity is detected
        viewModelScope.launch {
            networkState.collect { state ->
                if (state.hasMobileSignal || state.isOnline) {
                    repository.syncQueuedMessages()
                }
            }
        }

        chatThreads = repository.allChatThreads
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        pendingSyncCount = repository.pendingSyncCount
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

        pendingSyncMessages = repository.pendingSyncMessages
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        activeThreadMessages = activeChatThreadKey
            .flatMapLatest { key ->
                if (key.isBlank()) flowOf(emptyList())
                else repository.getMessagesForThread(key)
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        activeArchivedMessages = activeChatThreadKey
            .flatMapLatest { key ->
                if (key.isBlank()) flowOf(emptyList())
                else repository.getArchivedMessagesForThread(key)
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        allArchivedMessages = repository.allArchivedMessages
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        archivedCount = repository.archivedMessageCount
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

        activeCategoryContacts = activeChatThreadKey
            .flatMapLatest { key ->
                val category = when (key.lowercase()) {
                    "family" -> "Family"
                    "work" -> "Work"
                    "personal" -> "Personal"
                    "utility" -> "Utility"
                    else -> key.replaceFirstChar { if (it.isLowerCase()) it.titlecase(java.util.Locale.ROOT) else it.toString() }
                }
                repository.getContactsForCategory(category)
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        allCategoryContacts = repository.allCategoryContacts
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        diaryEntries = repository.allDiaryEntries
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        dailySchedules = repository.allDailySchedules
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        viewModelScope.launch {
            repository.seedDefaultSampleSchedulesIfEmpty()
        }

        events = repository.allEvents
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        vaultDocuments = repository.allVaultDocuments
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        tasks = repository.allTasks
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        musicTracks = repository.allMusicTracks
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        downloadedVideos = repository.allDownloadedVideos
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    }

    fun addDownloadedVideo(video: DownloadedVideoEntity) {
        viewModelScope.launch {
            repository.addDownloadedVideo(video)
        }
    }

    fun deleteDownloadedVideo(video: DownloadedVideoEntity) {
        viewModelScope.launch {
            repository.deleteDownloadedVideo(video)
        }
    }

    fun updateDownloadedVideo(video: DownloadedVideoEntity) {
        viewModelScope.launch {
            repository.updateDownloadedVideo(video)
        }
    }

    fun setSection(section: LedgerSection) {
        selectedSection.value = section
    }

    fun setActiveChatThread(threadKey: String) {
        activeChatThreadKey.value = threadKey
    }

    // ---------------- CHAT & OFFLINE QUEUE ----------------
    fun sendChatMessage(
        threadKey: String,
        text: String,
        attachmentUri: String? = null,
        attachmentName: String? = null,
        attachmentType: String? = null,
        attachmentSizeBytes: Long = 0L
    ) {
        if (text.isBlank() && attachmentUri == null && attachmentName == null) return
        val currentNet = networkState.value
        val shouldQueue = !currentNet.isOnline || (!currentNet.hasMobileSignal && currentNet.connectionType == "OFFLINE")

        viewModelScope.launch {
            repository.sendMessage(
                threadKey = threadKey,
                content = text.trim(),
                isSentByUser = true,
                senderName = "You",
                isPendingSync = shouldQueue,
                attachmentUri = attachmentUri,
                attachmentName = attachmentName,
                attachmentType = attachmentType,
                attachmentSizeBytes = attachmentSizeBytes
            )
        }
    }

    fun syncQueuedMessages(onComplete: ((Int) -> Unit)? = null) {
        viewModelScope.launch {
            val count = repository.syncQueuedMessages()
            onComplete?.invoke(count)
        }
    }

    fun setNetworkSimulationMode(mode: NetworkSimulationMode) {
        networkMonitor.setSimulationMode(mode)
    }

    fun triggerAutoSync(onComplete: ((Int) -> Unit)? = null) {
        viewModelScope.launch {
            val archived = repository.runAutoSyncAndArchive()
            onComplete?.invoke(archived)
        }
    }

    /**
     * Helper function to identify chat messages older than [days] (default 5 days)
     * and move them into the hidden 'Archive' section within the Vault.
     */
    fun identifyAndArchiveOldChatMessages(days: Int = 5, onComplete: ((ArchiveMessagesReport) -> Unit)? = null) {
        viewModelScope.launch {
            val report = repository.identifyAndArchiveOldChatMessages(days)
            onComplete?.invoke(report)
        }
    }

    fun restoreArchivedMessage(messageId: Long) {
        viewModelScope.launch {
            repository.restoreArchivedMessage(messageId)
        }
    }

    fun deleteMessage(message: ChatMessageEntity) {
        viewModelScope.launch {
            repository.deleteMessage(message)
        }
    }

    fun editMessage(messageId: Long, newContent: String) {
        viewModelScope.launch {
            repository.editMessage(messageId, newContent)
        }
    }

    fun updateThreadDetails(threadKey: String, newName: String, newEmoji: String) {
        viewModelScope.launch {
            repository.updateThreadDetails(threadKey, newName, newEmoji)
        }
    }

    fun renameCategoryAcrossApp(oldCategory: String, newCategory: String) {
        viewModelScope.launch {
            repository.renameCategoryAcrossApp(oldCategory, newCategory)
        }
    }

    fun createChatThread(name: String, category: String, iconEmoji: String) {
        viewModelScope.launch {
            val newKey = repository.createThread(name, category, iconEmoji)
            activeChatThreadKey.value = newKey
        }
    }

    fun deleteChatThread(threadKey: String) {
        viewModelScope.launch {
            repository.deleteThread(threadKey)
            if (activeChatThreadKey.value == threadKey) {
                val remaining = chatThreads.value.filter { it.threadKey != threadKey }
                if (remaining.isNotEmpty()) {
                    activeChatThreadKey.value = remaining.first().threadKey
                }
            }
        }
    }

    // ---------------- DIARY ----------------
    fun addDiaryEntry(title: String, body: String, moodOrTag: String, isPinned: Boolean, notifyMe: Boolean = false, timestamp: Long = System.currentTimeMillis(), imageUri: String? = null) {
        if (title.isBlank() && body.isBlank()) return
        viewModelScope.launch {
            repository.addDiaryEntry(title, body, moodOrTag, isPinned, notifyMe, timestamp, imageUri)
            val entry = DiaryEntryEntity(title = title, body = body, moodOrTag = moodOrTag, isPinned = isPinned, notifyMe = notifyMe, dateTimestamp = timestamp, imageUri = imageUri)
            OrganiserStorageManager.persistDiaryEntry(getApplication(), entry)
        }
    }

    fun updateDiaryEntry(entry: DiaryEntryEntity) {
        viewModelScope.launch {
            repository.updateDiaryEntry(entry)
        }
    }

    fun deleteDiaryEntry(entry: DiaryEntryEntity) {
        viewModelScope.launch {
            // Retain copy in My Organiser / Deleted_Archive before removing from UI
            OrganiserStorageManager.archiveDeletedDiaryEntry(getApplication(), entry)
            repository.deleteDiaryEntry(entry)
        }
    }

    // ---------------- EVENTS / DATES & REMINDERS ----------------
    fun addEvent(
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
    ) {
        if (title.isBlank()) return
        viewModelScope.launch {
            repository.addEvent(title, locationOrNote, eventTimestamp, notifyMe, category, includeYear, isAllDay, imageUri, eventType, isCompleted)
            val event = EventEntity(
                title = title,
                locationOrNote = locationOrNote,
                eventTimestamp = eventTimestamp,
                notifyMe = notifyMe,
                category = category,
                includeYear = includeYear,
                isAllDay = isAllDay,
                imageUri = imageUri,
                eventType = eventType,
                isCompleted = isCompleted
            )
            OrganiserStorageManager.persistEvent(getApplication(), event)
        }
    }

    fun toggleEventCompleted(event: EventEntity) {
        viewModelScope.launch {
            repository.toggleEventCompleted(event)
        }
    }

    fun updateEvent(event: EventEntity) {
        viewModelScope.launch {
            repository.updateEvent(event)
        }
    }

    fun deleteEvent(event: EventEntity) {
        viewModelScope.launch {
            OrganiserStorageManager.archiveDeletedEvent(getApplication(), event)
            repository.deleteEvent(event)
        }
    }

    fun toggleEventNotification(event: EventEntity) {
        viewModelScope.launch {
            repository.toggleEventNotification(event)
        }
    }

    // ---------------- VAULT ----------------
    fun addVaultDocument(
        title: String,
        originalFileName: String,
        uriString: String,
        fileType: String,
        category: String,
        fileSizeBytes: Long = 0L,
        notes: String = ""
    ) {
        if (title.isBlank()) return
        viewModelScope.launch {
            repository.addVaultDocument(title, originalFileName, uriString, fileType, category, fileSizeBytes, notes)
            val doc = VaultDocumentEntity(title = title, originalFileName = originalFileName, uriString = uriString, fileType = fileType, category = category, fileSizeBytes = fileSizeBytes, notes = notes)
            OrganiserStorageManager.persistVaultDocument(getApplication(), doc)
        }
    }

    fun deleteVaultDocument(document: VaultDocumentEntity) {
        viewModelScope.launch {
            // Keep in My Organiser / Deleted_Archive so data is preserved locally even if removed from UI
            OrganiserStorageManager.archiveDeletedVaultDocument(getApplication(), document)
            repository.deleteVaultDocument(document)
        }
    }

    fun loadSampleRoutine() {
        viewModelScope.launch {
            repository.loadSampleRoutine()
        }
    }

    // ---------------- DAILY SCHEDULE & DIARY ----------------
    fun addDailySchedule(
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
    ) {
        viewModelScope.launch {
            repository.addDailySchedule(
                title = title,
                note = note,
                timestamp = timestamp,
                endTimestamp = endTimestamp,
                isMultiDay = isMultiDay,
                recurrence = recurrence,
                timeSlot = timeSlot,
                category = category,
                notifyMe = notifyMe,
                colorHex = colorHex
            )
        }
    }

    fun getSchedulesForDate(calendar: Calendar = Calendar.getInstance()): Flow<List<DailyScheduleEntity>> {
        val cal = calendar.clone() as Calendar
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val startOfDay = cal.timeInMillis

        cal.set(Calendar.HOUR_OF_DAY, 23)
        cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59)
        cal.set(Calendar.MILLISECOND, 999)
        val endOfDay = cal.timeInMillis

        return repository.getSchedulesForDateRange(startOfDay, endOfDay)
    }

    fun addDailySchedule(schedule: DailyScheduleEntity) {
        viewModelScope.launch {
            repository.addDailySchedule(
                title = schedule.title,
                note = schedule.note,
                timestamp = schedule.timestamp,
                endTimestamp = schedule.endTimestamp,
                isMultiDay = schedule.isMultiDay,
                recurrence = schedule.recurrence,
                timeSlot = schedule.timeSlot,
                category = schedule.category,
                notifyMe = schedule.notifyMe,
                colorHex = schedule.colorHex
            )
        }
    }

    fun toggleDailyScheduleComplete(schedule: DailyScheduleEntity) {
        viewModelScope.launch {
            repository.toggleDailyScheduleComplete(schedule)
        }
    }

    fun updateDailySchedule(schedule: DailyScheduleEntity) {
        viewModelScope.launch {
            repository.updateDailySchedule(schedule)
        }
    }

    fun deleteDailySchedule(schedule: DailyScheduleEntity) {
        viewModelScope.launch {
            repository.deleteDailySchedule(schedule)
        }
    }

    // ---------------- CATEGORY CONTACTS / MOBILE NUMBERS ----------------
    fun addCategoryContact(category: String, name: String, phoneNumber: String, note: String = "", onResult: ((Boolean) -> Unit)? = null) {
        if (name.isBlank() || phoneNumber.isBlank()) return
        viewModelScope.launch {
            val success = repository.addCategoryContact(category, name, phoneNumber, note)
            onResult?.invoke(success)
        }
    }

    fun deleteCategoryContact(id: Long) {
        viewModelScope.launch {
            repository.deleteCategoryContact(id)
        }
    }

    fun deleteCategoryContact(contact: CategoryContactEntity) {
        viewModelScope.launch {
            repository.deleteCategoryContact(contact)
        }
    }

    // ---------------- GOOGLE DRIVE BACKUP & RESTORE ----------------
    suspend fun exportBackupJson(): String {
        return com.example.util.BackupManager.createBackupJson(repository)
    }

    fun restoreBackupJson(jsonString: String, onResult: (com.example.util.BackupRestoreResult) -> Unit) {
        viewModelScope.launch {
            val result = com.example.util.BackupManager.restoreFromJson(jsonString, repository)
            onResult(result)
        }
    }

    fun shareBackupToGoogleDrive(context: android.content.Context) {
        viewModelScope.launch {
            val json = com.example.util.BackupManager.createBackupJson(repository)
            com.example.util.BackupManager.shareToGoogleDrive(context, json)
        }
    }

    // ---------------- TASKS ----------------
    fun addTask(
        title: String,
        description: String,
        scheduledTimestamp: Long?,
        category: String,
        notifyMe: Boolean,
        attachmentUris: String
    ) {
        if (title.isBlank()) return
        viewModelScope.launch {
            repository.addTask(title, description, scheduledTimestamp, category, notifyMe, attachmentUris)
            val task = TaskEntity(title = title, description = description, scheduledTimestamp = scheduledTimestamp, category = category, notifyMe = notifyMe, attachmentUris = attachmentUris)
            OrganiserStorageManager.persistTask(getApplication(), task)
        }
    }

    fun toggleTaskComplete(task: TaskEntity) {
        viewModelScope.launch {
            repository.toggleTaskComplete(task)
        }
    }

    fun deleteTask(task: TaskEntity) {
        viewModelScope.launch {
            OrganiserStorageManager.archiveDeletedTask(getApplication(), task)
            repository.deleteTask(task)
        }
    }

    fun updateTask(task: TaskEntity) {
        viewModelScope.launch {
            repository.updateTask(task)
        }
    }

    // ---------------- MUSIC & SCORE CARDS ----------------
    fun addMusicTrack(
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
    ) {
        if (songName.isBlank() || albumName.isBlank()) return
        viewModelScope.launch {
            repository.addMusicTrack(
                songName = songName,
                albumName = albumName,
                category = category,
                artist = artist,
                uriString = uriString,
                fileName = fileName,
                fileSizeBytes = fileSizeBytes,
                durationMs = durationMs,
                sourceType = sourceType,
                notes = notes
            )
        }
    }

    fun updateMusicTrack(track: MusicTrackEntity) {
        viewModelScope.launch {
            repository.updateMusicTrack(track)
        }
    }

    fun deleteMusicTrack(track: MusicTrackEntity) {
        viewModelScope.launch {
            repository.deleteMusicTrack(track)
        }
    }

    fun toggleMusicFavorite(trackId: Long, isFavorite: Boolean) {
        viewModelScope.launch {
            repository.toggleMusicFavorite(trackId, isFavorite)
        }
    }

    // ---------------- GLOBAL SETTINGS ----------------
    fun toggleGlobalLocation(enabled: Boolean) {
        globalSettings.update { it.copy(isLocationEnabled = enabled) }
    }

    fun setGlobalLocation(locationName: String, lat: Double = 37.7749, lng: Double = -122.4194, isAuto: Boolean = false) {
        globalSettings.update {
            it.copy(
                selectedLocation = locationName,
                latitude = lat,
                longitude = lng,
                isAutoLocation = isAuto,
                isLocationEnabled = true
            )
        }
    }

    fun updateFromGpsResult(result: com.example.util.GpsLocationResult) {
        globalSettings.update {
            it.copy(
                selectedLocation = result.locationName,
                latitude = result.latitude,
                longitude = result.longitude,
                locationAccuracy = "Accuracy ±%.1fm (${result.provider})".format(result.accuracyMeters),
                isAutoLocation = true,
                isLocationEnabled = true
            )
        }
    }

    fun fetchDeviceGpsLocation(context: android.content.Context) {
        com.example.util.GpsLocationTracker.getCurrentGpsLocation(context) { gpsResult ->
            updateFromGpsResult(gpsResult)
        }
    }

    fun toggleGlobalTranslation(enabled: Boolean) {
        globalSettings.update { it.copy(isTranslationEnabled = enabled) }
    }

    fun setGlobalLanguage(languageName: String, languageCode: String) {
        globalSettings.update {
            it.copy(
                targetLanguage = languageName,
                targetLanguageCode = languageCode,
                isTranslationEnabled = true
            )
        }
    }

    fun toggleAutoTranslate(enabled: Boolean) {
        globalSettings.update { it.copy(autoTranslateContent = enabled) }
    }

    fun setTranslationEngine(engineName: String) {
        globalSettings.update { it.copy(translationEngine = engineName) }
    }
}
