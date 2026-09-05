package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import coil.compose.AsyncImage
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DiaryEntryEntity
import com.example.ui.components.AmbientWaterFlowBackground
import com.example.ui.components.LedgerEmptyState
import com.example.ui.components.LedgerPaperCard
import com.example.ui.components.LedgerTopHeader
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DiaryScreen(
    entries: List<DiaryEntryEntity>,
    onAddEntry: (String, String, String, Boolean, Boolean, Long, String?) -> Unit,
    onDeleteEntry: (DiaryEntryEntity) -> Unit,
    onUpdateEntry: (DiaryEntryEntity) -> Unit,
    modifier: Modifier = Modifier,
    onMenuClick: (() -> Unit)? = null
) {
    var selectedCategory by remember { mutableStateOf("All") }
    var searchQuery by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }
    var viewingEntry by remember { mutableStateOf<DiaryEntryEntity?>(null) }
    var editingEntry by remember { mutableStateOf<DiaryEntryEntity?>(null) }

    val filteredEntries = remember(entries, searchQuery) {
        entries.filter { entry ->
            val matchesQuery = searchQuery.isBlank() ||
                    entry.title.contains(searchQuery, ignoreCase = true) ||
                    entry.body.contains(searchQuery, ignoreCase = true)
            matchesQuery
        }.sortedWith(compareByDescending<DiaryEntryEntity> { it.isPinned }.thenByDescending { it.dateTimestamp })
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Transparent)
    ) {
        // AmbientWaterFlowBackground removed

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Transparent)
        ) {
            LedgerTopHeader(
                title = "Notes",
                onMenuClick = null
            )

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search notes", color = RoseQuartzTextMuted, fontSize = 13.sp) },
            leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null, tint = RoseQuartzSteel) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Filled.Clear, contentDescription = "Clear", tint = RoseQuartzTextMuted)
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = RoseQuartzPrimary,
                unfocusedBorderColor = RoseQuartzContainerHighest,
                focusedTextColor = RoseQuartzTextPrimary,
                unfocusedTextColor = RoseQuartzTextPrimary,
                focusedContainerColor = RoseQuartzContainerLowest.copy(alpha = 0.8f),
                unfocusedContainerColor = RoseQuartzContainerLowest.copy(alpha = 0.8f)
            ),
            singleLine = true
        )

        // Chronicle List
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            if (filteredEntries.isEmpty()) {
                LedgerEmptyState(
                    icon = Icons.AutoMirrored.Outlined.MenuBook,
                    title = if (searchQuery.isNotBlank()) "No Matching Reflections" else "No Reflections Recorded",
                    description = if (searchQuery.isNotBlank()) "Try refining your search keyword." else "Preserve insights, philosophical milestones, and personal moments.",
                    actionLabel = "Record First Reflection",
                    onActionClick = { showAddDialog = true }
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(top = 6.dp, bottom = 80.dp)
                ) {
                    items(filteredEntries, key = { it.id }) { entry ->
                        Box(modifier = Modifier.fillMaxWidth()) {
                            androidx.compose.animation.AnimatedVisibility(
                                visible = true,
                                enter = fadeIn(animationSpec = tween(350)) + slideInVertically(animationSpec = tween(350), initialOffsetY = { it / 3 })
                            ) {
                                DiaryEntryCard(
                                    entry = entry,
                                    onClick = { viewingEntry = entry },
                                    onEdit = { editingEntry = entry },
                                    onDelete = { onDeleteEntry(entry) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

        // Floating Action Button (+) at lower right - Small Red Button
        FloatingActionButton(
            onClick = { showAddDialog = true },
            containerColor = Color(0xFFD81B60),
            contentColor = Color.White,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 16.dp, end = 16.dp)
                .size(40.dp)
                .shadow(6.dp, CircleShape, ambientColor = Color(0xFFD81B60), spotColor = Color(0xFFD81B60))
        ) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = "New Entry",
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }
    }

    // Add Reflection Dialog
    if (showAddDialog) {
        DiaryEditorDialog(
            title = "New Chronicle Entry",
            initialCategory = if (selectedCategory == "All") "Reflection" else selectedCategory,
            onDismiss = { showAddDialog = false },
            onSave = { title, content, category, isPinned, notifyMe, imageUri ->
                onAddEntry(title, content, category, isPinned, notifyMe, System.currentTimeMillis(), imageUri)
                showAddDialog = false
            }
        )
    }

    // Viewing Dialog
    viewingEntry?.let { entry ->
        DiaryViewDialog(
            entry = entry,
            onDismiss = { viewingEntry = null },
            onEdit = {
                editingEntry = entry
                viewingEntry = null
            },
            onDelete = {
                onDeleteEntry(entry)
                viewingEntry = null
            }
        )
    }

    // Edit Dialog
    editingEntry?.let { entry ->
        DiaryEditorDialog(
            title = "Edit Chronicle Entry",
            initialTitle = entry.title,
            initialContent = entry.body,
            initialCategory = entry.moodOrTag,
            initialPinned = entry.isPinned,
            initialNotify = entry.notifyMe,
            initialImageUri = entry.imageUri,
            onDismiss = { editingEntry = null },
            onSave = { title, content, category, isPinned, notifyMe, imageUri ->
                onUpdateEntry(entry.copy(
                    title = title,
                    body = content,
                    moodOrTag = category,
                    isPinned = isPinned,
                    notifyMe = notifyMe,
                    imageUri = imageUri
                ))
                editingEntry = null
            }
        )
    }
}

@Composable
private fun DiaryEntryCard(
    entry: DiaryEntryEntity,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val monthFormatter = remember { SimpleDateFormat("MMM", Locale.getDefault()) }
    val dayFormatter = remember { SimpleDateFormat("dd", Locale.getDefault()) }
    val dateFormatter = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }

    val month = remember(entry.dateTimestamp) { monthFormatter.format(Date(entry.dateTimestamp)).uppercase() }
    val day = remember(entry.dateTimestamp) { dayFormatter.format(Date(entry.dateTimestamp)) }
    val formattedDate = remember(entry.dateTimestamp) { dateFormatter.format(Date(entry.dateTimestamp)) }

    LedgerPaperCard(
        modifier = Modifier.clickable { onClick() },
        borderColor = if (entry.isPinned) RoseQuartzPrimary else RoseQuartzContainerHighest
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Rose Quartz Calendar Date Block (Score Card Style)
            Surface(
                modifier = Modifier
                    .width(52.dp)
                    .height(56.dp),
                shape = RoundedCornerShape(10.dp),
                color = RoseQuartzPrimaryContainer,
                border = BorderStroke(1.dp, RoseQuartzPrimary.copy(alpha = 0.4f))
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(text = month, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = RoseQuartzPrimary)
                    Text(text = day, fontFamily = FontFamily.Serif, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = RoseQuartzPrimary)
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = entry.title,
                        fontFamily = FontFamily.Serif,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = RoseQuartzTextPrimary,
                        letterSpacing = 0.2.sp,
                        modifier = Modifier.weight(1f)
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        IconButton(
                            onClick = onEdit,
                            modifier = Modifier.size(30.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Edit,
                                contentDescription = "Edit Note",
                                tint = RoseQuartzPrimary,
                                modifier = Modifier.size(17.dp)
                            )
                        }
                        IconButton(
                            onClick = onDelete,
                            modifier = Modifier.size(30.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Delete,
                                contentDescription = "Delete Note",
                                tint = RoseQuartzError.copy(alpha = 0.75f),
                                modifier = Modifier.size(17.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = entry.body,
                    fontSize = 13.sp,
                    color = RoseQuartzTextSecondary,
                    lineHeight = 19.sp,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )

                if (!entry.imageUri.isNullOrEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    AsyncImage(
                        model = entry.imageUri,
                        contentDescription = "Attached note image",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Subtle divider line
                HorizontalDivider(
                    color = RoseQuartzContainerHighest,
                    thickness = 0.8.dp
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = RoseQuartzPrimaryContainer,
                        border = BorderStroke(1.dp, RoseQuartzPrimary.copy(alpha = 0.4f))
                    ) {
                        Text(
                            text = entry.moodOrTag.uppercase(),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = RoseQuartzPrimary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            letterSpacing = 0.5.sp
                        )
                    }

                    Text(
                        text = formattedDate.uppercase(),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = RoseQuartzTextMuted,
                        letterSpacing = 0.3.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun DiaryEditorDialog(
    title: String,
    initialTitle: String = "",
    initialContent: String = "",
    initialCategory: String = "Reflection",
    initialPinned: Boolean = false,
    initialNotify: Boolean = false,
    initialImageUri: String? = null,
    onDismiss: () -> Unit,
    onSave: (String, String, String, Boolean, Boolean, String?) -> Unit
) {
    var entryTitle by remember { mutableStateOf(initialTitle) }
    var entryContent by remember { mutableStateOf(initialContent) }
    var entryCategory by remember { mutableStateOf(initialCategory) }
    var isPinned by remember { mutableStateOf(initialPinned) }
    var notifyMe by remember { mutableStateOf(initialNotify) }
    var imageUri by remember { mutableStateOf(initialImageUri) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            imageUri = uri.toString()
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = RoseQuartzContainerLowest,
        shape = RoundedCornerShape(16.dp),
        title = {
            Text(
                text = title,
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold,
                color = RoseQuartzTextPrimary
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = entryTitle,
                    onValueChange = { entryTitle = it },
                    label = { Text("Reflection Title") },
                    placeholder = { Text("e.g. Rose Quartz Insights") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = RoseQuartzPrimary,
                        unfocusedBorderColor = RoseQuartzContainerHighest,
                        focusedTextColor = RoseQuartzTextPrimary,
                        unfocusedTextColor = RoseQuartzTextPrimary
                    )
                )

                OutlinedTextField(
                    value = entryCategory,
                    onValueChange = { entryCategory = it },
                    label = { Text("Category / Tag") },
                    placeholder = { Text("e.g. Reflection, Personal, Ideas") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = RoseQuartzPrimary,
                        unfocusedBorderColor = RoseQuartzContainerHighest,
                        focusedTextColor = RoseQuartzTextPrimary,
                        unfocusedTextColor = RoseQuartzTextPrimary
                    )
                )

                OutlinedTextField(
                    value = entryContent,
                    onValueChange = { entryContent = it },
                    label = { Text("Reflection Body") },
                    placeholder = { Text("Write your thoughts...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = RoseQuartzPrimary,
                        unfocusedBorderColor = RoseQuartzContainerHighest,
                        focusedTextColor = RoseQuartzTextPrimary,
                        unfocusedTextColor = RoseQuartzTextPrimary
                    )
                )

                // Image picker & preview
                OutlinedButton(
                    onClick = { imagePickerLauncher.launch("image/*") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = RoseQuartzPrimary),
                    border = BorderStroke(1.dp, RoseQuartzContainerHighest)
                ) {
                    Icon(Icons.Outlined.Image, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (imageUri == null) "Attach Image" else "Change Image")
                }

                if (!imageUri.isNullOrEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(RoseQuartzContainerLow)
                    ) {
                        AsyncImage(
                            model = imageUri,
                            contentDescription = "Attached image preview",
                            modifier = Modifier.fillMaxSize()
                        )
                        IconButton(
                            onClick = { imageUri = null },
                            modifier = Modifier.align(Alignment.TopEnd)
                        ) {
                            Icon(Icons.Filled.Clear, contentDescription = "Remove image", tint = Color.Red)
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Pin Reflection", fontSize = 13.sp, color = RoseQuartzTextPrimary)
                    Switch(
                        checked = isPinned,
                        onCheckedChange = { isPinned = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = RoseQuartzOnPrimary, checkedTrackColor = RoseQuartzPrimary)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (entryTitle.isNotBlank()) {
                        onSave(entryTitle.trim(), entryContent.trim(), entryCategory, isPinned, notifyMe, imageUri)
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = RoseQuartzPrimary,
                    contentColor = RoseQuartzOnPrimary
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Save Chronicle", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = RoseQuartzTextSecondary)
            }
        }
    )
}

@Composable
private fun DiaryViewDialog(
    entry: DiaryEntryEntity,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val dateFormatter = remember { SimpleDateFormat("MMMM dd, yyyy • hh:mm a", Locale.getDefault()) }
    val formattedDate = remember(entry.dateTimestamp) { dateFormatter.format(Date(entry.dateTimestamp)) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = RoseQuartzContainerLowest,
        shape = RoundedCornerShape(16.dp),
        title = {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = RoseQuartzPrimaryContainer,
                        border = BorderStroke(1.dp, RoseQuartzPrimary.copy(alpha = 0.4f))
                    ) {
                        Text(
                            text = entry.moodOrTag.uppercase(),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = RoseQuartzPrimary,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Text(text = formattedDate, fontSize = 11.sp, color = RoseQuartzTextMuted)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = entry.title,
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = RoseQuartzTextPrimary
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (!entry.imageUri.isNullOrEmpty()) {
                    AsyncImage(
                        model = entry.imageUri,
                        contentDescription = "Attached note image",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )
                }
                Text(
                    text = entry.body,
                    fontSize = 14.sp,
                    color = RoseQuartzTextSecondary,
                    lineHeight = 22.sp
                )
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = onDelete,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = RoseQuartzError),
                    border = BorderStroke(1.dp, RoseQuartzError.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Delete")
                }
                Button(
                    onClick = onEdit,
                    colors = ButtonDefaults.buttonColors(containerColor = RoseQuartzPrimary, contentColor = RoseQuartzOnPrimary),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Edit")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = RoseQuartzTextSecondary)
            }
        }
    )
}
