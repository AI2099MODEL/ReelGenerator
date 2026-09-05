package com.example.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.widget.Toast
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
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
    val addButtonText: String,
    val defaultEventType: String
) {
    IMPORTANT_DATES(
        title = "Important Dates",
        subtitle = "Birthdays & Anniversaries",
        addButtonText = "+ Add Date",
        defaultEventType = "IMPORTANT_DATE"
    ),
    REMIND_ME(
        title = "Remind Me",
        subtitle = "Coming Tasks & Daily Reminders",
        addButtonText = "+ Add Task",
        defaultEventType = "REMIND_ME"
    )
}

data class CategoryOption(
    val title: String,
    val icon: ImageVector,
    val color: Color
)

// -------------------------------------------------------------------------------------------------
// GOLDEN LUXURY & SUNSET THEME PALETTE (MATCHING TEXT TO IMAGE STUDIO)
// -------------------------------------------------------------------------------------------------
private val GoldHighlight = Color(0xFFFFF8D6)
private val GoldLight = Color(0xFFFFE082)
private val GoldPrimary = Color(0xFFFFD700)
private val GoldAccent = Color(0xFFD4AF37)
private val GoldDark = Color(0xFFAA771C)
private val GoldDeep = Color(0xFF4A3206)

private val MetallicGoldBrush = Brush.linearGradient(
    listOf(
        Color(0xFFFFDF73),
        Color(0xFFD4AF37),
        Color(0xFFFFF5B8),
        Color(0xFFAA771C),
        Color(0xFFFFE57F),
        Color(0xFFC59B27),
        Color(0xFF8C6212)
    )
)

private val SunsetPhoneWallpaperGradient = Brush.verticalGradient(
    listOf(
        Color(0xFF131628),
        Color(0xFF1C1F38),
        Color(0xFF2E2644),
        Color(0xFF4E2D4E),
        Color(0xFF783955),
        Color(0xFFA64A4E),
        Color(0xFFCD6742),
        Color(0xFFE8914F),
        Color(0xFF382230)
    )
)

/**
 * Clean main page with items list and a Floating Action Button (+ Add Date / + Add Task)
 * that opens the Golden Phone Mockup dialog for Data Entry.
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
                // Keep strictly only Birthday and Anniversary
                events.filter {
                    it.category.equals("Birthday", ignoreCase = true) ||
                    it.category.equals("Anniversary", ignoreCase = true) ||
                    (it.eventType == "IMPORTANT_DATE" && !it.category.equals("Coming Task", ignoreCase = true) && !it.category.equals("Daily Task", ignoreCase = true))
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
                onMenuClick = onMenuClick,
                actionIcon = if (mode == DateScreenMode.IMPORTANT_DATES) Icons.Filled.Cake else Icons.Filled.NotificationsActive,
                onActionClick = { showAddDialog = true }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = GoldPrimary,
                contentColor = Color(0xFF241400),
                shape = RoundedCornerShape(16.dp),
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 8.dp),
                modifier = Modifier.padding(bottom = 12.dp, end = 4.dp)
            ) {
                Icon(
                    Icons.Filled.Add,
                    contentDescription = "Add",
                    tint = Color(0xFF241400),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = mode.addButtonText,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color(0xFF241400)
                )
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 14.dp),
            contentPadding = PaddingValues(top = 8.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (displayItems.isNotEmpty()) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "✨ Saved ${mode.title} (${displayItems.size})",
                            color = GoldHighlight,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = GoldDeep.copy(alpha = 0.8f),
                            border = BorderStroke(0.8.dp, GoldAccent)
                        ) {
                            Text(
                                text = "${displayItems.size} ${if (displayItems.size == 1) "entry" else "entries"}",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                color = GoldLight,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                items(displayItems, key = { it.id }) { item ->
                    ItemCardGold(
                        item = item,
                        mode = mode,
                        onToggleComplete = { onToggleComplete?.invoke(item) },
                        onDelete = { eventToDelete = item }
                    )
                }
            }
        }
    }

    // ---------------------------------------------------------------------------------------------
    // DATA ENTRY IN GOLDEN PHONE MOCKUP WITH SUNSET THEME (OPENED VIA ADD BUTTON)
    // ---------------------------------------------------------------------------------------------
    if (showAddDialog) {
        GoldenDataEntryDialog(
            mode = mode,
            onDismiss = { showAddDialog = false },
            onSave = { title, note, notify, includeYear, timestamp, category ->
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
 * Data Entry Form inside the Golden Mobile Phone Mockup with Sunset Wallpaper
 * (No Calendar widget, Strictly Birthday & Anniversary for Important Dates, No Year displayed)
 */
@Composable
private fun GoldenDataEntryDialog(
    mode: DateScreenMode,
    onDismiss: () -> Unit,
    onSave: (
        title: String,
        note: String,
        notify: Boolean,
        includeYear: Boolean,
        timestamp: Long,
        category: String
    ) -> Unit
) {
    val context = LocalContext.current
    var selectedCategory by remember(mode) {
        mutableStateOf(if (mode == DateScreenMode.IMPORTANT_DATES) "Birthday" else "Coming Task")
    }
    var titleInput by remember { mutableStateOf("") }
    var noteInput by remember { mutableStateOf("") }
    var notifyAlert by remember { mutableStateOf(true) }

    // For Important Dates: Year is NEVER included
    val includeYearFlag = mode != DateScreenMode.IMPORTANT_DATES

    val pickedCalendar = remember {
        mutableStateOf(Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 9)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        })
    }

    // Strictly Birthday and Anniversary for Important Dates
    val categories = remember(mode) {
        if (mode == DateScreenMode.IMPORTANT_DATES) {
            listOf(
                CategoryOption("Birthday", Icons.Filled.Cake, Color(0xFFF43F5E)),
                CategoryOption("Anniversary", Icons.Filled.Favorite, Color(0xFFEC4899))
            )
        } else {
            listOf(
                CategoryOption("Coming Task", Icons.Filled.Schedule, Color(0xFF38BDF8)),
                CategoryOption("Daily Task", Icons.Filled.CheckCircle, Color(0xFF34D399)),
                CategoryOption("Reminder", Icons.Filled.NotificationsActive, Color(0xFFFBBF24))
            )
        }
    }

    // Date formatting WITHOUT YEAR for Important Dates
    val dateFormatNoYear = remember { SimpleDateFormat("EEEE, MMMM d", Locale.getDefault()) }
    val dateFormatWithYear = remember { SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault()) }
    val timeFormat = remember { SimpleDateFormat("hh:mm a", Locale.getDefault()) }

    val formattedSelectedDate = remember(pickedCalendar.value.timeInMillis, mode) {
        if (mode == DateScreenMode.IMPORTANT_DATES) {
            dateFormatNoYear.format(pickedCalendar.value.time)
        } else {
            dateFormatWithYear.format(pickedCalendar.value.time)
        }
    }

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
            // Outer Phone Chassis with Metallic Golden Bezel
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .shadow(
                        elevation = 24.dp,
                        shape = RoundedCornerShape(36.dp),
                        ambientColor = GoldAccent.copy(alpha = 0.5f),
                        spotColor = GoldPrimary.copy(alpha = 0.6f)
                    ),
                shape = RoundedCornerShape(36.dp),
                color = Color(0xFF0F0F14),
                border = BorderStroke(5.dp, MetallicGoldBrush)
            ) {
                // Inner Screen Bezel & Wallpaper Screen
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .padding(3.dp)
                        .clip(RoundedCornerShape(32.dp))
                        .background(SunsetPhoneWallpaperGradient)
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Top Speaker / Dynamic Island Notch with Close Button
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 4.dp, vertical = 2.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "9:41",
                                    color = GoldLight,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                // Notch pill
                                Box(
                                    modifier = Modifier
                                        .width(70.dp)
                                        .height(16.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color(0xFF070709)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(5.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFF1E293B))
                                        )
                                        Box(
                                            modifier = Modifier
                                                .width(18.dp)
                                                .height(3.dp)
                                                .clip(RoundedCornerShape(2.dp))
                                                .background(Color(0xFF334155))
                                        )
                                    }
                                }
                                IconButton(
                                    onClick = onDismiss,
                                    modifier = Modifier.size(26.dp)
                                ) {
                                    Icon(
                                        Icons.Filled.Close,
                                        contentDescription = "Close",
                                        tint = GoldLight,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }

                        // Golden Header Doodles & Typography
                        item {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Text(text = "☀️", fontSize = 22.sp)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "✦ ✨",
                                        color = GoldPrimary,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Text(
                                    text = if (mode == DateScreenMode.IMPORTANT_DATES) "Special Dates" else "Organize Today",
                                    color = GoldPrimary,
                                    fontSize = 24.sp,
                                    fontStyle = FontStyle.Italic,
                                    fontFamily = FontFamily.Cursive,
                                    textAlign = TextAlign.Center
                                )

                                Text(
                                    text = "♡",
                                    color = GoldLight,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                Text(
                                    text = if (mode == DateScreenMode.IMPORTANT_DATES)
                                        "BIRTHDAYS & ANNIVERSARIES"
                                    else
                                        "A MORE ORGANIZED YOU • A BRIGHTER TOMORROW",
                                    color = GoldLight.copy(alpha = 0.85f),
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        // Data Entry Container inside the phone screen
                        item {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(18.dp),
                                color = Color(0xEE1E182A),
                                border = BorderStroke(1.2.dp, GoldAccent.copy(alpha = 0.65f)),
                                shadowElevation = 6.dp
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    // Card Title
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(28.dp)
                                                    .clip(CircleShape)
                                                    .background(GoldPrimary.copy(alpha = 0.2f)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = if (mode == DateScreenMode.IMPORTANT_DATES) Icons.Filled.Cake else Icons.Filled.NotificationsActive,
                                                    contentDescription = null,
                                                    tint = GoldPrimary,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = if (mode == DateScreenMode.IMPORTANT_DATES) "Add Date" else "Add Task",
                                                color = GoldHighlight,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }

                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = GoldDeep.copy(alpha = 0.8f),
                                            border = BorderStroke(0.6.dp, GoldAccent)
                                        ) {
                                            Text(
                                                text = if (mode == DateScreenMode.IMPORTANT_DATES) "Annual" else "Tasks",
                                                color = GoldLight,
                                                fontSize = 9.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }

                                    // Category Selection Chips (Strictly Birthday & Anniversary for Important Dates)
                                    Column(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(
                                            text = "Category:",
                                            color = GoldLight,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            categories.forEach { cat ->
                                                val isSelected = selectedCategory == cat.title
                                                Surface(
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .height(40.dp)
                                                        .clip(RoundedCornerShape(10.dp))
                                                        .clickable { selectedCategory = cat.title },
                                                    shape = RoundedCornerShape(10.dp),
                                                    color = if (isSelected) GoldPrimary else Color(0xFF13101E),
                                                    border = BorderStroke(
                                                        1.2.dp,
                                                        if (isSelected) GoldHighlight else GoldAccent.copy(alpha = 0.35f)
                                                    )
                                                ) {
                                                    Row(
                                                        modifier = Modifier
                                                            .fillMaxSize()
                                                            .padding(horizontal = 8.dp),
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.Center
                                                    ) {
                                                        Icon(
                                                            imageVector = cat.icon,
                                                            contentDescription = null,
                                                            tint = if (isSelected) Color(0xFF241400) else cat.color,
                                                            modifier = Modifier.size(16.dp)
                                                        )
                                                        Spacer(modifier = Modifier.width(6.dp))
                                                        Text(
                                                            text = cat.title,
                                                            fontSize = 12.sp,
                                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                            color = if (isSelected) Color(0xFF241400) else GoldLight
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    // Title Input Field
                                    OutlinedTextField(
                                        value = titleInput,
                                        onValueChange = { titleInput = it },
                                        label = {
                                            Text(
                                                text = if (mode == DateScreenMode.IMPORTANT_DATES)
                                                    (if (selectedCategory == "Birthday") "Name / Person's Birthday *" else "Couple / Anniversary Title *")
                                                else
                                                    "Task / Reminder Title *",
                                                color = GoldLight.copy(alpha = 0.8f),
                                                fontSize = 11.5.sp
                                            )
                                        },
                                        placeholder = {
                                            Text(
                                                text = when (selectedCategory) {
                                                    "Birthday" -> "e.g. Papa's Birthday, Sarah"
                                                    "Anniversary" -> "e.g. Mom & Dad's Anniversary"
                                                    else -> "e.g. Important reminder"
                                                },
                                                color = GoldLight.copy(alpha = 0.45f),
                                                fontSize = 11.sp
                                            )
                                        },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = if (selectedCategory == "Birthday") Icons.Filled.Cake else Icons.Filled.Favorite,
                                                contentDescription = null,
                                                tint = GoldPrimary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        },
                                        singleLine = true,
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = GoldHighlight,
                                            unfocusedTextColor = GoldLight,
                                            focusedBorderColor = GoldPrimary,
                                            unfocusedBorderColor = GoldAccent.copy(alpha = 0.5f),
                                            focusedContainerColor = Color(0xFF13101E),
                                            unfocusedContainerColor = Color(0xFF13101E)
                                        )
                                    )

                                    // Selected Date & Time Controls (No Year displayed)
                                    Surface(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(10.dp),
                                        color = Color(0xFF13101E),
                                        border = BorderStroke(1.dp, GoldAccent.copy(alpha = 0.4f))
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp)) {
                                            // Date Row (Showing Month & Day only, no year)
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
                                                            .size(28.dp)
                                                            .clip(CircleShape)
                                                            .background(GoldPrimary.copy(alpha = 0.2f)),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Icon(
                                                            Icons.Filled.CalendarMonth,
                                                            contentDescription = null,
                                                            tint = GoldPrimary,
                                                            modifier = Modifier.size(16.dp)
                                                        )
                                                    }
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Column {
                                                        Text(
                                                            text = if (mode == DateScreenMode.IMPORTANT_DATES) "Date (Day & Month)" else "Selected Date",
                                                            fontSize = 9.sp,
                                                            color = GoldLight.copy(alpha = 0.7f),
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                        Text(
                                                            text = formattedSelectedDate,
                                                            fontSize = 12.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = GoldHighlight
                                                        )
                                                    }
                                                }
                                                Surface(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(6.dp))
                                                        .clickable {
                                                            showDatePickerDialog(context, pickedCalendar.value.timeInMillis) { newTs ->
                                                                val cal = Calendar.getInstance().apply { timeInMillis = newTs }
                                                                cal.set(Calendar.HOUR_OF_DAY, pickedCalendar.value.get(Calendar.HOUR_OF_DAY))
                                                                cal.set(Calendar.MINUTE, pickedCalendar.value.get(Calendar.MINUTE))
                                                                pickedCalendar.value = cal
                                                            }
                                                        },
                                                    shape = RoundedCornerShape(6.dp),
                                                    color = GoldDeep,
                                                    border = BorderStroke(0.6.dp, GoldAccent)
                                                ) {
                                                    Row(
                                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Icon(Icons.Filled.CalendarToday, contentDescription = null, tint = GoldLight, modifier = Modifier.size(11.dp))
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        Text("Change", color = GoldLight, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                    }
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(8.dp))

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
                                                            .size(28.dp)
                                                            .clip(CircleShape)
                                                            .background(GoldAccent.copy(alpha = 0.2f)),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Icon(
                                                            Icons.Filled.AccessTime,
                                                            contentDescription = null,
                                                            tint = GoldLight,
                                                            modifier = Modifier.size(16.dp)
                                                        )
                                                    }
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Column {
                                                        Text(
                                                            text = "Reminder Time",
                                                            fontSize = 9.sp,
                                                            color = GoldLight.copy(alpha = 0.7f),
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                        Text(
                                                            text = timeFormat.format(pickedCalendar.value.time),
                                                            fontSize = 12.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = GoldHighlight
                                                        )
                                                    }
                                                }
                                                Surface(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(6.dp))
                                                        .clickable {
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
                                                    shape = RoundedCornerShape(6.dp),
                                                    color = GoldDeep,
                                                    border = BorderStroke(0.6.dp, GoldAccent)
                                                ) {
                                                    Row(
                                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Icon(Icons.Filled.Schedule, contentDescription = null, tint = GoldLight, modifier = Modifier.size(11.dp))
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        Text("Set Time", color = GoldLight, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    // Notes / Details Input
                                    OutlinedTextField(
                                        value = noteInput,
                                        onValueChange = { noteInput = it },
                                        label = {
                                            Text(
                                                "Notes / Gift Ideas (Optional)",
                                                color = GoldLight.copy(alpha = 0.7f),
                                                fontSize = 11.sp
                                            )
                                        },
                                        placeholder = {
                                            Text(
                                                "e.g. Gift ideas, dinner plans, favorite cake",
                                                color = GoldLight.copy(alpha = 0.35f),
                                                fontSize = 10.5.sp
                                            )
                                        },
                                        leadingIcon = {
                                            Icon(
                                                Icons.AutoMirrored.Filled.Notes,
                                                contentDescription = null,
                                                tint = GoldLight.copy(alpha = 0.7f),
                                                modifier = Modifier.size(15.dp)
                                            )
                                        },
                                        singleLine = true,
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = GoldHighlight,
                                            unfocusedTextColor = GoldLight,
                                            focusedBorderColor = GoldPrimary,
                                            unfocusedBorderColor = GoldAccent.copy(alpha = 0.5f),
                                            focusedContainerColor = Color(0xFF13101E),
                                            unfocusedContainerColor = Color(0xFF13101E)
                                        )
                                    )

                                    // Notification Alert Card
                                    Surface(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable { notifyAlert = !notifyAlert },
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (notifyAlert) GoldDeep.copy(alpha = 0.6f) else Color(0xFF13101E),
                                        border = BorderStroke(
                                            1.dp,
                                            if (notifyAlert) GoldAccent else GoldAccent.copy(alpha = 0.3f)
                                        )
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 8.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Checkbox(
                                                checked = notifyAlert,
                                                onCheckedChange = { notifyAlert = it },
                                                colors = CheckboxDefaults.colors(
                                                    checkedColor = GoldPrimary,
                                                    checkmarkColor = Color(0xFF241400),
                                                    uncheckedColor = GoldLight.copy(alpha = 0.5f)
                                                )
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Column {
                                                Text(
                                                    text = "Annual reminder alert",
                                                    fontSize = 10.5.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = GoldHighlight
                                                )
                                                Text(
                                                    text = "Receive scheduled notification every year",
                                                    fontSize = 9.sp,
                                                    color = GoldLight.copy(alpha = 0.7f)
                                                )
                                            }
                                        }
                                    }

                                    // Save Date Button in Gold Gradient
                                    Button(
                                        onClick = {
                                            if (titleInput.isBlank()) {
                                                Toast.makeText(context, "Please enter a title for the entry", Toast.LENGTH_SHORT).show()
                                                return@Button
                                            }
                                            onSave(
                                                titleInput.trim(),
                                                noteInput.trim(),
                                                notifyAlert,
                                                includeYearFlag,
                                                pickedCalendar.value.timeInMillis,
                                                selectedCategory
                                            )
                                            Toast.makeText(context, "Saved $selectedCategory!", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(46.dp)
                                            .shadow(
                                                elevation = 6.dp,
                                                shape = RoundedCornerShape(10.dp),
                                                ambientColor = GoldPrimary,
                                                spotColor = GoldHighlight
                                            ),
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = GoldPrimary,
                                            contentColor = Color(0xFF241400)
                                        )
                                    ) {
                                        Icon(
                                            Icons.Filled.Check,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp),
                                            tint = Color(0xFF241400)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = if (mode == DateScreenMode.IMPORTANT_DATES) "Save Date" else "Save Task",
                                            color = Color(0xFF241400),
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }
        }
    }
}

/**
 * Item Card in exact style of the user screenshot:
 * - Rounded pill card with Sunset Landscape background gradient
 * - "Organize Today" sun & leaf brand badge on the left
 * - Title with Gold pill badge ("Today ✨", "Tomorrow ✨", countdown)
 * - Subtitle / note ("A more organized you awaits")
 * - Date & Time row with Calendar icon ("September 5 • 09:00 AM")
 * - "Better Days Ahead ♡" cursive script & Notification Bell on the right
 */
@Composable
private fun ItemCardGold(
    item: EventEntity,
    mode: DateScreenMode,
    onToggleComplete: () -> Unit,
    onDelete: () -> Unit
) {
    // Format without year for Important Dates
    val dateFormatNoYear = remember { SimpleDateFormat("MMMM d", Locale.getDefault()) }
    val dateFormatWithYear = remember { SimpleDateFormat("MMMM d, yyyy", Locale.getDefault()) }
    val timeFormat = remember { SimpleDateFormat("hh:mm a", Locale.getDefault()) }

    val formattedDate = remember(item.eventTimestamp, mode) {
        val d = Date(item.eventTimestamp)
        if (mode == DateScreenMode.IMPORTANT_DATES) {
            "${dateFormatNoYear.format(d)} • ${timeFormat.format(d)}"
        } else {
            "${dateFormatWithYear.format(d)} • ${timeFormat.format(d)}"
        }
    }

    // Calculate days until upcoming occurrence
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
            if (mode == DateScreenMode.IMPORTANT_DATES) {
                set(Calendar.YEAR, nowCal.get(Calendar.YEAR))
                if (before(nowCal)) {
                    add(Calendar.YEAR, 1)
                }
            }
        }
        val diffMillis = targetCal.timeInMillis - nowCal.timeInMillis
        (diffMillis / (1000 * 60 * 60 * 24)).toInt()
    }

    val badgeLabel = when {
        daysUntil == 0 -> "Today ✨"
        daysUntil == 1 -> "Tomorrow ✨"
        daysUntil in 2..365 -> "In $daysUntil days ✨"
        daysUntil < 0 -> "${-daysUntil}d ago"
        else -> "Special ✨"
    }

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
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(26.dp),
                ambientColor = Color(0xFFD47C3B).copy(alpha = 0.35f),
                spotColor = Color(0xFFFFD700).copy(alpha = 0.4f)
            ),
        shape = RoundedCornerShape(26.dp),
        color = Color(0xFF1B1A20),
        border = BorderStroke(1.2.dp, Color(0xFF8C5832).copy(alpha = 0.7f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(cardBackgroundGradient)
        ) {
            // Background Canvas overlay for sunset rays and foliage bokeh
            androidx.compose.foundation.Canvas(
                modifier = Modifier
                    .matchParentSize()
            ) {
                val width = size.width
                val height = size.height

                // Glowing sun burst on the right side
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFFFFFDE0),
                            Color(0xFFFFDF73),
                            Color(0xFFFF9E3D).copy(alpha = 0.6f),
                            Color.Transparent
                        ),
                        center = androidx.compose.ui.geometry.Offset(width * 0.82f, height * 0.38f),
                        radius = height * 0.9f
                    )
                )

                // Subtle foliage silhouette / leaf shapes on right side
                drawCircle(
                    color = Color(0x33101E14),
                    center = androidx.compose.ui.geometry.Offset(width * 0.72f, height * 0.65f),
                    radius = height * 0.45f
                )
            }

            // Foreground Content Layout
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 1. Organize Today Sun & Foliage Logo Badge
                OrganizeTodayBrandBadge()

                Spacer(modifier = Modifier.width(14.dp))

                // 2. Middle Content (Title + Pill, Subtitle, Date Row)
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    // Title + Today Badge Row
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = item.title,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFFFFFF),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        // Golden pill badge (e.g. Today ✨)
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = Color(0xFF7A4A0A),
                            border = BorderStroke(1.dp, Color(0xFFFFD56B))
                        ) {
                            Text(
                                text = badgeLabel,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFFF4D0),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                            )
                        }
                    }

                    // Subtitle / Note
                    Text(
                        text = if (item.locationOrNote.isNotBlank()) item.locationOrNote else "A more organized you awaits",
                        fontSize = 12.5.sp,
                        color = Color(0xFFD6D1DF),
                        fontWeight = FontWeight.Normal,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    // Date & Time Row with Calendar icon
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.CalendarToday,
                            contentDescription = "Date",
                            tint = Color(0xFFF1EDE6),
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = formattedDate,
                            fontSize = 12.5.sp,
                            color = Color(0xFFF1EDE6),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // 3. Right Side: "Better Days Ahead ♡" in golden cursive & Notification Bell
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Better",
                            color = Color(0xFFFFEAA7),
                            fontSize = 13.sp,
                            fontStyle = FontStyle.Italic,
                            fontFamily = FontFamily.Cursive,
                            fontWeight = FontWeight.Bold,
                            lineHeight = 13.sp
                        )
                        Text(
                            text = "Days",
                            color = Color(0xFFFFEAA7),
                            fontSize = 13.sp,
                            fontStyle = FontStyle.Italic,
                            fontFamily = FontFamily.Cursive,
                            fontWeight = FontWeight.Bold,
                            lineHeight = 13.sp
                        )
                        Text(
                            text = "Ahead",
                            color = Color(0xFFFFEAA7),
                            fontSize = 13.sp,
                            fontStyle = FontStyle.Italic,
                            fontFamily = FontFamily.Cursive,
                            fontWeight = FontWeight.Bold,
                            lineHeight = 13.sp
                        )
                        Text(
                            text = "♡",
                            color = Color(0xFFFFDF73),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Bell / Action Icons
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        IconButton(
                            onClick = onToggleComplete,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = if (item.notifyMe) Icons.Outlined.Notifications else Icons.Outlined.NotificationsOff,
                                contentDescription = "Notification",
                                tint = if (item.notifyMe) Color(0xFFFFFFFF) else Color(0x99FFFFFF),
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        IconButton(
                            onClick = onDelete,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Delete,
                                contentDescription = "Delete",
                                tint = Color(0xCCFFFFFF),
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Custom Compose implementation of the "Organize Today" badge with rising sun & foliage
 */
@Composable
private fun OrganizeTodayBrandBadge() {
    Surface(
        modifier = Modifier.size(62.dp),
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFFFFFBE8),
        border = BorderStroke(1.dp, Color(0xFFFFE599)),
        shadowElevation = 4.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFFFFFFFF),
                            Color(0xFFFFF9E4),
                            Color(0xFFFFEFBE)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 4.dp, vertical = 3.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Rising Sun with rays icon
                Text(
                    text = "☀️",
                    fontSize = 16.sp,
                    lineHeight = 16.sp,
                    textAlign = TextAlign.Center
                )

                // "Organize" in dark pine green
                Text(
                    text = "Organize",
                    fontSize = 8.5.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF0F3832),
                    lineHeight = 9.sp,
                    textAlign = TextAlign.Center
                )

                // "Today" in golden cursive
                Text(
                    text = "Today",
                    fontSize = 11.5.sp,
                    fontStyle = FontStyle.Italic,
                    fontFamily = FontFamily.Cursive,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF8A531C),
                    lineHeight = 12.sp,
                    textAlign = TextAlign.Center
                )
            }

            // Decorative green leaf accent at bottom-left corner
            Text(
                text = "🌿",
                fontSize = 10.sp,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 2.dp, bottom = 1.dp)
            )
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
