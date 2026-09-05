package com.example.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.R
import com.example.data.model.DailyScheduleEntity
import com.example.ui.components.AppNotification
import com.example.ui.components.ColoredNotificationBannerHost
import com.example.ui.components.LedgerEmptyState
import com.example.ui.components.LedgerTopHeader
import com.example.ui.components.NotificationType
import com.example.ui.components.OrganizeTodayBrandBadge
import com.example.ui.theme.*
import com.example.util.rememberSpeechToTextLauncher
import com.example.util.rememberTextToSpeechHelper
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DailyScheduleScreen(
    schedules: List<DailyScheduleEntity>,
    onAddSchedule: (DailyScheduleEntity) -> Unit,
    onToggleComplete: (DailyScheduleEntity) -> Unit,
    onUpdateSchedule: (DailyScheduleEntity) -> Unit,
    onDeleteSchedule: (DailyScheduleEntity) -> Unit,
    onLoadSampleRoutine: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    onMenuClick: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val tts = rememberTextToSpeechHelper()

    var showAddDialog by remember { mutableStateOf(false) }
    var viewingSchedule by remember { mutableStateOf<DailyScheduleEntity?>(null) }
    var editingSchedule by remember { mutableStateOf<DailyScheduleEntity?>(null) }
    var selectedCalendarDate by remember { mutableStateOf(Calendar.getInstance()) }
    var presetTimeSlot by remember { mutableStateOf<String?>(null) }

    // Colored Notification Banner State
    var activeNotification by remember { mutableStateOf<AppNotification?>(null) }

    fun showNotification(title: String, message: String, type: NotificationType) {
        activeNotification = AppNotification(
            title = title,
            message = message,
            type = type
        )
    }

    val filteredSchedules = remember(schedules, selectedCalendarDate) {
        val startOfDay = (selectedCalendarDate.clone() as Calendar).apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val endOfDay = (selectedCalendarDate.clone() as Calendar).apply {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.timeInMillis

        schedules.filter { item ->
            if (item.isMultiDay && item.endTimestamp != null) {
                item.timestamp <= endOfDay && item.endTimestamp >= startOfDay
            } else {
                item.timestamp in startOfDay..endOfDay
            }
        }.sortedWith(
            compareBy<DailyScheduleEntity> { it.isCompleted }
                .thenBy { it.timestamp }
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Transparent)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Transparent)
        ) {
            // Header
            LedgerTopHeader(
                title = "Daily Schedule",
                onMenuClick = onMenuClick
            )

            // Colored Notification Banner Host
            ColoredNotificationBannerHost(
                notification = activeNotification,
                onDismiss = { activeNotification = null }
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Agenda Score Cards directly on the Background Image
            if (filteredSchedules.isNotEmpty()) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp)
                        .weight(1f),
                    contentPadding = PaddingValues(top = 4.dp, bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredSchedules, key = { item -> item.id }) { schedule ->
                        ScheduleScoreCard(
                            schedule = schedule,
                            onToggleComplete = {
                                onToggleComplete(schedule)
                                val newStatus = if (!schedule.isCompleted) "Done ✓" else "Pending"
                                showNotification("Status Updated", "'${schedule.title}' marked $newStatus", NotificationType.SUCCESS)
                            },
                            onClick = { viewingSchedule = schedule },
                            onDelete = {
                                onDeleteSchedule(schedule)
                                showNotification("Schedule Deleted", "'${schedule.title}' removed", NotificationType.ERROR)
                            },
                            onSpeak = {
                                val speechText = "${schedule.title}. Scheduled for ${schedule.timeSlot}. ${if (schedule.note.isNotBlank()) schedule.note else ""}"
                                tts.speak(speechText)
                                showNotification("Voice Output Playing", "Reading: '${schedule.title}'", NotificationType.VOICE)
                            }
                        )
                    }
                }
            } else {
                Spacer(modifier = Modifier.weight(1f))
            }
        }

        // Add Dialog
        if (showAddDialog) {
            AddEditScheduleDialog(
                initialSchedule = null,
                presetTimeSlot = presetTimeSlot,
                onDismiss = {
                    showAddDialog = false
                    presetTimeSlot = null
                },
                onSave = { newSchedule ->
                    onAddSchedule(newSchedule)
                    showAddDialog = false
                    presetTimeSlot = null
                    showNotification(
                        title = "Schedule Created",
                        message = "'${newSchedule.title}' saved for ${newSchedule.timeSlot}",
                        type = NotificationType.SUCCESS
                    )
                },
                onNotify = { title, msg, type -> showNotification(title, msg, type) }
            )
        }

        // Edit Dialog
        editingSchedule?.let { schedule ->
            AddEditScheduleDialog(
                initialSchedule = schedule,
                onDismiss = { editingSchedule = null },
                onSave = { updated ->
                    onUpdateSchedule(updated)
                    editingSchedule = null
                    viewingSchedule = null
                    showNotification(
                        title = "Schedule Updated",
                        message = "'${updated.title}' updated",
                        type = NotificationType.SUCCESS
                    )
                },
                onNotify = { title, msg, type -> showNotification(title, msg, type) }
            )
        }

        // View Detail Dialog
        viewingSchedule?.let { schedule ->
            ScheduleDetailDialog(
                schedule = schedule,
                onDismiss = { viewingSchedule = null },
                onEdit = {
                    editingSchedule = schedule
                    viewingSchedule = null
                },
                onDelete = {
                    onDeleteSchedule(schedule)
                    viewingSchedule = null
                    showNotification("Schedule Deleted", "'${schedule.title}' removed", NotificationType.ERROR)
                },
                onToggleComplete = {
                    onToggleComplete(schedule)
                    viewingSchedule = schedule.copy(isCompleted = !schedule.isCompleted)
                },
                onSpeak = {
                    val speechText = "${schedule.title}. Scheduled at ${schedule.timeSlot}. ${if (schedule.note.isNotBlank()) schedule.note else ""}"
                    tts.speak(speechText)
                    showNotification("Voice Output Playing", "Reading: '${schedule.title}'", NotificationType.VOICE)
                }
            )
        }

        // Floating Action Button (+) at lower right
        FloatingActionButton(
            onClick = { showAddDialog = true },
            containerColor = Color(0xFFD81B60),
            contentColor = Color.White,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 16.dp, end = 16.dp)
                .size(44.dp)
                .shadow(6.dp, CircleShape, ambientColor = Color(0xFFD81B60), spotColor = Color(0xFFD81B60))
        ) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = "Add Schedule",
                tint = Color.White,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

@Composable
private fun ScheduleScoreCard(
    schedule: DailyScheduleEntity,
    onToggleComplete: () -> Unit,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onSpeak: (() -> Unit)? = null
) {
    val (timeDigits, period) = parseTimeDisplay(schedule.timeSlot)
    val categoryStyle = getCategoryStyleForTitle(schedule.title)

    val cardBackgroundGradient = Brush.horizontalGradient(
        listOf(
            Color(0xFF1E1D22),
            Color(0xFF26242C),
            Color(0xFF332932),
            Color(0xFF4C2A33),
            Color(0xFF7A3E31),
            Color(0xFFB55D30),
            Color(0xFFD47C3B),
            Color(0xFFE59C4A)
        )
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(18.dp),
                ambientColor = Color(0xFFD47C3B).copy(alpha = 0.3f),
                spotColor = Color(0xFFE59C4A).copy(alpha = 0.4f)
            ),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, Color(0xFFFFD56B).copy(alpha = 0.6f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(cardBackgroundGradient)
                .padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 1. Time Badge Column
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White.copy(alpha = 0.18f),
                    border = BorderStroke(0.8.dp, Color.White.copy(alpha = 0.3f)),
                    modifier = Modifier.padding(end = 12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = timeDigits,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                        if (period.isNotEmpty()) {
                            Text(
                                text = period,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFFD56B)
                            )
                        }
                    }
                }

                // 2. Category Icon Badge
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = categoryStyle.icon,
                        contentDescription = schedule.category,
                        tint = Color(0xFFFFD56B),
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                // 3. Task Title & Heart Script / Note
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = schedule.title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (schedule.isCompleted) Color.White.copy(alpha = 0.6f) else Color.White,
                        textDecoration = if (schedule.isCompleted) TextDecoration.LineThrough else null,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Favorite,
                            contentDescription = "Heart",
                            tint = Color(0xFFF43F5E),
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = if (schedule.note.isNotBlank()) schedule.note else "Better Days Ahead ♡",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Cursive,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFFE082),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                // 4. Action Buttons & Radio Checkbox
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (onSpeak != null) {
                        IconButton(
                            onClick = onSpeak,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.VolumeUp,
                                contentDescription = "Read Aloud",
                                tint = Color(0xFFE0E7FF),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.DeleteOutline,
                            contentDescription = "Delete",
                            tint = Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    // Completion Checkbox Ring
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(if (schedule.isCompleted) Color(0xFF38BDF8) else Color.Transparent)
                            .border(
                                width = if (schedule.isCompleted) 0.dp else 1.8.dp,
                                color = if (schedule.isCompleted) Color.Transparent else Color.White.copy(alpha = 0.8f),
                                shape = CircleShape
                            )
                            .clickable { onToggleComplete() },
                        contentAlignment = Alignment.Center
                    ) {
                        if (schedule.isCompleted) {
                            Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = "Completed",
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun parseTimeDisplay(timeSlot: String): Pair<String, String> {
    val trimmed = timeSlot.trim()
    if (trimmed.isEmpty()) return Pair("12:00", "AM")
    val upper = trimmed.uppercase(Locale.getDefault())

    val isPm = upper.contains("PM")
    val isAm = upper.contains("AM")
    val period = if (isPm) "PM" else if (isAm) "AM" else ""

    val cleanDigits = upper.replace("AM", "").replace("PM", "").trim()
    val digits = if (cleanDigits.startsWith("0") && cleanDigits.length > 1 && cleanDigits[1] != ':') {
        cleanDigits.substring(1)
    } else {
        cleanDigits
    }

    return Pair(if (digits.isNotEmpty()) digits else "12:00", period)
}

private data class ScheduleCategoryStyle(
    val bg: Color,
    val iconTint: Color,
    val icon: ImageVector
)

private fun getCategoryStyleForTitle(title: String): ScheduleCategoryStyle {
    val lower = title.lowercase(Locale.getDefault())
    return when {
        lower.contains("yoga") || lower.contains("tea") || lower.contains("meditat") || lower.contains("spa") || lower.contains("health") ->
            ScheduleCategoryStyle(Color(0xFFDCFCE7), Color(0xFF059669), Icons.Filled.SelfImprovement)
        lower.contains("work") || lower.contains("project") || lower.contains("code") || lower.contains("office") || lower.contains("meeting") || lower.contains("dev") ->
            ScheduleCategoryStyle(Color(0xFFDBEAFE), Color(0xFF2563EB), Icons.Filled.Laptop)
        lower.contains("lunch") || lower.contains("break") || lower.contains("food") || lower.contains("eat") || lower.contains("dinner") || lower.contains("meal") || lower.contains("coffee") ->
            ScheduleCategoryStyle(Color(0xFFFCE7F3), Color(0xFFE11D48), Icons.Filled.Restaurant)
        lower.contains("read") || lower.contains("book") || lower.contains("study") || lower.contains("learn") || lower.contains("journal") ->
            ScheduleCategoryStyle(Color(0xFFF3E8FF), Color(0xFF9333EA), Icons.Filled.MenuBook)
        lower.contains("gym") || lower.contains("run") || lower.contains("walk") || lower.contains("fit") || lower.contains("workout") ->
            ScheduleCategoryStyle(Color(0xFFFEF3C7), Color(0xFFD97706), Icons.Filled.FitnessCenter)
        lower.contains("shop") || lower.contains("buy") || lower.contains("grocer") ->
            ScheduleCategoryStyle(Color(0xFFE0F2FE), Color(0xFF0284C7), Icons.Filled.ShoppingCart)
        else ->
            ScheduleCategoryStyle(Color(0xFFFFEDD5), Color(0xFFEA580C), Icons.Filled.EventNote)
    }
}

@Composable
private fun AddEditScheduleDialog(
    initialSchedule: DailyScheduleEntity?,
    presetTimeSlot: String? = null,
    onDismiss: () -> Unit,
    onSave: (DailyScheduleEntity) -> Unit,
    onNotify: ((String, String, NotificationType) -> Unit)? = null
) {
    val context = LocalContext.current
    var title by remember { mutableStateOf(initialSchedule?.title ?: "") }
    var note by remember { mutableStateOf(initialSchedule?.note ?: "") }
    var recurrence by remember { mutableStateOf(initialSchedule?.recurrence ?: "DAILY") }
    var category by remember { mutableStateOf(initialSchedule?.category ?: "Routine") }
    var timeSlot by remember { mutableStateOf(initialSchedule?.timeSlot ?: presetTimeSlot ?: "09:00 AM") }
    var notifyMe by remember { mutableStateOf(initialSchedule?.notifyMe ?: true) }

    // Voice Speech-to-Text Launchers
    val launchTitleVoiceInput = rememberSpeechToTextLauncher(
        onSpeechResult = { spoken ->
            title = spoken
            onNotify?.invoke("Voice Speech Captured", "Title set to: '$spoken'", NotificationType.VOICE)
        },
        onError = { err -> onNotify?.invoke("Voice Error", err, NotificationType.ERROR) }
    )

    val launchNoteVoiceInput = rememberSpeechToTextLauncher(
        onSpeechResult = { spoken ->
            note = if (note.isBlank()) spoken else "$note\n$spoken"
            onNotify?.invoke("Voice Speech Captured", "Notes updated by voice", NotificationType.VOICE)
        },
        onError = { err -> onNotify?.invoke("Voice Error", err, NotificationType.ERROR) }
    )

    // Multi-Day / Date Range State
    var isMultiDay by remember { mutableStateOf(initialSchedule?.isMultiDay ?: (initialSchedule?.endTimestamp != null)) }

    val startCalendar = remember {
        Calendar.getInstance().apply {
            if (initialSchedule != null) {
                timeInMillis = initialSchedule.timestamp
            }
        }
    }
    var startTimestamp by remember { mutableStateOf(startCalendar.timeInMillis) }

    val endCalendar = remember {
        Calendar.getInstance().apply {
            if (initialSchedule?.endTimestamp != null) {
                timeInMillis = initialSchedule.endTimestamp
            } else {
                // Default to +2 days for multi-day
                timeInMillis = startTimestamp + (2 * 24 * 60 * 60 * 1000L)
            }
        }
    }
    var endTimestamp by remember { mutableStateOf(endCalendar.timeInMillis) }

    // User-added Custom Categories State
    var customCategories by remember { mutableStateOf(listOf<String>()) }
    var showAddCategoryInput by remember { mutableStateOf(false) }
    var newCategoryText by remember { mutableStateOf("") }

    val defaultCategories = listOf("Routine", "Work", "Health", "Study", "Personal", "Diary", "Travel", "Project")
    val allCategories = remember(customCategories) {
        (defaultCategories + customCategories).distinct()
    }

    val recurrences = listOf("DAILY", "WEEK", "MONTH", "ANNUAL")
    val dateFormat = remember { SimpleDateFormat("EEE, MMM d, yyyy", Locale.getDefault()) }

    // Form Shimmer and animated glow transitions
    val glowTransition = rememberInfiniteTransition(label = "DialogGlow")
    val borderGlowAlpha by glowTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "BorderGlow"
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .wrapContentHeight()
                .padding(vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .shadow(
                        elevation = 22.dp,
                        shape = RoundedCornerShape(26.dp),
                        ambientColor = Color(0xFF0284C7).copy(alpha = 0.30f),
                        spotColor = Color(0xFF0284C7).copy(alpha = 0.40f)
                    ),
                shape = RoundedCornerShape(26.dp),
                color = Color.White,
                border = BorderStroke(
                    1.6.dp,
                    Brush.linearGradient(
                        listOf(
                            Color(0xFF0284C7).copy(alpha = borderGlowAlpha),
                            Color(0xFF38BDF8).copy(alpha = 0.8f),
                            Color(0xFFBAE6FD)
                        )
                    )
                )
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    // App Icon Background Image with subtle warm overlay
                    Image(
                        painter = painterResource(id = R.drawable.app_icon),
                        contentDescription = "Background",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .matchParentSize()
                            .clip(RoundedCornerShape(26.dp))
                    )
                    // Translucent frosted glass layer for clear readability
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(Color.White.copy(alpha = 0.92f))
                    )

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 18.dp, vertical = 14.dp),
                        verticalArrangement = Arrangement.spacedBy(11.dp)
                    ) {
                        // Header Banner with animated gradient title badge
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(34.dp)
                                            .clip(CircleShape)
                                            .background(
                                                Brush.radialGradient(
                                                    listOf(Color(0xFFBAE6FD), Color(0xFF0284C7))
                                                )
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Filled.CalendarMonth,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(19.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = if (initialSchedule == null) "New Schedule & Diary" else "Edit Schedule",
                                            color = Color(0xFF0369A1),
                                            fontSize = 20.sp,
                                            fontFamily = FontFamily.Cursive,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "Plan activities, multi-day ranges & diary notes",
                                            color = Color(0xFF64748B),
                                            fontSize = 11.sp
                                        )
                                    }
                                }

                                IconButton(
                                    onClick = onDismiss,
                                    modifier = Modifier
                                        .size(30.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFF1F5F9))
                                ) {
                                    Icon(Icons.Filled.Close, contentDescription = "Close", tint = Color(0xFF0369A1), modifier = Modifier.size(18.dp))
                                }
                            }
                        }

                        // Title Input with glowing border on focus & Microphone voice input button
                        item {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                color = Color.White.copy(alpha = 0.95f),
                                border = BorderStroke(1.dp, Color(0xFFBAE6FD))
                            ) {
                                OutlinedTextField(
                                    value = title,
                                    onValueChange = { title = it },
                                    label = { Text("Schedule / Activity Title *", color = Color(0xFF0369A1), fontSize = 12.5.sp, fontFamily = FontFamily.Cursive) },
                                    placeholder = { Text("e.g. Conference Trip, Morning Routine, Book Reading", fontSize = 12.sp) },
                                    leadingIcon = {
                                        Icon(Icons.Filled.EditCalendar, contentDescription = null, tint = Color(0xFF0284C7), modifier = Modifier.size(20.dp))
                                    },
                                    trailingIcon = {
                                        IconButton(
                                            onClick = launchTitleVoiceInput,
                                            modifier = Modifier
                                                .size(34.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFFF3E8FF))
                                        ) {
                                            Icon(
                                                Icons.Filled.Mic,
                                                contentDescription = "Voice Input Title",
                                                tint = Color(0xFF9333EA),
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    },
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Color(0xFF0284C7),
                                        unfocusedBorderColor = Color.Transparent,
                                        focusedContainerColor = Color.Transparent,
                                        unfocusedContainerColor = Color.Transparent
                                    )
                                )
                            }
                        }

                        // Multi-Day / Single-Day Mode Switch (UI Effect Card)
                        item {
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable { isMultiDay = !isMultiDay },
                                shape = RoundedCornerShape(14.dp),
                                color = if (isMultiDay) Color(0xFFF5F3FF) else Color(0xFFF0F9FF),
                                border = BorderStroke(
                                    1.2.dp,
                                    if (isMultiDay) Color(0xFFC084FC) else Color(0xFFBAE6FD)
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(28.dp)
                                                .clip(CircleShape)
                                                .background(if (isMultiDay) Color(0xFF9333EA) else Color(0xFF0284C7)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                if (isMultiDay) Icons.Filled.DateRange else Icons.Filled.Today,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                        Column {
                                            Text(
                                                text = if (isMultiDay) "Multi-Day Schedule (Date Range)" else "Single-Day Schedule",
                                                color = if (isMultiDay) Color(0xFF6B21A8) else Color(0xFF0369A1),
                                                fontSize = 12.5.sp,
                                                fontFamily = FontFamily.Cursive,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = if (isMultiDay) "Extends across multiple days with Start & End dates" else "Happens on a single date",
                                                color = Color(0xFF64748B),
                                                fontSize = 10.5.sp
                                            )
                                        }
                                    }

                                    Switch(
                                        checked = isMultiDay,
                                        onCheckedChange = { isMultiDay = it },
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = Color.White,
                                            checkedTrackColor = Color(0xFF9333EA),
                                            uncheckedThumbColor = Color.White,
                                            uncheckedTrackColor = Color(0xFFBAE6FD)
                                        ),
                                        modifier = Modifier.height(26.dp)
                                    )
                                }
                            }
                        }

                        // Date & Time pickers Container with Animated Multi-Day Expansion
                        item {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                color = Color(0xFFF8FAFC),
                                border = BorderStroke(1.dp, Color(0xFFBAE6FD))
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    // Animated Start and End Date row
                                    if (isMultiDay) {
                                        // Multi-Day Pickers
                                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                            // Start Date
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Icon(Icons.Filled.PlayArrow, contentDescription = null, tint = Color(0xFF0284C7), modifier = Modifier.size(14.dp))
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        Text("Start Date:", fontSize = 11.sp, color = Color(0xFF0369A1), fontFamily = FontFamily.Cursive, fontWeight = FontWeight.Bold)
                                                    }
                                                    Text(dateFormat.format(Date(startTimestamp)), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0C4A6E))
                                                }
                                                Button(
                                                    onClick = {
                                                        val c = Calendar.getInstance().apply { timeInMillis = startTimestamp }
                                                        DatePickerDialog(
                                                            context,
                                                            { _, y, m, d ->
                                                                val newCal = Calendar.getInstance().apply { set(y, m, d) }
                                                                startTimestamp = newCal.timeInMillis
                                                                if (endTimestamp < startTimestamp) {
                                                                    endTimestamp = startTimestamp + (1 * 24 * 60 * 60 * 1000L)
                                                                }
                                                            },
                                                            c.get(Calendar.YEAR),
                                                            c.get(Calendar.MONTH),
                                                            c.get(Calendar.DAY_OF_MONTH)
                                                        ).show()
                                                    },
                                                    shape = RoundedCornerShape(8.dp),
                                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                                    modifier = Modifier.height(32.dp)
                                                ) {
                                                    Text("Pick Start", fontSize = 11.sp)
                                                }
                                            }

                                            // Duration Badge
                                            val daysCount = (((endTimestamp - startTimestamp) / (1000 * 60 * 60 * 24)).toInt() + 1).coerceAtLeast(1)
                                            Surface(
                                                modifier = Modifier.fillMaxWidth(),
                                                shape = RoundedCornerShape(8.dp),
                                                color = Color(0xFFF3E8FF),
                                                border = BorderStroke(0.8.dp, Color(0xFFD8B4FE))
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                    horizontalArrangement = Arrangement.Center,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Icon(Icons.Filled.DateRange, contentDescription = null, tint = Color(0xFF7E22CE), modifier = Modifier.size(13.dp))
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(
                                                        text = "Duration: $daysCount Days Extended Schedule",
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color(0xFF7E22CE)
                                                    )
                                                }
                                            }

                                            // End Date
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Icon(Icons.Filled.Stop, contentDescription = null, tint = Color(0xFF9333EA), modifier = Modifier.size(14.dp))
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        Text("End Date:", fontSize = 11.sp, color = Color(0xFF7E22CE), fontFamily = FontFamily.Cursive, fontWeight = FontWeight.Bold)
                                                    }
                                                    Text(dateFormat.format(Date(endTimestamp)), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF581C87))
                                                }
                                                Button(
                                                    onClick = {
                                                        val c = Calendar.getInstance().apply { timeInMillis = endTimestamp }
                                                        DatePickerDialog(
                                                            context,
                                                            { _, y, m, d ->
                                                                val newCal = Calendar.getInstance().apply { set(y, m, d) }
                                                                if (newCal.timeInMillis < startTimestamp) {
                                                                    Toast.makeText(context, "End date adjusted to match or follow start date", Toast.LENGTH_SHORT).show()
                                                                    endTimestamp = startTimestamp
                                                                } else {
                                                                    endTimestamp = newCal.timeInMillis
                                                                }
                                                            },
                                                            c.get(Calendar.YEAR),
                                                            c.get(Calendar.MONTH),
                                                            c.get(Calendar.DAY_OF_MONTH)
                                                        ).show()
                                                    },
                                                    shape = RoundedCornerShape(8.dp),
                                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9333EA)),
                                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                                    modifier = Modifier.height(32.dp)
                                                ) {
                                                    Text("Pick End", fontSize = 11.sp)
                                                }
                                            }
                                        }
                                    } else {
                                        // Single Date selector
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text("Date:", fontSize = 11.sp, color = Color(0xFF0369A1), fontFamily = FontFamily.Cursive, fontWeight = FontWeight.Bold)
                                                Text(dateFormat.format(Date(startTimestamp)), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0C4A6E))
                                            }
                                            Button(
                                                onClick = {
                                                    val c = Calendar.getInstance().apply { timeInMillis = startTimestamp }
                                                    DatePickerDialog(
                                                        context,
                                                        { _, y, m, d ->
                                                            val newCal = Calendar.getInstance().apply { set(y, m, d) }
                                                            startTimestamp = newCal.timeInMillis
                                                        },
                                                        c.get(Calendar.YEAR),
                                                        c.get(Calendar.MONTH),
                                                        c.get(Calendar.DAY_OF_MONTH)
                                                    ).show()
                                                },
                                                shape = RoundedCornerShape(8.dp),
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                                modifier = Modifier.height(32.dp)
                                            ) {
                                                Text("Change Date", fontSize = 11.sp)
                                            }
                                        }
                                    }

                                    HorizontalDivider(color = Color(0xFFBAE6FD).copy(alpha = 0.5f))

                                    // Time Slot selector
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text("Time Slot:", fontSize = 11.sp, color = Color(0xFF0369A1), fontFamily = FontFamily.Cursive, fontWeight = FontWeight.Bold)
                                            Text(timeSlot, fontSize = 12.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0C4A6E))
                                        }

                                        Button(
                                            onClick = {
                                                TimePickerDialog(context, { _, h, m ->
                                                    val amPm = if (h >= 12) "PM" else "AM"
                                                    val displayH = if (h == 0) 12 else if (h > 12) h - 12 else h
                                                    timeSlot = String.format(Locale.getDefault(), "%02d:%02d %s", displayH, m, amPm)
                                                }, 9, 0, false).show()
                                            },
                                            shape = RoundedCornerShape(8.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                            modifier = Modifier.height(32.dp)
                                        ) {
                                            Text("Pick Time", fontSize = 11.sp)
                                        }
                                    }
                                }
                            }
                        }

                        // Content / Diary Notes Input with Microphone Voice Button
                        item {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                color = Color.White.copy(alpha = 0.95f),
                                border = BorderStroke(1.dp, Color(0xFFBAE6FD))
                            ) {
                                Box(modifier = Modifier.fillMaxWidth()) {
                                    OutlinedTextField(
                                        value = note,
                                        onValueChange = { note = it },
                                        label = { Text("Diary Notes / Agenda / Checklist", color = Color(0xFF0369A1), fontSize = 12.5.sp, fontFamily = FontFamily.Cursive) },
                                        placeholder = { Text("Write or speak your daily diary thoughts, schedule notes, steps or goals...", fontSize = 12.sp) },
                                        minLines = 3,
                                        maxLines = 6,
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(end = 38.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = Color(0xFF0284C7),
                                            unfocusedBorderColor = Color.Transparent,
                                            focusedContainerColor = Color.Transparent,
                                            unfocusedContainerColor = Color.Transparent
                                        )
                                    )

                                    IconButton(
                                        onClick = launchNoteVoiceInput,
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(top = 12.dp, end = 8.dp)
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFF3E8FF))
                                    ) {
                                        Icon(
                                            Icons.Filled.Mic,
                                            contentDescription = "Voice Dictate Notes",
                                            tint = Color(0xFF9333EA),
                                            modifier = Modifier.size(17.dp)
                                        )
                                    }
                                }
                            }
                        }

                        // Notification alert toggle
                        item {
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { notifyMe = !notifyMe },
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFFF0F9FF),
                                border = BorderStroke(1.dp, Color(0xFFBAE6FD))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 10.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = notifyMe,
                                        onCheckedChange = { notifyMe = it },
                                        colors = CheckboxDefaults.colors(checkedColor = Color(0xFF0284C7), checkmarkColor = Color.White)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Set Notification Alert Reminder", fontSize = 12.5.sp, color = Color(0xFF0369A1), fontFamily = FontFamily.Cursive, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        // Save Button with vibrant glow gradient
                        item {
                            Button(
                                onClick = {
                                    if (title.isBlank()) {
                                        Toast.makeText(context, "Please enter a title for the schedule", Toast.LENGTH_SHORT).show()
                                        return@Button
                                    }
                                    val finalEndTimestamp = if (isMultiDay) endTimestamp else null
                                    val scheduleToSave = initialSchedule?.copy(
                                        title = title.trim(),
                                        note = note.trim(),
                                        timestamp = startTimestamp,
                                        endTimestamp = finalEndTimestamp,
                                        isMultiDay = isMultiDay,
                                        recurrence = recurrence,
                                        timeSlot = timeSlot,
                                        category = category,
                                        notifyMe = notifyMe
                                    ) ?: DailyScheduleEntity(
                                        title = title.trim(),
                                        note = note.trim(),
                                        timestamp = startTimestamp,
                                        endTimestamp = finalEndTimestamp,
                                        isMultiDay = isMultiDay,
                                        recurrence = recurrence,
                                        timeSlot = timeSlot,
                                        category = category,
                                        notifyMe = notifyMe
                                    )
                                    onSave(scheduleToSave)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .shadow(6.dp, RoundedCornerShape(12.dp), spotColor = Color(0xFF0284C7)),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7))
                            ) {
                                Icon(
                                    Icons.Filled.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (initialSchedule == null) "Save Schedule & Diary" else "Update Schedule",
                                    color = Color.White,
                                    fontSize = 15.sp,
                                    fontFamily = FontFamily.Cursive,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ScheduleDetailDialog(
    schedule: DailyScheduleEntity,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onToggleComplete: () -> Unit,
    onSpeak: (() -> Unit)? = null
) {
    val dateFormat = remember { SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault()) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .wrapContentHeight()
                .padding(vertical = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .shadow(elevation = 20.dp, shape = RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp),
                color = Color.White,
                border = BorderStroke(1.5.dp, Color(0xFFBAE6FD))
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    Image(
                        painter = painterResource(id = R.drawable.app_icon),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .matchParentSize()
                            .clip(RoundedCornerShape(24.dp))
                    )
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(Color.White.copy(alpha = 0.92f))
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Top Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                if (schedule.isMultiDay) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = Color(0xFFF3E8FF),
                                        border = BorderStroke(1.dp, Color(0xFFD8B4FE))
                                    ) {
                                        Text(
                                            text = "MULTI-DAY",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF7E22CE),
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                                        )
                                    }
                                }

                                if (onSpeak != null) {
                                    IconButton(
                                        onClick = onSpeak,
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFF3E8FF))
                                    ) {
                                        Icon(
                                            Icons.Filled.VolumeUp,
                                            contentDescription = "Speak Schedule",
                                            tint = Color(0xFF9333EA),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }

                            IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                                Icon(Icons.Filled.Close, contentDescription = "Close", tint = Color(0xFF0369A1))
                            }
                        }

                        // Title
                        Text(
                            text = schedule.title,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Cursive,
                            color = Color(0xFF0369A1)
                        )

                        // Date and Time Info Container
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFF0F9FF),
                            border = BorderStroke(1.dp, Color(0xFFBAE6FD))
                        ) {
                            Column(
                                modifier = Modifier.padding(10.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                if (schedule.isMultiDay && schedule.endTimestamp != null) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Filled.PlayArrow, contentDescription = null, tint = Color(0xFF0284C7), modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Starts: ${dateFormat.format(Date(schedule.timestamp))}", fontSize = 12.sp, color = Color(0xFF0C4A6E), fontWeight = FontWeight.Medium)
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Filled.Stop, contentDescription = null, tint = Color(0xFF9333EA), modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Ends: ${dateFormat.format(Date(schedule.endTimestamp))}", fontSize = 12.sp, color = Color(0xFF581C87), fontWeight = FontWeight.Medium)
                                    }
                                } else {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Filled.CalendarMonth, contentDescription = null, tint = Color(0xFF0284C7), modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(dateFormat.format(Date(schedule.timestamp)), fontSize = 12.5.sp, color = Color(0xFF0C4A6E), fontWeight = FontWeight.Medium)
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.AccessTime, contentDescription = null, tint = Color(0xFF0284C7), modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(schedule.timeSlot, fontSize = 12.5.sp, color = Color(0xFF0C4A6E), fontWeight = FontWeight.Medium)
                                }
                            }
                        }

                        // Content / Diary notes
                        if (schedule.note.isNotBlank()) {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                color = Color.White.copy(alpha = 0.85f),
                                border = BorderStroke(0.8.dp, Color(0xFFE2E8F0))
                            ) {
                                Text(
                                    text = schedule.note,
                                    fontSize = 13.5.sp,
                                    color = Color(0xFF1E293B),
                                    lineHeight = 20.sp,
                                    modifier = Modifier.padding(10.dp)
                                )
                            }
                        }

                        // Action buttons (Complete, Edit, Delete)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = onToggleComplete,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, Color(0xFF0284C7))
                            ) {
                                Text(if (schedule.isCompleted) "Mark Pending" else "Mark Done ✓", color = Color(0xFF0284C7), fontSize = 12.sp)
                            }

                            Button(
                                onClick = onEdit,
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7))
                            ) {
                                Icon(Icons.Filled.Edit, contentDescription = "Edit", tint = Color.White, modifier = Modifier.size(16.dp))
                            }

                            IconButton(onClick = onDelete) {
                                Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = Color(0xFFEF4444))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DailyCalendarWeekStrip(
    selectedCalendar: Calendar,
    onDateSelected: (Calendar) -> Unit
) {
    val monthYearFormat = remember { SimpleDateFormat("MMMM yyyy", Locale.getDefault()) }
    val dayOfWeekFormat = remember { SimpleDateFormat("EEE", Locale.getDefault()) }
    val dayNumFormat = remember { SimpleDateFormat("d", Locale.getDefault()) }

    val daysOfWeek = remember(selectedCalendar) {
        val cal = selectedCalendar.clone() as Calendar
        cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
        val list = mutableListOf<Calendar>()
        for (i in 0 until 7) {
            list.add(cal.clone() as Calendar)
            cal.add(Calendar.DAY_OF_MONTH, 1)
        }
        list
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 2.dp)
    ) {
        // Month and Year Title
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "📅 ${monthYearFormat.format(selectedCalendar.time)}",
                fontSize = 14.5.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color.White.copy(alpha = 0.15f),
                border = BorderStroke(0.8.dp, Color.White.copy(alpha = 0.25f))
            ) {
                Text(
                    text = "Calendar Agenda",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFFFFD56B),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Week Days Strip
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            daysOfWeek.forEach { dayCal ->
                val isSelected = dayCal.get(Calendar.DAY_OF_YEAR) == selectedCalendar.get(Calendar.DAY_OF_YEAR) &&
                        dayCal.get(Calendar.YEAR) == selectedCalendar.get(Calendar.YEAR)

                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 2.dp)
                        .clickable { onDateSelected(dayCal) },
                    shape = RoundedCornerShape(10.dp),
                    color = if (isSelected) Color(0xFFFFD56B) else Color.White.copy(alpha = 0.12f),
                    border = BorderStroke(
                        1.dp,
                        if (isSelected) Color(0xFFFFE8A3) else Color.White.copy(alpha = 0.2f)
                    )
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(vertical = 5.dp)
                    ) {
                        Text(
                            text = dayOfWeekFormat.format(dayCal.time).uppercase(),
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Color(0xFF1B1A20) else Color.White.copy(alpha = 0.8f)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = dayNumFormat.format(dayCal.time),
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isSelected) Color(0xFF1B1A20) else Color.White
                        )
                    }
                }
            }
        }
    }
}
