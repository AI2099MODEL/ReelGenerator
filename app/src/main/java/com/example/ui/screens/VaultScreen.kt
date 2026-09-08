package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.R
import com.example.data.model.VaultDocumentEntity
import com.example.ui.components.LocalNotificationService
import com.example.ui.components.NotificationType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Harmonized gold styling palette
private val GoldPrimary = Color(0xFFFFD700)
private val GoldHighlight = Color(0xFFFFF4C2)
private val GoldAccent = Color(0xFFC59B27)
private val GoldLight = Color(0xFFF6E7A9)

@Composable
fun VaultScreen(
    vaultDocs: List<VaultDocumentEntity>,
    onAddDocument: (String, String, String, String, String, Long, String) -> Unit,
    onDeleteDocument: (VaultDocumentEntity) -> Unit,
    onHomeClick: (() -> Unit)? = null,
    onMenuClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val notificationService = LocalNotificationService.current

    var showUploadDialog by remember { mutableStateOf(false) }
    var docToDelete by remember { mutableStateOf<VaultDocumentEntity?>(null) }

    // Form states
    var docTitle by remember { mutableStateOf("") }
    var docNotes by remember { mutableStateOf("") }
    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    var selectedFileName by remember { mutableStateOf("") }
    var selectedFileSize by remember { mutableStateOf(0L) }
    var selectedFileType by remember { mutableStateOf("DOCUMENT") }

    val resetForm = {
        docTitle = ""
        docNotes = ""
        selectedUri = null
        selectedFileName = ""
        selectedFileSize = 0L
        selectedFileType = "DOCUMENT"
    }

    // Document file picker (PDF, Doc, text, etc.)
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            if (uri != null) {
                val (name, size) = getFileInfo(context, uri)
                selectedUri = uri
                selectedFileName = name
                selectedFileSize = size
                selectedFileType = detectFileType(context, uri, name)
                if (docTitle.isBlank()) {
                    docTitle = name.substringBeforeLast('.')
                }
            }
        }
    )

    // Gallery photo picker
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if (uri != null) {
                val (name, size) = getFileInfo(context, uri)
                selectedUri = uri
                selectedFileName = name.ifEmpty { "image_${System.currentTimeMillis()}.jpg" }
                selectedFileSize = size
                selectedFileType = "IMAGE"
                if (docTitle.isBlank()) {
                    docTitle = selectedFileName.substringBeforeLast('.')
                }
            }
        }
    )

    val openDocument = { doc: VaultDocumentEntity ->
        try {
            val uri = Uri.parse(doc.uriString)
            val fallbackMime = when (doc.fileType.uppercase(Locale.getDefault())) {
                "PDF" -> "application/pdf"
                "IMAGE", "PNG", "JPG", "JPEG" -> "image/*"
                "TXT", "TEXT" -> "text/plain"
                "DOC", "DOCX" -> "application/msword"
                else -> "*/*"
            }
            val mime = try {
                context.contentResolver.getType(uri) ?: fallbackMime
            } catch (_: Exception) {
                fallbackMime
            }
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, mime)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(Intent.createChooser(intent, "Open Document"))
        } catch (_: Exception) {
            notificationService.show(
                "Document Stored",
                "Saved in local storage: ${doc.originalFileName}",
                NotificationType.INFO
            )
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Transparent)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 10.dp)
        ) {
            // Clean, lightweight top area (no bulky heading or "vault change" header)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.20f))
                            .border(1.dp, GoldPrimary.copy(alpha = 0.6f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.FolderShared,
                            contentDescription = null,
                            tint = GoldHighlight,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Documents",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = GoldHighlight
                        )
                        Text(
                            text = if (vaultDocs.isEmpty()) "No documents stored" else "${vaultDocs.size} document${if (vaultDocs.size == 1) "" else "s"}",
                            fontSize = 11.5.sp,
                            color = GoldLight.copy(alpha = 0.8f)
                        )
                    }
                }

                // Simple "+ Upload Document" button
                Button(
                    onClick = {
                        resetForm()
                        showUploadDialog = true
                    },
                    modifier = Modifier.height(38.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GoldPrimary,
                        contentColor = Color(0xFF241400)
                    ),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 0.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.CloudUpload,
                        contentDescription = "Upload Document",
                        modifier = Modifier.size(17.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Upload",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Document List
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentPadding = PaddingValues(start = 14.dp, end = 14.dp, top = 8.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(vaultDocs, key = { it.id }) { doc ->
                    DocumentCard(
                        doc = doc,
                        onClick = { openDocument(doc) },
                        onDelete = { docToDelete = doc }
                    )
                }
            }
        }

        // ==========================================
        // UI FORM: Upload Document Dialog
        // Crisp, light, high-contrast readable container
        // ==========================================
        if (showUploadDialog) {
            Dialog(
                onDismissRequest = {
                    showUploadDialog = false
                    resetForm()
                },
                properties = DialogProperties(usePlatformDefaultWidth = false)
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth(0.92f)
                        .wrapContentHeight()
                        .padding(vertical = 20.dp),
                    shape = RoundedCornerShape(24.dp),
                    border = BorderStroke(1.5.dp, GoldPrimary.copy(alpha = 0.55f)),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFDF9)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(22.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(15.dp)
                    ) {
                        // Form Title & Close
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(GoldPrimary.copy(alpha = 0.18f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.CloudUpload,
                                        contentDescription = null,
                                        tint = Color(0xFFB45309),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Text(
                                    text = "Upload Document",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    color = Color(0xFF1E293B)
                                )
                            }
                            IconButton(
                                onClick = {
                                    showUploadDialog = false
                                    resetForm()
                                },
                                modifier = Modifier.size(30.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Close,
                                    contentDescription = "Close",
                                    tint = Color(0xFF64748B),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        HorizontalDivider(color = Color(0xFFE2E8F0), thickness = 1.dp)

                        // File selection affordances (File/PDF, Camera, Gallery)
                        Text(
                            text = "Select File / Source:",
                            color = Color(0xFF334155),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Files / PDF Picker
                            Button(
                                onClick = {
                                    try {
                                        filePickerLauncher.launch("*/*")
                                    } catch (e: Exception) {
                                        notificationService.show(
                                            "File Picker Unavailable",
                                            "Could not open file picker: ${e.localizedMessage ?: "No app available"}",
                                            NotificationType.ALERT
                                        )
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFF8F5EE),
                                    contentColor = Color(0xFF1E293B)
                                ),
                                border = BorderStroke(1.dp, Color(0xFFD4A359).copy(alpha = 0.55f)),
                                contentPadding = PaddingValues(horizontal = 10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.InsertDriveFile,
                                    contentDescription = "Files",
                                    tint = Color(0xFFB45309),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "Files / PDF",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1E293B)
                                )
                            }

                            // Gallery
                            Button(
                                onClick = {
                                    try {
                                        photoPickerLauncher.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    } catch (e: Exception) {
                                        try {
                                            filePickerLauncher.launch("image/*")
                                        } catch (e2: Exception) {
                                            notificationService.show(
                                                "Gallery Unavailable",
                                                "Could not open photo gallery: ${e2.localizedMessage ?: "No app available"}",
                                                NotificationType.ALERT
                                            )
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFF8F5EE),
                                    contentColor = Color(0xFF1E293B)
                                ),
                                border = BorderStroke(1.dp, Color(0xFFD4A359).copy(alpha = 0.55f)),
                                contentPadding = PaddingValues(horizontal = 10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.PhotoLibrary,
                                    contentDescription = "Gallery",
                                    tint = Color(0xFFB45309),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "Photos / Gallery",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1E293B)
                                )
                            }
                        }

                        // Selected file badge
                        if (selectedFileName.isNotBlank()) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFFF0FDF4),
                                border = BorderStroke(1.dp, Color(0xFF86EFAC)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.CheckCircle,
                                            contentDescription = null,
                                            tint = Color(0xFF16A34A),
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(
                                                text = selectedFileName,
                                                fontSize = 12.sp,
                                                color = Color(0xFF14532D),
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            if (selectedFileSize > 0) {
                                                Text(
                                                    text = formatFileSize(selectedFileSize),
                                                    fontSize = 10.sp,
                                                    color = Color(0xFF166534)
                                                )
                                            }
                                        }
                                    }

                                    Text(
                                        text = selectedFileType,
                                        color = Color(0xFF14532D),
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Black,
                                        modifier = Modifier
                                            .background(
                                                Color(0xFFDCFCE7),
                                                RoundedCornerShape(4.dp)
                                            )
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        // Document Title Input (High contrast, clearly visible font and lighter field)
                        OutlinedTextField(
                            value = docTitle,
                            onValueChange = { docTitle = it },
                            label = {
                                Text(
                                    "Document Name *",
                                    color = Color(0xFF334155),
                                    fontWeight = FontWeight.SemiBold
                                )
                            },
                            placeholder = {
                                Text(
                                    "Enter document name",
                                    color = Color(0xFF94A3B8)
                                )
                            },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color(0xFF0F172A),
                                unfocusedTextColor = Color(0xFF1E293B),
                                focusedBorderColor = Color(0xFFD97706),
                                unfocusedBorderColor = Color(0xFFCBD5E1),
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color(0xFFFAF9F6),
                                cursorColor = Color(0xFFB45309)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )

                        // Notes / Description Input (Optional, high contrast visible font)
                        OutlinedTextField(
                            value = docNotes,
                            onValueChange = { docNotes = it },
                            label = {
                                Text(
                                    "Notes / Description (Optional)",
                                    color = Color(0xFF475569),
                                    fontWeight = FontWeight.Medium
                                )
                            },
                            placeholder = {
                                Text(
                                    "Add any notes about this document",
                                    color = Color(0xFF94A3B8)
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 72.dp),
                            maxLines = 4,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color(0xFF0F172A),
                                unfocusedTextColor = Color(0xFF1E293B),
                                focusedBorderColor = Color(0xFFD97706),
                                unfocusedBorderColor = Color(0xFFCBD5E1),
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color(0xFFFAF9F6),
                                cursorColor = Color(0xFFB45309)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )

                        // Form Actions
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(
                                onClick = {
                                    showUploadDialog = false
                                    resetForm()
                                }
                            ) {
                                Text("Cancel", color = Color(0xFF475569), fontWeight = FontWeight.SemiBold)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    if (docTitle.isNotBlank()) {
                                        val cleanTitle = docTitle.trim()
                                        val cleanFile = if (selectedFileName.isNotBlank()) {
                                            selectedFileName
                                        } else {
                                            "${cleanTitle.replace(" ", "_")}.${selectedFileType.lowercase()}"
                                        }
                                        val uriStr = selectedUri?.toString() ?: "file://documents/$cleanFile"

                                        onAddDocument(
                                            cleanTitle,
                                            cleanFile,
                                            uriStr,
                                            selectedFileType,
                                            "Document",
                                            selectedFileSize,
                                            docNotes.trim()
                                        )
                                        notificationService.show(
                                            "Upload Successful",
                                            "Saved \"$cleanTitle\"",
                                            NotificationType.SUCCESS
                                        )
                                        showUploadDialog = false
                                        resetForm()
                                    }
                                },
                                enabled = docTitle.isNotBlank(),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = GoldPrimary,
                                    contentColor = Color(0xFF1F1604),
                                    disabledContainerColor = Color(0xFFE2E8F0),
                                    disabledContentColor = Color(0xFF94A3B8)
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Upload Document", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Delete Confirmation Dialog
        if (docToDelete != null) {
            val doc = docToDelete!!
            Dialog(
                onDismissRequest = { docToDelete = null },
                properties = DialogProperties(usePlatformDefaultWidth = false)
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth(0.88f)
                        .wrapContentHeight(),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.5.dp, GoldPrimary.copy(alpha = 0.5f)),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFDF9)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Delete Document?",
                            color = Color(0xFF1E293B),
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Are you sure you want to remove \"${doc.title}\"? It will be backed up in My Organiser safety archive.",
                            color = Color(0xFF475569),
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(onClick = { docToDelete = null }) {
                                Text("Cancel", color = Color(0xFF64748B), fontWeight = FontWeight.SemiBold)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    onDeleteDocument(doc)
                                    notificationService.show(
                                        "Deleted",
                                        "Removed ${doc.title}",
                                        NotificationType.INFO
                                    )
                                    docToDelete = null
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFDC2626),
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Delete", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Clean translucent frosted glass card for documents.
 * Shows the background image through rather than an opaque black background!
 */
@Composable
private fun DocumentCard(
    doc: VaultDocumentEntity,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = Color.White.copy(alpha = 0.16f), // Translucent glass, background image shows through!
        border = BorderStroke(1.dp, GoldAccent.copy(alpha = 0.45f)),
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Document Type Icon
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White.copy(alpha = 0.18f))
                    .border(1.dp, GoldPrimary.copy(alpha = 0.4f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                val icon = when (doc.fileType.uppercase(Locale.getDefault())) {
                    "PDF" -> Icons.Filled.PictureAsPdf
                    "IMAGE", "PNG", "JPG", "JPEG" -> Icons.Filled.Image
                    "TXT", "TEXT" -> Icons.Filled.Article
                    else -> Icons.Filled.Description
                }
                val iconTint = when (doc.fileType.uppercase(Locale.getDefault())) {
                    "PDF" -> Color(0xFFFF7070)
                    "IMAGE", "PNG", "JPG", "JPEG" -> Color(0xFF64B5F6)
                    else -> GoldHighlight
                }
                Icon(
                    imageVector = icon,
                    contentDescription = doc.fileType,
                    tint = iconTint,
                    modifier = Modifier.size(24.dp)
                )
            }

            // Info column
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    text = doc.title,
                    color = GoldHighlight,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = doc.originalFileName,
                        color = GoldLight.copy(alpha = 0.8f),
                        fontSize = 11.5.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (doc.fileSizeBytes > 0) {
                        Text(
                            text = "•",
                            color = GoldLight.copy(alpha = 0.5f),
                            fontSize = 10.sp
                        )
                        Text(
                            text = formatFileSize(doc.fileSizeBytes),
                            color = GoldLight.copy(alpha = 0.8f),
                            fontSize = 11.sp
                        )
                    }
                }

                if (doc.notes.isNotBlank()) {
                    Text(
                        text = doc.notes,
                        color = GoldLight.copy(alpha = 0.7f),
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Text(
                    text = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(doc.dateAddedTimestamp)),
                    color = GoldLight.copy(alpha = 0.55f),
                    fontSize = 10.sp
                )
            }

            // Delete action button
            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.DeleteOutline,
                    contentDescription = "Delete",
                    tint = Color(0xFFFF8080).copy(alpha = 0.85f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

// Helpers
private fun getFileInfo(context: Context, uri: Uri): Pair<String, Long> {
    var displayName = "Document_${System.currentTimeMillis()}"
    var size = 0L
    try {
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
            if (cursor.moveToFirst()) {
                if (nameIndex != -1) {
                    cursor.getString(nameIndex)?.let { displayName = it }
                }
                if (sizeIndex != -1) {
                    size = cursor.getLong(sizeIndex)
                }
            }
        }
    } catch (_: Exception) {
        uri.lastPathSegment?.let { seg ->
            displayName = seg.substringAfterLast('/')
        }
    }
    return Pair(displayName, size)
}

private fun formatFileSize(bytes: Long): String {
    if (bytes <= 0) return ""
    return when {
        bytes >= 1024 * 1024 -> String.format(Locale.getDefault(), "%.1f MB", bytes / (1024f * 1024f))
        bytes >= 1024 -> String.format(Locale.getDefault(), "%.1f KB", bytes / 1024f)
        else -> "$bytes B"
    }
}

private fun detectFileType(context: Context, uri: Uri, fileName: String): String {
    val ext = fileName.substringAfterLast('.', "").uppercase(Locale.getDefault())
    if (ext.isNotBlank() && ext.length <= 5) return ext
    val mime = try {
        context.contentResolver.getType(uri)
    } catch (_: Exception) {
        null
    }
    return when {
        mime?.contains("pdf", ignoreCase = true) == true -> "PDF"
        mime?.contains("image", ignoreCase = true) == true -> "IMAGE"
        mime?.contains("text", ignoreCase = true) == true -> "TXT"
        mime?.contains("word", ignoreCase = true) == true -> "DOC"
        else -> "FILE"
    }
}
