import re

with open("app/src/main/java/com/example/ui/LedgerViewModel.kt", "r") as f:
    content = f.read()

# Add OrganiserStorageManager imports if needed
if "import com.example.util.OrganiserStorageManager" not in content:
    content = content.replace("import com.example.util.NetworkState", "import com.example.util.NetworkState\nimport com.example.util.OrganiserStorageManager")

# In init, call OrganiserStorageManager.initOrganiserStorage(application)
init_pattern = """        viewModelScope.launch {
            repository.seedInitialDataIfNeeded()
            repository.runAutoSyncAndArchive()
        }"""
init_replacement = """        // Initialize My Organiser directory in local storage upon app startup
        viewModelScope.launch {
            OrganiserStorageManager.initOrganiserStorage(application)
            repository.seedInitialDataIfNeeded()
            repository.runAutoSyncAndArchive()
        }"""
if init_pattern in content:
    content = content.replace(init_pattern, init_replacement)

# Update addDiaryEntry
diary_add_pattern = """    fun addDiaryEntry(title: String, body: String, moodOrTag: String, isPinned: Boolean, notifyMe: Boolean = false, timestamp: Long = System.currentTimeMillis(), imageUri: String? = null) {
        if (title.isBlank() && body.isBlank()) return
        viewModelScope.launch {
            repository.addDiaryEntry(title, body, moodOrTag, isPinned, notifyMe, timestamp, imageUri)
        }
    }"""
diary_add_replacement = """    fun addDiaryEntry(title: String, body: String, moodOrTag: String, isPinned: Boolean, notifyMe: Boolean = false, timestamp: Long = System.currentTimeMillis(), imageUri: String? = null) {
        if (title.isBlank() && body.isBlank()) return
        viewModelScope.launch {
            repository.addDiaryEntry(title, body, moodOrTag, isPinned, notifyMe, timestamp, imageUri)
            val entry = DiaryEntryEntity(title = title, body = body, moodOrTag = moodOrTag, isPinned = isPinned, notifyMe = notifyMe, dateTimestamp = timestamp, imageUri = imageUri)
            OrganiserStorageManager.persistDiaryEntry(getApplication(), entry)
        }
    }"""
content = content.replace(diary_add_pattern, diary_add_replacement)

# Update deleteDiaryEntry
diary_del_pattern = """    fun deleteDiaryEntry(entry: DiaryEntryEntity) {
        viewModelScope.launch {
            repository.deleteDiaryEntry(entry)
        }
    }"""
diary_del_replacement = """    fun deleteDiaryEntry(entry: DiaryEntryEntity) {
        viewModelScope.launch {
            // Retain copy in My Organiser / Deleted_Archive before removing from UI
            OrganiserStorageManager.archiveDeletedDiaryEntry(getApplication(), entry)
            repository.deleteDiaryEntry(entry)
        }
    }"""
content = content.replace(diary_del_pattern, diary_del_replacement)

# Update addEvent
event_add_pattern = """    fun addEvent(title: String, locationOrNote: String, eventTimestamp: Long, notifyMe: Boolean, category: String = "General", includeYear: Boolean = true, isAllDay: Boolean = false, imageUri: String? = null) {
        if (title.isBlank()) return
        viewModelScope.launch {
            repository.addEvent(title, locationOrNote, eventTimestamp, notifyMe, category, includeYear, isAllDay, imageUri)
        }
    }"""
event_add_replacement = """    fun addEvent(title: String, locationOrNote: String, eventTimestamp: Long, notifyMe: Boolean, category: String = "General", includeYear: Boolean = true, isAllDay: Boolean = false, imageUri: String? = null) {
        if (title.isBlank()) return
        viewModelScope.launch {
            repository.addEvent(title, locationOrNote, eventTimestamp, notifyMe, category, includeYear, isAllDay, imageUri)
            val event = EventEntity(title = title, locationOrNote = locationOrNote, eventTimestamp = eventTimestamp, notifyMe = notifyMe, category = category, includeYear = includeYear, isAllDay = isAllDay, imageUri = imageUri)
            OrganiserStorageManager.persistEvent(getApplication(), event)
        }
    }"""
content = content.replace(event_add_pattern, event_add_replacement)

# Update deleteEvent
event_del_pattern = """    fun deleteEvent(event: EventEntity) {
        viewModelScope.launch {
            repository.deleteEvent(event)
        }
    }"""
event_del_replacement = """    fun deleteEvent(event: EventEntity) {
        viewModelScope.launch {
            OrganiserStorageManager.archiveDeletedEvent(getApplication(), event)
            repository.deleteEvent(event)
        }
    }"""
content = content.replace(event_del_pattern, event_del_replacement)

# Update addVaultDocument
vault_add_pattern = """    fun addVaultDocument(
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
        }
    }"""
vault_add_replacement = """    fun addVaultDocument(
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
    }"""
content = content.replace(vault_add_pattern, vault_add_replacement)

# Update deleteVaultDocument
vault_del_pattern = """    fun deleteVaultDocument(document: VaultDocumentEntity) {
        viewModelScope.launch {
            repository.deleteVaultDocument(document)
        }
    }"""
vault_del_replacement = """    fun deleteVaultDocument(document: VaultDocumentEntity) {
        viewModelScope.launch {
            // Keep in My Organiser / Deleted_Archive so data is preserved locally even if removed from UI
            OrganiserStorageManager.archiveDeletedVaultDocument(getApplication(), document)
            repository.deleteVaultDocument(document)
        }
    }"""
content = content.replace(vault_del_pattern, vault_del_replacement)

# Update addTask
task_add_pattern = """    fun addTask(
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
        }
    }"""
task_add_replacement = """    fun addTask(
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
    }"""
content = content.replace(task_add_pattern, task_add_replacement)

# Update deleteTask
task_del_pattern = """    fun deleteTask(task: TaskEntity) {
        viewModelScope.launch {
            repository.deleteTask(task)
        }
    }"""
task_del_replacement = """    fun deleteTask(task: TaskEntity) {
        viewModelScope.launch {
            OrganiserStorageManager.archiveDeletedTask(getApplication(), task)
            repository.deleteTask(task)
        }
    }"""
content = content.replace(task_del_pattern, task_del_replacement)

with open("app/src/main/java/com/example/ui/LedgerViewModel.kt", "w") as f:
    f.write(content)

print("ViewModel updated successfully")
