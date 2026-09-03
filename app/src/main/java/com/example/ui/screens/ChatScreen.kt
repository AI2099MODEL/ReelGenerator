package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.ChatMessageEntity
import com.example.data.model.ChatThreadEntity
import com.example.ui.components.AppHeaderLogo
import com.example.ui.components.HeaderSkyCloudsBackground
import com.example.ui.components.LedgerEmptyState
import com.example.ui.components.NetworkQueueDialog
import com.example.ui.components.NetworkStatusChip
import com.example.ui.components.OfflineStatusBanner
import com.example.ui.theme.*
import com.example.util.BackupRestoreResult
import com.example.util.NetworkSimulationMode
import com.example.util.NetworkState
import com.example.util.QRCodeGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.*

// Upload Limits (Industry Standards: Signal / WhatsApp / Messenger)
private const val MAX_IMAGE_SIZE_BYTES = 25L * 1024 * 1024 // 25 MB
private const val MAX_VIDEO_SIZE_BYTES = 100L * 1024 * 1024 // 100 MB
private const val MAX_DOC_AUDIO_SIZE_BYTES = 50L * 1024 * 1024 // 50 MB
private const val MAX_OVERALL_SIZE_BYTES = 100L * 1024 * 1024 // 100 MB

private data class PendingAttachment(
    val uriString: String,
    val fileName: String,
    val fileType: String, // "IMAGE", "VIDEO", "DOC", "AUDIO", "FILE"
    val fileSizeBytes: Long = 0L
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    threads: List<ChatThreadEntity>,
    activeThreadKey: String,
    messages: List<ChatMessageEntity>,
    archivedMessages: List<ChatMessageEntity>,
    archivedCount: Int,
    onSelectThread: (String) -> Unit,
    onSendMessage: (String, String, String?, String?, String?, Long) -> Unit,
    onTriggerAutoSync: ((Int) -> Unit) -> Unit,
    onRestoreArchived: (Long) -> Unit,
    onDeleteMessage: (ChatMessageEntity) -> Unit,
    onEditMessage: ((Long, String) -> Unit)? = null,
    onRenameCategory: ((String, String) -> Unit)? = null,
    onExportBackup: suspend () -> String,
    onRestoreBackup: (String, (BackupRestoreResult) -> Unit) -> Unit,
    onShareBackupToGoogleDrive: () -> Unit,
    networkState: NetworkState? = null,
    pendingSyncCount: Int = 0,
    onSyncQueuedMessages: (() -> Unit)? = null,
    onSetSimulationMode: ((NetworkSimulationMode) -> Unit)? = null,
    modifier: Modifier = Modifier,
    onMenuClick: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    var inputText by remember { mutableStateOf("") }
    var pendingAttachment by remember { mutableStateOf<PendingAttachment?>(null) }
    var showQrDialog by remember { mutableStateOf(false) }
    var showBackupDialog by remember { mutableStateOf(false) }
    var showArchiveDialog by remember { mutableStateOf(false) }
    var showNetworkDialog by remember { mutableStateOf(false) }
    var showAttachmentPickerSheet by remember { mutableStateOf(false) }
    var isSyncing by remember { mutableStateOf(false) }
    var backupStatusMessage by remember { mutableStateOf<String?>(null) }

    // Editing Chat Message Dialog
    var editingMessage by remember { mutableStateOf<ChatMessageEntity?>(null) }
    var editContentText by remember { mutableStateOf("") }

    // Renaming Category Dialog
    var renamingCategoryTarget by remember { mutableStateOf<CategoryTabInfo?>(null) }
    var newCategoryNameText by remember { mutableStateOf("") }

    // Helper for validating file size limit
    fun checkAndSetAttachment(uri: Uri, isVideo: Boolean = false) {
        val (name, size) = queryFileInfo(context, uri)
        val ext = name.substringAfterLast('.', "").uppercase()
        val type = when {
            isVideo || ext in listOf("MP4", "MKV", "3GP", "MOV", "WEBM", "AVI") -> "VIDEO"
            ext in listOf("JPG", "JPEG", "PNG", "WEBP", "GIF", "HEIC", "BMP") -> "IMAGE"
            ext in listOf("PDF", "DOC", "DOCX", "TXT", "XLS", "XLSX", "PPT", "PPTX") -> "DOC"
            ext in listOf("MP3", "M4A", "WAV", "AAC", "OGG") -> "AUDIO"
            else -> "FILE"
        }

        val limitBytes = when (type) {
            "IMAGE" -> MAX_IMAGE_SIZE_BYTES
            "VIDEO" -> MAX_VIDEO_SIZE_BYTES
            "DOC", "AUDIO" -> MAX_DOC_AUDIO_SIZE_BYTES
            else -> MAX_OVERALL_SIZE_BYTES
        }

        if (size > limitBytes) {
            val limitMb = limitBytes / (1024 * 1024)
            Toast.makeText(
                context,
                "File too large! Max $type limit is $limitMb MB. (Selected: ${formatFileSize(size)})",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        pendingAttachment = PendingAttachment(
            uriString = uri.toString(),
            fileName = name.ifEmpty { "${type.lowercase()}_${System.currentTimeMillis()}" },
            fileType = type,
            fileSizeBytes = size
        )
        Toast.makeText(context, "Attached: $name (${formatFileSize(size)})", Toast.LENGTH_SHORT).show()
    }

    // Media / Image Picker Launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            checkAndSetAttachment(uri, isVideo = false)
        }
        showAttachmentPickerSheet = false
    }

    // Video Picker Launcher
    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            checkAndSetAttachment(uri, isVideo = true)
        }
        showAttachmentPickerSheet = false
    }

    // Document / General File Picker Launcher
    val documentPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            checkAndSetAttachment(uri, isVideo = false)
        }
        showAttachmentPickerSheet = false
    }

    // SAF File picker for restoring backup JSON
    val openDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            coroutineScope.launch(Dispatchers.IO) {
                try {
                    val contentResolver = context.contentResolver
                    val stringBuilder = StringBuilder()
                    contentResolver.openInputStream(uri)?.use { inputStream ->
                        BufferedReader(InputStreamReader(inputStream, Charsets.UTF_8)).use { reader ->
                            var line = reader.readLine()
                            while (line != null) {
                                stringBuilder.append(line)
                                line = reader.readLine()
                            }
                        }
                    }
                    val jsonContent = stringBuilder.toString()
                    withContext(Dispatchers.Main) {
                        onRestoreBackup(jsonContent) { result ->
                            backupStatusMessage = if (result.success) {
                                "Restored: ${result.threadsCount} categories, ${result.messagesCount} chats, ${result.diaryCount} diary entries."
                            } else {
                                "Restore failed: ${result.message}"
                            }
                            Toast.makeText(context, backupStatusMessage, Toast.LENGTH_LONG).show()
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        backupStatusMessage = "Error reading backup: ${e.localizedMessage ?: e.message}"
                        Toast.makeText(context, backupStatusMessage, Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    // SAF File creator for exporting backup JSON directly
    val createDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        if (uri != null) {
            coroutineScope.launch(Dispatchers.IO) {
                try {
                    val json = onExportBackup()
                    context.contentResolver.openOutputStream(uri)?.use { output ->
                        output.write(json.toByteArray(Charsets.UTF_8))
                    }
                    withContext(Dispatchers.Main) {
                        backupStatusMessage = "Backup saved successfully! Ready for Google Drive."
                        Toast.makeText(context, backupStatusMessage, Toast.LENGTH_LONG).show()
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        backupStatusMessage = "Export failed: ${e.localizedMessage ?: e.message}"
                        Toast.makeText(context, backupStatusMessage, Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    // The 4 categories with custom or standard titles
    val defaultTabDefinitions = listOf(
        CategoryTabInfo("family", "Family", "🏡"),
        CategoryTabInfo("work", "Work", "💼"),
        CategoryTabInfo("personal", "Personal", "🌿"),
        CategoryTabInfo("utility", "Utility", "⚡")
    )

    val standardCategories = defaultTabDefinitions.map { def ->
        val matchingThread = threads.firstOrNull { it.threadKey.equals(def.key, ignoreCase = true) }
        val displayTitle = matchingThread?.category?.takeIf { it.isNotBlank() }
            ?: matchingThread?.name?.takeIf { it.isNotBlank() }
            ?: def.title
        val displayEmoji = matchingThread?.iconEmoji?.takeIf { it.isNotBlank() } ?: def.emoji
        def.copy(title = displayTitle, emoji = displayEmoji)
    }

    val currentTab = standardCategories.firstOrNull { it.key.equals(activeThreadKey, ignoreCase = true) }
        ?: standardCategories.first()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Transparent)
    ) {
        // Top Header with Animated Floating Sky and White Clouds
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            HeaderSkyCloudsBackground(
                modifier = Modifier.matchParentSize()
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AppHeaderLogo(size = 32.dp)

                    Text(
                        text = "ChitChat",
                        fontSize = 22.sp,
                        fontFamily = FontFamily.Cursive,
                        fontWeight = FontWeight.Bold,
                        color = RoseQuartzTextPrimary,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Network Signal / Offline Chip in Chat Top Bar
                    if (networkState != null) {
                        NetworkStatusChip(
                            networkState = networkState,
                            pendingSyncCount = pendingSyncCount,
                            onOpenNetworkDialog = { showNetworkDialog = true }
                        )
                    }

                    // QR Code Share
                    IconButton(
                        onClick = { showQrDialog = true },
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(RoseQuartzContainerLowest),
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.QrCode2,
                            contentDescription = "Share Category QR Code",
                            tint = RoseQuartzPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Right-hand side Submenu / Drawer Button
                    if (onMenuClick != null) {
                        IconButton(
                            onClick = onMenuClick,
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(RoseQuartzPrimaryContainer)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Menu,
                                contentDescription = "Open Side Menu",
                                tint = RoseQuartzPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }



        // Strictly 4 Categories Tab Bar with dynamic titles and edit button
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 2.dp),
            shape = RoundedCornerShape(12.dp),
            color = RoseQuartzContainerLowest.copy(alpha = 0.9f),
            border = BorderStroke(1.dp, RoseQuartzContainerHighest)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                standardCategories.forEach { tab ->
                    val isSelected = tab.key.equals(activeThreadKey, ignoreCase = true)
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onSelectThread(tab.key) },
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) RoseQuartzPrimary else Color.Transparent
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(vertical = 8.dp, horizontal = 2.dp)
                        ) {
                            Text(text = tab.emoji, fontSize = 12.sp)
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = tab.title,
                                color = if (isSelected) RoseQuartzOnPrimary else RoseQuartzTextPrimary,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 11.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (isSelected && onRenameCategory != null) {
                                Spacer(modifier = Modifier.width(2.dp))
                                Icon(
                                    imageVector = Icons.Filled.Edit,
                                    contentDescription = "Rename Category",
                                    tint = RoseQuartzOnPrimary.copy(alpha = 0.85f),
                                    modifier = Modifier
                                        .size(11.dp)
                                        .clickable {
                                            renamingCategoryTarget = tab
                                            newCategoryNameText = tab.title
                                        }
                                )
                            }
                        }
                    }
                }
            }
        }

        // 5-Day Auto-Sync & Active Status Bar (No mobile numbers in chat screen)
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            shape = RoundedCornerShape(10.dp),
            color = RoseQuartzContainerLowest.copy(alpha = 0.85f),
            border = BorderStroke(1.dp, RoseQuartzContainerHighest)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(RoseQuartzPrimary)
                    )
                    Text(
                        text = "Chat Stream (${messages.size} messages)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = RoseQuartzTextPrimary
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (archivedMessages.isNotEmpty()) {
                        TextButton(
                            onClick = { showArchiveDialog = true },
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp),
                            modifier = Modifier.height(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Archive,
                                contentDescription = "View Archive",
                                tint = RoseQuartzPrimary,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "Archive (${archivedMessages.size})",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = RoseQuartzPrimary
                            )
                        }
                    } else {
                        Text(
                            text = "⚡ Auto-Sync Active",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = RoseQuartzTextSecondary
                        )
                    }
                }
            }
        }

        // Messages Feed: Latest on top (sorted descending)
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(top = 8.dp, bottom = 12.dp)
            ) {
                items(messages, key = { it.id }) { msg ->
                    ChatMessageBubble(
                        message = msg,
                        onDelete = { onDeleteMessage(msg) },
                        onEdit = if (onEditMessage != null) {
                            {
                                editingMessage = msg
                                editContentText = msg.content
                            }
                        } else null
                    )
                }
            }
        }

        // Pending Attachment Bar (if selected)
        AnimatedVisibility(
            visible = pendingAttachment != null,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            if (pendingAttachment != null) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 2.dp),
                    shape = RoundedCornerShape(10.dp),
                    color = RoseQuartzPrimaryContainer,
                    border = BorderStroke(1.dp, RoseQuartzPrimary.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = when (pendingAttachment?.fileType) {
                                    "IMAGE" -> Icons.Outlined.Image
                                    "AUDIO" -> Icons.Outlined.Audiotrack
                                    else -> Icons.Outlined.Description
                                },
                                contentDescription = "Attachment",
                                tint = RoseQuartzPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                            Column {
                                Text(
                                    text = pendingAttachment?.fileName.orEmpty(),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = RoseQuartzTextPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = formatFileSize(pendingAttachment?.fileSizeBytes ?: 0L) + " • " + (pendingAttachment?.fileType ?: "FILE"),
                                    fontSize = 9.sp,
                                    color = RoseQuartzTextSecondary
                                )
                            }
                        }

                        IconButton(
                            onClick = { pendingAttachment = null },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "Remove attachment",
                                tint = RoseQuartzPrimary,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }
        }

        // Input Bar with Attachment Trigger and Send Button
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            shape = RoundedCornerShape(14.dp),
            color = RoseQuartzContainerLowest.copy(alpha = 0.95f),
            border = BorderStroke(1.dp, RoseQuartzContainerHighest),
            shadowElevation = 3.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Attach Button (Photo / Document / File)
                IconButton(
                    onClick = { showAttachmentPickerSheet = true },
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (pendingAttachment != null) RoseQuartzPrimaryContainer else Color.Transparent)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.AttachFile,
                        contentDescription = "Attach file",
                        tint = if (pendingAttachment != null) RoseQuartzPrimary else RoseQuartzTextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = {
                        Text(
                            text = if (pendingAttachment != null) "Add a caption..." else "Message in ${currentTab.title}...",
                            color = RoseQuartzTextMuted,
                            fontSize = 13.sp
                        )
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        focusedTextColor = RoseQuartzTextPrimary,
                        unfocusedTextColor = RoseQuartzTextPrimary,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent
                    ),
                    maxLines = 4,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(onSend = {
                        if (inputText.isNotBlank() || pendingAttachment != null) {
                            onSendMessage(
                                activeThreadKey,
                                inputText.trim(),
                                pendingAttachment?.uriString,
                                pendingAttachment?.fileName,
                                pendingAttachment?.fileType,
                                pendingAttachment?.fileSizeBytes ?: 0L
                            )
                            inputText = ""
                            pendingAttachment = null
                            focusManager.clearFocus()
                        }
                    })
                )

                val canSend = inputText.isNotBlank() || pendingAttachment != null

                IconButton(
                    onClick = {
                        if (canSend) {
                            onSendMessage(
                                activeThreadKey,
                                inputText.trim(),
                                pendingAttachment?.uriString,
                                pendingAttachment?.fileName,
                                pendingAttachment?.fileType,
                                pendingAttachment?.fileSizeBytes ?: 0L
                            )
                            inputText = ""
                            pendingAttachment = null
                            focusManager.clearFocus()
                        }
                    },
                    enabled = canSend,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (canSend) RoseQuartzPrimary else RoseQuartzContainerHigh)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send",
                        tint = if (canSend) RoseQuartzOnPrimary else RoseQuartzTextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }

    // ----------------------------------------------------
    // ATTACHMENT PICKER MODAL SHEET
    // ----------------------------------------------------
    if (showAttachmentPickerSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAttachmentPickerSheet = false },
            containerColor = RoseQuartzContainerLowest,
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Add Attachment to ${currentTab.title}",
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = RoseQuartzTextPrimary
                )

                Text(
                    text = "Attach photos (up to 25MB), videos (up to 100MB), documents (up to 50MB), or audio recordings. Files stay local and private.",
                    fontSize = 12.sp,
                    color = RoseQuartzTextSecondary
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    AttachmentOptionButton(
                        icon = Icons.Outlined.Image,
                        label = "Image (25MB)",
                        onClick = { imagePickerLauncher.launch("image/*") }
                    )

                    AttachmentOptionButton(
                        icon = Icons.Outlined.Videocam,
                        label = "Video (100MB)",
                        onClick = { videoPickerLauncher.launch("video/*") }
                    )

                    AttachmentOptionButton(
                        icon = Icons.Outlined.Description,
                        label = "Doc / PDF (50MB)",
                        onClick = { documentPickerLauncher.launch("*/*") }
                    )

                    AttachmentOptionButton(
                        icon = Icons.Outlined.Audiotrack,
                        label = "Audio (50MB)",
                        onClick = { documentPickerLauncher.launch("audio/*") }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // ----------------------------------------------------
    // 5-DAY ARCHIVED CHATS DIALOG
    // ----------------------------------------------------
    if (showArchiveDialog) {
        AlertDialog(
            onDismissRequest = { showArchiveDialog = false },
            containerColor = RoseQuartzContainerLowest,
            shape = RoundedCornerShape(16.dp),
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Archive,
                        contentDescription = "Archived chats",
                        tint = RoseQuartzPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                    Column {
                        Text(
                            text = "${currentTab.title} Archived Chats",
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Bold,
                            color = RoseQuartzTextPrimary,
                            fontSize = 17.sp
                        )
                        Text(
                            text = "Old chats (>5 days) auto-synced to local archive",
                            fontSize = 11.sp,
                            color = RoseQuartzTextSecondary
                        )
                    }
                }
            },
            text = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 350.dp)
                ) {
                    if (archivedMessages.isEmpty()) {
                        Text(
                            text = "No archived messages for ${currentTab.title}. Active messages stay visible for 5 days.",
                            fontSize = 12.sp,
                            color = RoseQuartzTextSecondary,
                            modifier = Modifier.padding(12.dp)
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(archivedMessages, key = { it.id }) { archMsg ->
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = RoseQuartzBg.copy(alpha = 0.9f),
                                    border = BorderStroke(1.dp, RoseQuartzContainerHighest),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Text(
                                                    text = if (archMsg.isSentByUser) "YOU" else archMsg.senderName.uppercase(),
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = RoseQuartzPrimary
                                                )
                                                Text(
                                                    text = SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(archMsg.timestamp)),
                                                    fontSize = 9.sp,
                                                    color = RoseQuartzTextMuted
                                                )
                                            }
                                            Text(
                                                text = archMsg.content.ifEmpty { "📎 ${archMsg.attachmentName.orEmpty()}" },
                                                fontSize = 12.sp,
                                                color = RoseQuartzTextPrimary,
                                                maxLines = 2,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }

                                        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                            IconButton(
                                                onClick = {
                                                    onRestoreArchived(archMsg.id)
                                                    Toast.makeText(context, "Restored message to active thread!", Toast.LENGTH_SHORT).show()
                                                },
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Outlined.Unarchive,
                                                    contentDescription = "Restore",
                                                    tint = RoseQuartzPrimary,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }

                                            IconButton(
                                                onClick = {
                                                    onDeleteMessage(archMsg)
                                                    Toast.makeText(context, "Deleted archived message", Toast.LENGTH_SHORT).show()
                                                },
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Outlined.Delete,
                                                    contentDescription = "Delete",
                                                    tint = RoseQuartzTextMuted,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showArchiveDialog = false }) {
                    Text("Close", color = RoseQuartzTextSecondary)
                }
            }
        )
    }

    // ----------------------------------------------------
    // CATEGORY QR CODE SHARING DIALOG
    // ----------------------------------------------------
    if (showQrDialog) {
        val qrPayload = remember(currentTab.title, messages) {
            val latestMsg = messages.firstOrNull()?.content ?: "Stream active"
            val messageTranscript = messages.take(5).joinToString("\n") {
                val who = if (it.isSentByUser) "You" else it.senderName
                val attach = if (it.attachmentName != null) " [📎 ${it.attachmentName}]" else ""
                "• $who: ${it.content}$attach"
            }
            buildString {
                appendLine("═══ MYLYFE: ${currentTab.emoji} ${currentTab.title.uppercase()} ═══")
                appendLine("Category: ${currentTab.title}")
                appendLine("Active Window: 5-Day Auto-Sync")
                appendLine("Active Messages: ${messages.size}")
                if (messageTranscript.isNotBlank()) {
                    appendLine("\nRecent Messages:")
                    appendLine(messageTranscript)
                }
                appendLine("\nLatest Note: $latestMsg")
                appendLine("Generated: ${SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).format(Date())}")
                appendLine("Offline Ready • Mobile Signal Compatible")
            }
        }

        val qrBitmap: ImageBitmap? = remember(qrPayload) {
            QRCodeGenerator.generateQrCodeBitmap(qrPayload, size = 512)
        }

        AlertDialog(
            onDismissRequest = { showQrDialog = false },
            containerColor = RoseQuartzContainerLowest,
            shape = RoundedCornerShape(16.dp),
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(text = currentTab.emoji, fontSize = 22.sp)
                    Column {
                        Text(
                            text = "${currentTab.title} QR Share Code",
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Bold,
                            color = RoseQuartzTextPrimary,
                            fontSize = 17.sp
                        )
                        Text(
                            text = "Scan to transfer active ${currentTab.title} notes offline",
                            fontSize = 11.sp,
                            color = RoseQuartzTextSecondary
                        )
                    }
                }
            },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White,
                        border = BorderStroke(1.dp, RoseQuartzContainerHighest),
                        modifier = Modifier
                            .size(220.dp)
                            .padding(4.dp)
                    ) {
                        if (qrBitmap != null) {
                            Image(
                                bitmap = qrBitmap,
                                contentDescription = "QR Code for ${currentTab.title}",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(8.dp)
                            )
                        } else {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                CircularProgressIndicator(color = RoseQuartzPrimary)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Transfers 5-day active message stream and attachment references.",
                        fontSize = 11.sp,
                        color = RoseQuartzTextSecondary
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedButton(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("${currentTab.title} Chat Details", qrPayload)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "Copied ${currentTab.title} transcript to clipboard!", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, RoseQuartzPrimary)
                        ) {
                            Icon(Icons.Outlined.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp), tint = RoseQuartzPrimary)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Copy Text", fontSize = 12.sp, color = RoseQuartzPrimary)
                        }

                        Button(
                            onClick = {
                                val sendIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, qrPayload)
                                    putExtra(Intent.EXTRA_SUBJECT, "MyLyfe ${currentTab.title} Transcript")
                                    type = "text/plain"
                                }
                                val shareIntent = Intent.createChooser(sendIntent, "Share ${currentTab.title} Transcript")
                                context.startActivity(shareIntent)
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = RoseQuartzPrimary)
                        ) {
                            Icon(Icons.Outlined.Share, contentDescription = null, modifier = Modifier.size(14.dp), tint = RoseQuartzOnPrimary)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Share", fontSize = 12.sp, color = RoseQuartzOnPrimary)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showQrDialog = false }) {
                    Text("Close", color = RoseQuartzTextSecondary)
                }
            }
        )
    }

    // ----------------------------------------------------
    // GOOGLE DRIVE BACKUP & RESTORE DIALOG
    // ----------------------------------------------------
    if (showBackupDialog) {
        AlertDialog(
            onDismissRequest = { showBackupDialog = false },
            containerColor = RoseQuartzContainerLowest,
            shape = RoundedCornerShape(16.dp),
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.CloudSync,
                        contentDescription = "Google Drive Cloud Backup",
                        tint = RoseQuartzPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                    Column {
                        Text(
                            text = "Google Drive Backup",
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Bold,
                            color = RoseQuartzTextPrimary,
                            fontSize = 18.sp
                        )
                        Text(
                            text = "Save & restore full MyLyfe archives",
                            fontSize = 11.sp,
                            color = RoseQuartzTextSecondary
                        )
                    }
                }
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = RoseQuartzPrimaryContainer.copy(alpha = 0.5f),
                        border = BorderStroke(1.dp, RoseQuartzPrimaryContainer),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "☁️ Google Drive Cloud Archive",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = RoseQuartzPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Backups include all 4 categories (Family, Work, Personal, Utility), 5-day active & archived messages, attachments metadata, diary reflections, and vault documents.",
                                fontSize = 11.sp,
                                color = RoseQuartzTextSecondary,
                                lineHeight = 15.sp
                            )
                        }
                    }

                    if (backupStatusMessage != null) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = RoseQuartzContainerHigh,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = backupStatusMessage!!,
                                fontSize = 11.sp,
                                color = RoseQuartzPrimary,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }

                    Button(
                        onClick = { onShareBackupToGoogleDrive() },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = RoseQuartzPrimary,
                            contentColor = RoseQuartzOnPrimary
                        )
                    ) {
                        Icon(Icons.Outlined.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Export to Google Drive", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }

                    OutlinedButton(
                        onClick = {
                            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
                            createDocumentLauncher.launch("mylyfe_backup_$timestamp.json")
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, RoseQuartzPrimary)
                    ) {
                        Icon(Icons.Outlined.SaveAlt, contentDescription = null, modifier = Modifier.size(16.dp), tint = RoseQuartzPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Save JSON File Locally", fontSize = 12.sp, color = RoseQuartzPrimary)
                    }

                    FilledTonalButton(
                        onClick = {
                            openDocumentLauncher.launch(arrayOf("application/json", "text/*", "*/*"))
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = RoseQuartzContainerHighest,
                            contentColor = RoseQuartzTextPrimary
                        )
                    ) {
                        Icon(Icons.Outlined.RestorePage, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Restore from Google Drive / File", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showBackupDialog = false }) {
                    Text("Close", color = RoseQuartzTextSecondary)
                }
            }
        )
    }

    // ----------------------------------------------------
    // NETWORK & QUEUED SYNC INSPECTOR DIALOG
    // ----------------------------------------------------
    if (showNetworkDialog && networkState != null) {
        NetworkQueueDialog(
            networkState = networkState,
            pendingSyncCount = pendingSyncCount,
            onSyncNow = {
                onSyncQueuedMessages?.invoke()
            },
            onSetSimulationMode = { mode ->
                onSetSimulationMode?.invoke(mode)
            },
            onDismiss = { showNetworkDialog = false }
        )
    }

    // ----------------------------------------------------
    // EDIT CHAT MESSAGE DIALOG
    // ----------------------------------------------------
    if (editingMessage != null) {
        val targetMsg = editingMessage!!
        AlertDialog(
            onDismissRequest = { editingMessage = null },
            containerColor = RoseQuartzContainerLowest,
            shape = RoundedCornerShape(16.dp),
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = null,
                        tint = RoseQuartzPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Edit Message",
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        color = RoseQuartzTextPrimary,
                        fontSize = 17.sp
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Modify your chat note. Changes are saved locally and update thread previews.",
                        fontSize = 11.sp,
                        color = RoseQuartzTextSecondary
                    )
                    OutlinedTextField(
                        value = editContentText,
                        onValueChange = { editContentText = it },
                        placeholder = { Text("Enter updated message...", fontSize = 12.sp, color = RoseQuartzTextMuted) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 100.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = RoseQuartzPrimary,
                            unfocusedBorderColor = RoseQuartzContainerHighest,
                            focusedTextColor = RoseQuartzTextPrimary,
                            unfocusedTextColor = RoseQuartzTextPrimary
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (editContentText.isNotBlank()) {
                            onEditMessage?.invoke(targetMsg.id, editContentText.trim())
                            editingMessage = null
                            Toast.makeText(context, "Message updated successfully", Toast.LENGTH_SHORT).show()
                        }
                    },
                    enabled = editContentText.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = RoseQuartzPrimary,
                        contentColor = RoseQuartzOnPrimary
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Save Edit", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { editingMessage = null }) {
                    Text("Cancel", color = RoseQuartzTextSecondary)
                }
            }
        )
    }

    // ----------------------------------------------------
    // RENAME CATEGORY DIALOG (Work, Family, Personal, Utility)
    // ----------------------------------------------------
    if (renamingCategoryTarget != null) {
        val targetCat = renamingCategoryTarget!!
        AlertDialog(
            onDismissRequest = { renamingCategoryTarget = null },
            containerColor = RoseQuartzContainerLowest,
            shape = RoundedCornerShape(16.dp),
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(text = targetCat.emoji, fontSize = 20.sp)
                    Text(
                        text = "Rename Category",
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        color = RoseQuartzTextPrimary,
                        fontSize = 17.sp
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Rename '${targetCat.title}' across the entire app (chat streams, contacts, and side ledger). The app maintains its 4 core streams.",
                        fontSize = 11.sp,
                        color = RoseQuartzTextSecondary
                    )
                    OutlinedTextField(
                        value = newCategoryNameText,
                        onValueChange = { newCategoryNameText = it },
                        label = { Text("Category Name") },
                        placeholder = { Text("e.g. Clients, Project Alpha, Household") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = RoseQuartzPrimary,
                            unfocusedBorderColor = RoseQuartzContainerHighest,
                            focusedTextColor = RoseQuartzTextPrimary,
                            unfocusedTextColor = RoseQuartzTextPrimary
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newCategoryNameText.isNotBlank()) {
                            onRenameCategory?.invoke(targetCat.key, newCategoryNameText.trim())
                            renamingCategoryTarget = null
                            Toast.makeText(context, "Category renamed to '${newCategoryNameText.trim()}'", Toast.LENGTH_SHORT).show()
                        }
                    },
                    enabled = newCategoryNameText.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = RoseQuartzPrimary,
                        contentColor = RoseQuartzOnPrimary
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Rename", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { renamingCategoryTarget = null }) {
                    Text("Cancel", color = RoseQuartzTextSecondary)
                }
            }
        )
    }
}

private data class CategoryTabInfo(
    val key: String,
    val title: String,
    val emoji: String
)

@Composable
private fun AttachmentOptionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(8.dp)
    ) {
        Surface(
            shape = CircleShape,
            color = RoseQuartzPrimaryContainer,
            modifier = Modifier.size(48.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = RoseQuartzPrimary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = RoseQuartzTextPrimary
        )
    }
}

@Composable
private fun ChatMessageBubble(
    message: ChatMessageEntity,
    onDelete: () -> Unit,
    onEdit: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val isUser = message.isSentByUser
    val timeFormatter = remember { SimpleDateFormat("MMM d, hh:mm a", Locale.getDefault()) }
    val formattedTime = remember(message.timestamp) { timeFormatter.format(Date(message.timestamp)) }

    val bubbleBg = if (isUser) RoseQuartzPrimary else RoseQuartzContainerLowest
    val textColor = if (isUser) RoseQuartzOnPrimary else RoseQuartzTextPrimary
    val borderColor = if (isUser) RoseQuartzPrimary else RoseQuartzContainerHighest

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Column(
            modifier = Modifier.widthIn(max = 300.dp),
            horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
        ) {
            Surface(
                shape = RoundedCornerShape(
                    topStart = 14.dp,
                    topEnd = 14.dp,
                    bottomStart = if (isUser) 14.dp else 2.dp,
                    bottomEnd = if (isUser) 2.dp else 14.dp
                ),
                color = bubbleBg,
                border = BorderStroke(1.dp, borderColor),
                shadowElevation = 1.dp
            ) {
                Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                    if (!isUser) {
                        Text(
                            text = message.senderName.ifBlank { "ASSISTANT" }.uppercase(),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = RoseQuartzPrimary,
                            letterSpacing = 0.8.sp,
                            modifier = Modifier.padding(bottom = 2.dp)
                        )
                    }

                    // Render Attachment if present
                    if (!message.attachmentUri.isNullOrBlank() || !message.attachmentName.isNullOrBlank()) {
                        val uriStr = message.attachmentUri.orEmpty()
                        val fileName = message.attachmentName.orEmpty().ifEmpty { "Attachment" }
                        val fileType = message.attachmentType.orEmpty().ifEmpty { "FILE" }

                        if (fileType == "IMAGE" && uriStr.isNotBlank()) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isUser) Color.Black.copy(alpha = 0.15f) else RoseQuartzContainerHigh,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 180.dp)
                                    .padding(bottom = 6.dp)
                                    .clickable {
                                        try {
                                            val viewIntent = Intent(Intent.ACTION_VIEW).apply {
                                                setDataAndType(Uri.parse(uriStr), "image/*")
                                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                            }
                                            context.startActivity(viewIntent)
                                        } catch (e: Exception) {
                                            Toast.makeText(context, "Image: $fileName", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                            ) {
                                AsyncImage(
                                    model = uriStr,
                                    contentDescription = fileName,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        } else if (fileType == "VIDEO" && uriStr.isNotBlank()) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isUser) Color.Black.copy(alpha = 0.25f) else RoseQuartzContainerHigh,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 6.dp)
                                    .clickable {
                                        try {
                                            val viewIntent = Intent(Intent.ACTION_VIEW).apply {
                                                setDataAndType(Uri.parse(uriStr), "video/*")
                                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                            }
                                            context.startActivity(viewIntent)
                                        } catch (e: Exception) {
                                            Toast.makeText(context, "Video: $fileName", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = if (isUser) Color.White.copy(alpha = 0.25f) else RoseQuartzPrimaryContainer,
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Filled.PlayArrow,
                                                contentDescription = "Play Video",
                                                tint = if (isUser) RoseQuartzOnPrimary else RoseQuartzPrimary,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = fileName,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isUser) RoseQuartzOnPrimary else RoseQuartzTextPrimary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = formatFileSize(message.attachmentSizeBytes) + " • Video (Tap to Play)",
                                            fontSize = 9.sp,
                                            color = if (isUser) RoseQuartzOnPrimary.copy(alpha = 0.8f) else RoseQuartzTextMuted
                                        )
                                    }
                                }
                            }
                        } else {
                            // Document / Audio / File Attachment Card
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isUser) Color.Black.copy(alpha = 0.15f) else RoseQuartzContainerHigh,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 6.dp)
                                    .clickable {
                                        if (uriStr.isNotBlank()) {
                                            try {
                                                val viewIntent = Intent(Intent.ACTION_VIEW).apply {
                                                    data = Uri.parse(uriStr)
                                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                                }
                                                context.startActivity(viewIntent)
                                            } catch (e: Exception) {
                                                Toast.makeText(context, "Attachment: $fileName", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = when (fileType) {
                                            "DOC" -> Icons.Outlined.Description
                                            "AUDIO" -> Icons.Outlined.Audiotrack
                                            else -> Icons.Outlined.InsertDriveFile
                                        },
                                        contentDescription = fileType,
                                        tint = if (isUser) RoseQuartzOnPrimary else RoseQuartzPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = fileName,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isUser) RoseQuartzOnPrimary else RoseQuartzTextPrimary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = formatFileSize(message.attachmentSizeBytes) + " • " + fileType,
                                            fontSize = 9.sp,
                                            color = if (isUser) RoseQuartzOnPrimary.copy(alpha = 0.8f) else RoseQuartzTextMuted
                                        )
                                    }
                                }
                            }
                        }
                    }

                    if (message.content.isNotBlank()) {
                        Text(
                            text = message.content,
                            fontSize = 13.sp,
                            color = textColor,
                            lineHeight = 18.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.padding(horizontal = 4.dp)
            ) {
                Text(
                    text = formattedTime,
                    fontSize = 10.sp,
                    color = RoseQuartzTextMuted
                )

                Spacer(modifier = Modifier.width(2.dp))
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = "Delete message",
                    tint = RoseQuartzTextMuted,
                    modifier = Modifier
                        .size(12.dp)
                        .clickable { onDelete() }
                )

                if (isUser) {
                    if (onEdit != null) {
                        Spacer(modifier = Modifier.width(2.dp))
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = "Edit message",
                            tint = RoseQuartzTextMuted,
                            modifier = Modifier
                                .size(12.dp)
                                .clickable { onEdit() }
                        )
                    }

                    if (message.isPendingSync) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Schedule,
                                contentDescription = "Queued",
                                tint = RoseQuartzAccentAmber,
                                modifier = Modifier.size(11.dp)
                            )
                            Text(
                                text = "Queued (awaiting signal)",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = RoseQuartzAccentAmber
                            )
                        }
                    } else {
                        Icon(
                            imageVector = Icons.Filled.DoneAll,
                            contentDescription = "Synced",
                            tint = RoseQuartzPrimary,
                            modifier = Modifier.size(11.dp)
                        )
                    }
                }
            }
        }
    }
}

private fun queryFileInfo(context: Context, uri: Uri): Pair<String, Long> {
    var name = ""
    var size = 0L
    try {
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                if (nameIndex != -1) name = cursor.getString(nameIndex) ?: ""
                if (sizeIndex != -1) size = cursor.getLong(sizeIndex)
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return Pair(name, size)
}

private fun formatFileSize(bytes: Long): String {
    if (bytes <= 0) return "0 KB"
    val kb = bytes / 1024.0
    val mb = kb / 1024.0
    return when {
        mb >= 1.0 -> String.format(Locale.US, "%.1f MB", mb)
        else -> String.format(Locale.US, "%.0f KB", kb.coerceAtLeast(1.0))
    }
}
