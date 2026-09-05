package com.example.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.EventEntity
import com.example.ui.components.LedgerTopHeader
import java.text.SimpleDateFormat
import java.util.*

enum class DateScreenMode(
    val title: String,
    val subtitle: String,
    val emptyTitle: String,
    val emptySubtitle: String,
    val addButtonText: String,
    val defaultEventType: String
) {
    IMPORTANT_DATES(
        title = "Important Dates",
        subtitle = "Birthdays & Anniversaries",
        emptyTitle = "No Important Dates Yet",
        emptySubtitle = "Keep track of upcoming birthdays, anniversaries, and milestones.",
        addButtonText = "Add Date",
        defaultEventType = "IMPORTANT_DATE"
    ),
    REMIND_ME(
        title = "Remind Me",
        subtitle = "Coming Tasks & Daily Reminders",
        emptyTitle = "No Reminders Yet",
        emptySubtitle = "Never miss coming tasks, daily duties, or personal reminders.",
        addButtonText = "Add Task",
        defaultEventType = "REMIND_ME"
    )
}

data class CategoryOption(
    val title: String,
    val icon: ImageVector,
    val color: Color
)

/**
 * Clean, modern screen with the app background showing through,
 * + button to add, identical rich UI for adding, and tailored for:
 * - Important Dates (birthdays & anniversaries)
 * - Remind Me (coming tasks & daily tasks)
 */
@Composable
fun RemindMeDatesScreen(
    mode: DateScreenMode,
    events: List<EventEntity>,
    onAddEvent: (
        title: String,
        locationOrNote: String,
        notify: Boolean,
        includeYear: Boolean,
        eventTimestamp: Long,
        category: String,
        eventType: String
    ) -> Unit,
    onDeleteEvent: (EventEntity) -> Unit,
    onToggleComplete: ((EventEntity) -> Unit)? = null,
    onHomeClick: (() -> Unit)? = null,
    onMenuClick: (() -> Unit)? = null
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var eventToDelete by remember { mutableStateOf<EventEntity?>(null) }

    // Filter items based on mode
    val displayItems = remember(events, mode) {
        when (mode) {
            DateScreenMode.IMPORTANT_DATES -> {
                events.filter {
                    it.eventType == "IMPORTANT_DATE" ||
                    it.category.equals("Birthday", ignoreCase = true) ||
                    it.category.equals("Anniversary", ignoreCase = true)
                }.sortedBy { it.eventTimestamp }
            }
            DateScreenMode.REMIND_ME -> {
                events.filter {
                    it.eventType == "REMIND_ME" ||
                    (!it.category.equals("Birthday", ignoreCase = true) &&
                     !it.category.equals("Anniversary", ignoreCase = true) &&
                     it.eventType != "IMPORTANT_DATE")
                }.sortedWith(
                    compareBy<EventEntity> { it.isCompleted }
                        .thenBy { it.eventTimestamp }
                )
            }
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            LedgerTopHeader(
                title = mode.title,
                onHomeClick = onHomeClick,
                onMenuClick = onMenuClick
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(20.dp),
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp),
                modifier = Modifier.padding(bottom = 16.dp, end = 8.dp)
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add")
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = mode.addButtonText,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (displayItems.isEmpty()) {
                // Initial page: Clean background page showing through with centered frosted + button prompt
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.88f)
                        ),
                        border = BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp, vertical = 32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .background(
                                        brush = Brush.radialGradient(
                                            listOf(
                                                MaterialTheme.colorScheme.primary.copy(alpha = 0.25f),
                                                MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
                                            )
                                        ),
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (mode == DateScreenMode.IMPORTANT_DATES) Icons.Filled.Cake else Icons.Filled.NotificationsActive,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(38.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = mode.emptyTitle,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = mode.emptySubtitle,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                lineHeight = 20.sp
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            Button(
                                onClick = { showAddDialog = true },
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                ),
                                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
                            ) {
                                Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = mode.addButtonText,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                            }
                        }
                    }
                }
            } else {
                // List of items cleanly displayed over the background page
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        // Header summary row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp, horizontal = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = mode.subtitle,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                            ) {
                                Text(
                                    text = "${displayItems.size} ${if (displayItems.size == 1) "item" else "items"}",
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }

                    items(displayItems, key = { it.id }) { item ->
                        ItemCard(
                            item = item,
                            mode = mode,
                            onToggleComplete = { onToggleComplete?.invoke(item) },
                            onDelete = { eventToDelete = item }
                        )
                    }
                }
            }
        }
    }

    // Add Date / Add Task Dialog (Identical Rich UI of Adding)
    if (showAddDialog) {
        AddDateOrTaskDialog(
            mode = mode,
            onDismiss = { showAddDialog = false },
            onConfirm = { title, note, notify, includeYear, timestamp, category ->
                onAddEvent(
                    title,
                    note,
                    notify,
                    includeYear,
                    timestamp,
                    category,
                    mode.defaultEventType
                )
                showAddDialog = false
            }
        )
    }

    // Delete Confirmation Dialog
    eventToDelete?.let { item ->
        AlertDialog(
            onDismissRequest = { eventToDelete = null },
            title = { Text("Delete Entry?", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to remove \"${item.title}\"?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteEvent(item)
                        eventToDelete = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { eventToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

/**
 * Item Card showing Category, Title, Date, Countdown, Notes, Alert, and Complete Checkbox (for Remind Me)
 */
@Composable
private fun ItemCard(
    item: EventEntity,
    mode: DateScreenMode,
    onToggleComplete: () -> Unit,
    onDelete: () -> Unit
) {
    val (categoryIcon, categoryColor) = when (item.category.lowercase()) {
        "birthday" -> Pair(Icons.Filled.Cake, Color(0xFFF43F5E))
        "anniversary" -> Pair(Icons.Filled.Favorite, Color(0xFFE11D48))
        "daily task" -> Pair(Icons.Filled.CheckCircle, Color(0xFF10B981))
        "coming task" -> Pair(Icons.Filled.Schedule, Color(0xFF0284C7))
        "reminder" -> Pair(Icons.Filled.NotificationsActive, Color(0xFFF59E0B))
        else -> Pair(Icons.Filled.Event, Color(0xFF6366F1))
    }

    val dateFormat = remember { SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault()) }
    val timeFormat = remember { SimpleDateFormat("hh:mm a", Locale.getDefault()) }
    val formattedDate = remember(item.eventTimestamp) {
        val d = Date(item.eventTimestamp)
        "${dateFormat.format(d)} • ${timeFormat.format(d)}"
    }

    // Calculate days until event
    val daysUntil = remember(item.eventTimestamp, item.includeYear) {
        val nowCal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val targetCal = Calendar.getInstance().apply {
            timeInMillis = item.eventTimestamp
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (!item.includeYear && mode == DateScreenMode.IMPORTANT_DATES) {
                set(Calendar.YEAR, nowCal.get(Calendar.YEAR))
                if (before(nowCal)) {
                    add(Calendar.YEAR, 1)
                }
            }
        }
        val diffMillis = targetCal.timeInMillis - nowCal.timeInMillis
        (diffMillis / (1000 * 60 * 60 * 24)).toInt()
    }

    val countdownText = when {
        daysUntil == 0 -> "Today! 🎉"
        daysUntil == 1 -> "Tomorrow!"
        daysUntil in 2..365 -> "In $daysUntil days"
        daysUntil < 0 -> "${-daysUntil} days ago"
        else -> ""
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(
                alpha = if (item.isCompleted) 0.75f else 0.94f
            )
        ),
        border = BorderStroke(
            width = 1.dp,
            color = if (item.isCompleted) MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                    else categoryColor.copy(alpha = 0.3f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // In Remind Me mode: completion checkbox
            if (mode == DateScreenMode.REMIND_ME) {
                Checkbox(
                    checked = item.isCompleted,
                    onCheckedChange = { onToggleComplete() },
                    modifier = Modifier.padding(end = 6.dp)
                )
            }

            // Category Icon Badge
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(categoryColor.copy(alpha = if (item.isCompleted) 0.10f else 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = categoryIcon,
                    contentDescription = null,
                    tint = if (item.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant else categoryColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Details Column
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (item.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant
                               else MaterialTheme.colorScheme.onSurface,
                        textDecoration = if (item.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    if (countdownText.isNotBlank() && !item.isCompleted) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = categoryColor.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = countdownText,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = categoryColor
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Date & Time
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.CalendarToday,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = formattedDate,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Note / Location (if entered)
                if (item.locationOrNote.isNotBlank()) {
                    Spacer(modifier = Modifier.height(3.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Notes,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = item.locationOrNote,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Alert notification indicator & category label
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                    ) {
                        Text(
                            text = item.category,
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                        )
                    }

                    if (item.notifyMe) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.NotificationsActive,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(10.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "Alert On",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Delete Action Button
            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

/**
 * Add Date / Add Task Dialog
 * Preserves the exact same UI of adding:
 * - Header with title & close button
 * - Category Chips
 * - Title input
 * - Date Card with picker and interactive calendar + quick adjusters
 * - Time picker row
 * - Notes input
 * - Reminder alert toggle
 * - Cancel and Save buttons
 */
@Composable
private fun AddDateOrTaskDialog(
    mode: DateScreenMode,
    onDismiss: () -> Unit,
    onConfirm: (
        title: String,
        note: String,
        notify: Boolean,
        includeYear: Boolean,
        timestamp: Long,
        category: String
    ) -> Unit
) {
    val context = LocalContext.current
    val categories = remember(mode) {
        when (mode) {
            DateScreenMode.IMPORTANT_DATES -> listOf(
                CategoryOption("Birthday", Icons.Filled.Cake, Color(0xFFE11D48)),
                CategoryOption("Anniversary", Icons.Filled.Favorite, Color(0xFFDB2777))
            )
            DateScreenMode.REMIND_ME -> listOf(
                CategoryOption("Coming Task", Icons.Filled.Schedule, Color(0xFF0284C7)),
                CategoryOption("Daily Task", Icons.Filled.CheckCircle, Color(0xFF10B981)),
                CategoryOption("Reminder", Icons.Filled.Notifications, Color(0xFFF59E0B)),
                CategoryOption("General", Icons.Filled.Assignment, Color(0xFF6366F1))
            )
        }
    }

    var selectedCategory by remember { mutableStateOf(categories.first().title) }
    var title by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var notify by remember { mutableStateOf(true) }
    val includeYear by remember { mutableStateOf(true) }

    // Calendar state
    val pickedCalendar = remember {
        mutableStateOf(Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 9)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        })
    }

    val dateFormat = remember { SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault()) }
    val timeFormat = remember { SimpleDateFormat("hh:mm a", Locale.getDefault()) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.92f),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Dialog Header
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .background(
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (mode == DateScreenMode.IMPORTANT_DATES) Icons.Filled.Cake else Icons.Filled.NotificationsActive,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = if (mode == DateScreenMode.IMPORTANT_DATES) "Add Important Date" else "Add to Remind Me",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = if (mode == DateScreenMode.IMPORTANT_DATES) "Store a birthday or anniversary" else "Add coming tasks or daily reminders",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                Icons.Filled.Close,
                                contentDescription = "Close",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    HorizontalDivider(
                        modifier = Modifier.padding(top = 12.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )
                }

                // Category Selection Chips
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Category:",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        val chunkedCategories = categories.chunked(2)
                        chunkedCategories.forEach { rowCategories ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                rowCategories.forEach { cat ->
                                    val isSelected = selectedCategory == cat.title
                                    Surface(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(44.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .clickable { selectedCategory = cat.title },
                                        shape = RoundedCornerShape(12.dp),
                                        color = if (isSelected) cat.color else cat.color.copy(alpha = 0.10f),
                                        border = BorderStroke(
                                            1.dp,
                                            if (isSelected) cat.color else cat.color.copy(alpha = 0.25f)
                                        )
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(horizontal = 10.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.Center
                                        ) {
                                            Icon(
                                                imageVector = cat.icon,
                                                contentDescription = null,
                                                tint = if (isSelected) Color.White else cat.color,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = cat.title,
                                                fontSize = 13.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                }
                                if (rowCategories.size == 1) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }

                // Title Input
                item {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text(if (mode == DateScreenMode.IMPORTANT_DATES) "Title *" else "Task / Reminder Title *") },
                        placeholder = {
                            Text(
                                when (selectedCategory) {
                                    "Birthday" -> "e.g. Papa's Birthday"
                                    "Anniversary" -> "e.g. Wedding Anniversary"
                                    "Coming Task" -> "e.g. Pay electricity bill / Submit taxes"
                                    "Daily Task" -> "e.g. Morning medication / Workout"
                                    "Reminder" -> "e.g. Doctor appointment"
                                    else -> "e.g. Important item"
                                }
                            )
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Filled.EditCalendar,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)
                        )
                    )
                }

                // Selected Date & Time Card (with date picker, time picker, and quick adjusters)
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                        ),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            // Date Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Filled.CalendarMonth,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "Selected Date",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = dateFormat.format(pickedCalendar.value.time),
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                                FilledTonalButton(
                                    onClick = {
                                        showDatePickerDialog(context, pickedCalendar.value.timeInMillis) { newTs ->
                                            val cal = Calendar.getInstance().apply { timeInMillis = newTs }
                                            cal.set(Calendar.HOUR_OF_DAY, pickedCalendar.value.get(Calendar.HOUR_OF_DAY))
                                            cal.set(Calendar.MINUTE, pickedCalendar.value.get(Calendar.MINUTE))
                                            pickedCalendar.value = cal
                                        }
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Icon(Icons.Filled.CalendarToday, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Change", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Time Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Filled.AccessTime,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.secondary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "Scheduled Time",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.secondary,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = timeFormat.format(pickedCalendar.value.time),
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                                FilledTonalButton(
                                    onClick = {
                                        showTimePickerDialog(
                                            context,
                                            pickedCalendar.value.get(Calendar.HOUR_OF_DAY),
                                            pickedCalendar.value.get(Calendar.MINUTE)
                                        ) { h, m ->
                                            val cal = pickedCalendar.value.clone() as Calendar
                                            cal.set(Calendar.HOUR_OF_DAY, h)
                                            cal.set(Calendar.MINUTE, m)
                                            pickedCalendar.value = cal
                                        }
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Icon(Icons.Filled.Schedule, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Set Time", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Quick adjusters: -1 Day, Today, +1 Day, +3 Days, +1 Week
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        val cal = pickedCalendar.value.clone() as Calendar
                                        cal.add(Calendar.DAY_OF_MONTH, -1)
                                        pickedCalendar.value = cal
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp)
                                ) {
                                    Text("-1 Day", fontSize = 10.sp)
                                }
                                OutlinedButton(
                                    onClick = {
                                        val h = pickedCalendar.value.get(Calendar.HOUR_OF_DAY)
                                        val m = pickedCalendar.value.get(Calendar.MINUTE)
                                        val cal = Calendar.getInstance().apply {
                                            set(Calendar.HOUR_OF_DAY, h)
                                            set(Calendar.MINUTE, m)
                                        }
                                        pickedCalendar.value = cal
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp)
                                ) {
                                    Text("Today", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                                OutlinedButton(
                                    onClick = {
                                        val cal = pickedCalendar.value.clone() as Calendar
                                        cal.add(Calendar.DAY_OF_MONTH, 1)
                                        pickedCalendar.value = cal
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp)
                                ) {
                                    Text("+1 Day", fontSize = 10.sp)
                                }
                                OutlinedButton(
                                    onClick = {
                                        val cal = pickedCalendar.value.clone() as Calendar
                                        cal.add(Calendar.DAY_OF_MONTH, 7)
                                        pickedCalendar.value = cal
                                    },
                                    modifier = Modifier.weight(1.1f),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp)
                                ) {
                                    Text("+1 Week", fontSize = 10.sp)
                                }
                            }
                        }
                    }
                }

                // Interactive Calendar Widget right inside Dialog for instant month/day picking
                item {
                    MiniCalendarWidget(
                        calendarState = pickedCalendar.value,
                        onSelectDay = { day ->
                            val cal = pickedCalendar.value.clone() as Calendar
                            cal.set(Calendar.DAY_OF_MONTH, day)
                            pickedCalendar.value = cal
                        },
                        onPrevMonth = {
                            val cal = pickedCalendar.value.clone() as Calendar
                            cal.add(Calendar.MONTH, -1)
                            pickedCalendar.value = cal
                        },
                        onNextMonth = {
                            val cal = pickedCalendar.value.clone() as Calendar
                            cal.add(Calendar.MONTH, 1)
                            pickedCalendar.value = cal
                        }
                    )
                }

                // Notes / Location Input
                item {
                    OutlinedTextField(
                        value = note,
                        onValueChange = { note = it },
                        label = { Text("Notes / Details (Optional)") },
                        placeholder = {
                            Text(
                                if (mode == DateScreenMode.IMPORTANT_DATES)
                                    "e.g. Gift ideas, dinner reservations, anniversary years"
                                else
                                    "e.g. Steps to complete, contact person, priority details"
                            )
                        },
                        leadingIcon = {
                            Icon(
                                Icons.AutoMirrored.Filled.Notes,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)
                        )
                    )
                }

                // Notification Alert Card
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { notify = !notify },
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (notify) MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
                                             else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                        ),
                        border = BorderStroke(
                            1.dp,
                            if (notify) MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                            else Color.Transparent
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(checked = notify, onCheckedChange = { notify = it })
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = if (mode == DateScreenMode.IMPORTANT_DATES) "Remind me before this date" else "Alert notification reminder",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Receive scheduled alert on your device",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // Action Buttons: Cancel and Save
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text("Cancel", fontWeight = FontWeight.SemiBold)
                        }
                        Button(
                            onClick = {
                                if (title.isNotBlank()) {
                                    onConfirm(
                                        title.trim(),
                                        note.trim(),
                                        notify,
                                        includeYear,
                                        pickedCalendar.value.timeInMillis,
                                        selectedCategory
                                    )
                                }
                            },
                            enabled = title.isNotBlank(),
                            modifier = Modifier
                                .weight(1.3f)
                                .height(50.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (mode == DateScreenMode.IMPORTANT_DATES) "Save Date" else "Save Task",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Interactive mini calendar component for fast day selection inside the dialog
 */
@Composable
private fun MiniCalendarWidget(
    calendarState: Calendar,
    onSelectDay: (Int) -> Unit,
    onPrevMonth: () -> Unit,
    onNextMonth: () -> Unit
) {
    val monthYearFormat = remember { SimpleDateFormat("MMMM yyyy", Locale.getDefault()) }
    val currentMonthYear = remember(calendarState.timeInMillis) {
        monthYearFormat.format(calendarState.time)
    }

    val daysInMonth = remember(calendarState.timeInMillis) {
        val cal = calendarState.clone() as Calendar
        cal.getActualMaximum(Calendar.DAY_OF_MONTH)
    }

    val firstDayOfWeek = remember(calendarState.timeInMillis) {
        val cal = calendarState.clone() as Calendar
        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.get(Calendar.DAY_OF_WEEK) - 1 // 0 for Sunday
    }

    val selectedDay = calendarState.get(Calendar.DAY_OF_MONTH)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
        ) {
            // Month Header with Prev and Next
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onPrevMonth, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Filled.ChevronLeft, contentDescription = "Previous Month", modifier = Modifier.size(18.dp))
                }
                Text(
                    text = currentMonthYear,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                IconButton(onClick = onNextMonth, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Filled.ChevronRight, contentDescription = "Next Month", modifier = Modifier.size(18.dp))
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Day labels S M T W T F S
            val dayHeaders = listOf("S", "M", "T", "W", "T", "F", "S")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                dayHeaders.forEach { header ->
                    Text(
                        text = header,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Days grid (up to 6 rows)
            val totalCells = 42
            for (row in 0 until 6) {
                if (row * 7 - firstDayOfWeek + 1 > daysInMonth) break
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    for (col in 0 until 7) {
                        val cellIndex = row * 7 + col
                        val dayNumber = cellIndex - firstDayOfWeek + 1

                        if (dayNumber in 1..daysInMonth) {
                            val isSelected = dayNumber == selectedDay
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(30.dp)
                                    .padding(1.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isSelected) MaterialTheme.colorScheme.primary
                                        else Color.Transparent
                                    )
                                    .clickable { onSelectDay(dayNumber) },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "$dayNumber",
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                                            else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        } else {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

private fun showDatePickerDialog(context: Context, initialTimestamp: Long, onDateSelected: (Long) -> Unit) {
    val cal = Calendar.getInstance().apply { timeInMillis = initialTimestamp }
    val dpd = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            val resultCal = Calendar.getInstance().apply {
                timeInMillis = initialTimestamp
                set(Calendar.YEAR, year)
                set(Calendar.MONTH, month)
                set(Calendar.DAY_OF_MONTH, dayOfMonth)
            }
            onDateSelected(resultCal.timeInMillis)
        },
        cal.get(Calendar.YEAR),
        cal.get(Calendar.MONTH),
        cal.get(Calendar.DAY_OF_MONTH)
    )
    dpd.show()
}

private fun showTimePickerDialog(context: Context, initialHour: Int, initialMinute: Int, onTimeSelected: (Int, Int) -> Unit) {
    val tpd = TimePickerDialog(
        context,
        { _, hourOfDay, minute ->
            onTimeSelected(hourOfDay, minute)
        },
        initialHour,
        initialMinute,
        false
    )
    tpd.show()
}
