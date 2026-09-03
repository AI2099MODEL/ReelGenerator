const fs = require('fs');

const content = `package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.DiaryEntryEntity
import com.example.ui.components.LedgerEmptyState
import com.example.ui.components.showNativeDatePicker
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.sin

// Living Water Palette
val LWParchment = Color(0xFFFFF8EF)
val LWParchmentDark = Color(0xFFF5EDDE)
val LWNavy = Color(0xFF1B263B)
val LWSurfaceVariant = Color(0xFF45474D)
val LWOutline = Color(0xFFC5C6CD)
val LWHover = Color(0xFFE9E2D3)

@Composable
fun DiaryScreen(
    entries: List<DiaryEntryEntity>,
    onAddEntry: (title: String, body: String, moodOrTag: String, isPinned: Boolean, timestamp: Long) -> Unit,
    onDeleteEntry: (DiaryEntryEntity) -> Unit,
    onUpdateEntry: (DiaryEntryEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedTagFilter by remember { mutableStateOf("All") }
    var showAddDialog by remember { mutableStateOf(false) }
    var editingEntry by remember { mutableStateOf<DiaryEntryEntity?>(null) }
    var viewingEntry by remember { mutableStateOf<DiaryEntryEntity?>(null) }

    val tags = listOf("All", "Reflection", "Milestone", "Deep Thought", "Gratitude", "Ideas", "Daily")

    val filteredEntries = remember(entries, searchQuery, selectedTagFilter) {
        entries.filter { entry ->
            val matchesTag = selectedTagFilter == "All" || entry.moodOrTag.equals(selectedTagFilter, ignoreCase = true)
            val matchesQuery = searchQuery.isBlank() ||
                    entry.title.contains(searchQuery, ignoreCase = true) ||
                    entry.body.contains(searchQuery, ignoreCase = true)
            matchesTag && matchesQuery
        }.sortedByDescending { it.dateTimestamp }.let { list ->
            // Pins first
            list.filter { it.isPinned } + list.filter { !it.isPinned }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(LWParchment)
    ) {
        LivingWaterBackground()
        
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(LWParchment.copy(alpha = 0.6f))
                    .border(BorderStroke(1.dp, LWOutline.copy(alpha = 0.3f)))
                    .padding(horizontal = 16.dp, vertical = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { /* TODO */ }) {
                    Icon(Icons.Outlined.Menu, contentDescription = "Menu", tint = LWNavy)
                }
                Text(
                    text = "Ledger Diary",
                    fontFamily = FontFamily.Serif,
                    fontStyle = FontStyle.Italic,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    color = LWNavy
                )
                IconButton(onClick = { showAddDialog = true }) {
                    Icon(Icons.Outlined.HistoryEdu, contentDescription = "Add Entry", tint = LWNavy)
                }
            }

            // Main Content Scroll
            LazyColumn(
                modifier = Modifier.fillMaxWidth().weight(1f),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                item {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Text(
                            text = "Chronicles",
                            fontFamily = FontFamily.Serif,
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Bold,
                            color = LWNavy,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Text(
                            text = "A meticulous record of thoughts, observations, and milestones, preserved for posteriority.",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 14.sp,
                            color = LWSurfaceVariant,
                            lineHeight = 20.sp
                        )
                    }
                }
                
                // Filters
                item {
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(horizontal = 24.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(tags) { tag ->
                            val isSelected = selectedTagFilter == tag
                            Surface(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { selectedTagFilter = tag },
                                color = if (isSelected) LWNavy else Color.Transparent,
                                border = if (isSelected) null else BorderStroke(1.dp, LWOutline)
                            ) {
                                Text(
                                    text = tag,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
                                    color = if (isSelected) Color.White else LWSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }

                if (filteredEntries.isEmpty()) {
                    item {
                        LedgerEmptyState(
                            icon = Icons.Outlined.MenuBook,
                            title = "The Ledger is Empty",
                            subtitle = "Pen your first thought.",
                            actionLabel = "New Entry",
                            onAction = { showAddDialog = true }
                        )
                    }
                } else {
                    items(filteredEntries, key = { it.id }) { entry ->
                        // Determine if we should feature this entry (e.g. pinned or first)
                        val isFeatured = filteredEntries.indexOf(entry) == 0
                        
                        DiaryEntryGlassCard(
                            entry = entry,
                            isFeatured = isFeatured,
                            onClick = { viewingEntry = entry }
                        )
                    }
                }
            }
        }
    }

    // Add Entry Dialog
    if (showAddDialog) {
        DiaryEntryEditorDialog(
            entry = null,
            onDismiss = { showAddDialog = false },
            onSave = { title, body, tag, isPinned, timestamp ->
                onAddEntry(title, body, tag, isPinned, timestamp)
                showAddDialog = false
            }
        )
    }

    // Edit Entry Dialog
    editingEntry?.let { entry ->
        DiaryEntryEditorDialog(
            entry = entry,
            onDismiss = { editingEntry = null },
            onSave = { title, body, tag, isPinned, timestamp ->
                onUpdateEntry(entry.copy(title = title, body = body, moodOrTag = tag, isPinned = isPinned, dateTimestamp = timestamp))
                editingEntry = null
            }
        )
    }

    // View Entry Dialog
    viewingEntry?.let { entry ->
        DiaryEntryViewDialog(
            entry = entry,
            onDismiss = { viewingEntry = null },
            onEdit = { 
                viewingEntry = null
                editingEntry = entry
            },
            onDelete = { 
                onDeleteEntry(entry)
                viewingEntry = null
            }
        )
    }
}

@Composable
fun LivingWaterBackground() {
    val infiniteTransition = rememberInfiniteTransition()
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 100f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "time"
    )

    androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        
        // Simulating the WebGL ripple effect with multiple gradient circles drifting
        val c1 = Offset(w * (0.5f + 0.3f * sin(time * 0.1f)), h * (0.5f + 0.2f * sin(time * 0.13f)))
        val c2 = Offset(w * (0.2f + 0.4f * sin(time * 0.15f)), h * (0.8f + 0.3f * sin(time * 0.08f)))
        
        drawRect(color = LWParchment)
        
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(LWParchmentDark.copy(alpha = 0.6f), Color.Transparent),
                center = c1,
                radius = w * 0.8f
            ),
            center = c1,
            radius = w * 0.8f
        )
        
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFFE9E2D3).copy(alpha = 0.5f), Color.Transparent),
                center = c2,
                radius = w * 0.9f
            ),
            center = c2,
            radius = w * 0.9f
        )
    }
}

@Composable
fun DiaryEntryGlassCard(
    entry: DiaryEntryEntity,
    isFeatured: Boolean,
    onClick: () -> Unit
) {
    val dateFmt = remember { SimpleDateFormat("MMM d, yyyy", Locale.getDefault()) }
    val formattedDate = remember(entry.dateTimestamp) { dateFmt.format(Date(entry.dateTimestamp)) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
    ) {
        if (isFeatured) {
            Image(
                painter = painterResource(id = R.drawable.bg_fern),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize(),
                alpha = 0.15f
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(LWParchment.copy(alpha = 0.6f))
                .border(BorderStroke(1.dp, LWOutline.copy(alpha = 0.3f)), RoundedCornerShape(16.dp))
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Surface(
                    color = LWParchment.copy(alpha = 0.8f),
                    border = BorderStroke(1.dp, LWOutline.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = entry.moodOrTag.uppercase(),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        color = LWSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        Icons.Outlined.CalendarToday,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = LWSurfaceVariant
                    )
                    Text(
                        text = formattedDate,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        color = LWSurfaceVariant
                    )
                }
            }

            Text(
                text = entry.title,
                fontFamily = FontFamily.Serif,
                fontSize = if (isFeatured) 24.sp else 20.sp,
                fontWeight = FontWeight.Bold,
                color = LWNavy,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = entry.body,
                fontFamily = FontFamily.SansSerif,
                fontSize = 14.sp,
                color = LWSurfaceVariant,
                lineHeight = 20.sp,
                maxLines = if (isFeatured) 4 else 2,
                overflow = TextOverflow.Ellipsis
            )
            
            if (entry.isPinned) {
                Icon(
                    Icons.Default.PushPin,
                    contentDescription = "Pinned",
                    modifier = Modifier.size(16.dp).align(Alignment.End),
                    tint = LWNavy
                )
            }
        }
    }
}

// Keeping Dialogs almost identical but styled with Living Water colors where applicable

@Composable
private fun DiaryEntryEditorDialog(
    entry: DiaryEntryEntity?,
    onDismiss: () -> Unit,
    onSave: (String, String, String, Boolean, Long) -> Unit
) {
    val context = LocalContext.current
    var title by remember { mutableStateOf(entry?.title ?: "") }
    var body by remember { mutableStateOf(entry?.body ?: "") }
    var tag by remember { mutableStateOf(entry?.moodOrTag ?: "Reflection") }
    var isPinned by remember { mutableStateOf(entry?.isPinned ?: false) }
    var timestamp by remember { mutableStateOf(entry?.dateTimestamp ?: System.currentTimeMillis()) }

    val dateFmt = remember { SimpleDateFormat("MMM d, yyyy", Locale.getDefault()) }
    val tagOptions = listOf("Reflection", "Milestone", "Deep Thought", "Gratitude", "Ideas", "Daily", "Plans")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (entry == null) "New Entry" else "Edit Entry",
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold,
                color = LWNavy
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = dateFmt.format(Date(timestamp)),
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        color = LWSurfaceVariant
                    )
                    TextButton(
                        onClick = {
                            val cal = Calendar.getInstance().apply { timeInMillis = timestamp }
                            showNativeDatePicker(context, cal) { year, month, day ->
                                val newCal = Calendar.getInstance().apply {
                                    set(Calendar.YEAR, year)
                                    set(Calendar.MONTH, month)
                                    set(Calendar.DAY_OF_MONTH, day)
                                }
                                timestamp = newCal.timeInMillis
                            }
                        }
                    ) {
                        Text("Change", fontSize = 12.sp, color = LWNavy)
                    }
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title", fontFamily = FontFamily.Serif) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = body,
                    onValueChange = { body = it },
                    label = { Text("Body") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    maxLines = 10
                )

                Text(
                    text = "Category:",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    color = LWSurfaceVariant
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(tagOptions) { opt ->
                        val isSel = tag == opt
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { tag = opt },
                            color = if (isSel) LWNavy else Color.Transparent,
                            border = if (!isSel) BorderStroke(1.dp, LWOutline) else null
                        ) {
                            Text(
                                text = opt,
                                fontSize = 10.sp,
                                color = if (isSel) Color.White else LWSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Checkbox(
                        checked = isPinned,
                        onCheckedChange = { isPinned = it },
                        colors = CheckboxDefaults.colors(checkedColor = LWNavy)
                    )
                    Text("Pin this entry", fontSize = 13.sp, color = LWSurfaceVariant)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank() || body.isNotBlank()) {
                        onSave(title.ifBlank { "Untitled" }, body, tag, isPinned, timestamp)
                    }
                },
                enabled = title.isNotBlank() || body.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = LWNavy)
            ) {
                Text("Save", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = LWSurfaceVariant)
            }
        }
    )
}

@Composable
private fun DiaryEntryViewDialog(
    entry: DiaryEntryEntity,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val dateFmt = remember { SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault()) }
    val formattedDate = remember(entry.dateTimestamp) { dateFmt.format(Date(entry.dateTimestamp)) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = entry.title,
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    color = LWNavy
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = formattedDate,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    color = LWSurfaceVariant
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = LWParchmentDark,
                    border = BorderStroke(1.dp, LWOutline.copy(alpha = 0.5f))
                ) {
                    Text(
                        text = entry.moodOrTag.uppercase(),
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = LWSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = entry.body,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        lineHeight = 22.sp,
                        color = LWSurfaceVariant
                    )
                )
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onEdit) {
                    Text("Edit", color = LWNavy)
                }
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = LWNavy)
                ) {
                    Text("Done", color = Color.White)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDelete) {
                Text("Delete", color = MaterialTheme.colorScheme.error)
            }
        }
    )
}
`
fs.writeFileSync('app/src/main/java/com/example/ui/screens/DiaryScreen.kt', content);
