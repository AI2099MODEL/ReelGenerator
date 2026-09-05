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
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import com.example.data.model.EventEntity
import com.example.ui.components.LedgerTopHeader
import com.example.ui.components.OrganizeTodayBrandBadge
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import com.example.R
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
        val nowMillis = System.currentTimeMillis()

        val baseModeItems = when (mode) {
            DateScreenMode.IMPORTANT_DATES -> {
                events.filter {
                    it.category.equals("Birthday", ignoreCase = true) ||
                    it.category.equals("Anniversary", ignoreCase = true) ||
                    (it.eventType == "IMPORTANT_DATE" && !it.category.equals("Coming Task", ignoreCase = true) && !it.category.equals("Daily Task", ignoreCase = true))
                }
            }
            DateScreenMode.REMIND_ME -> {
                events.filter {
                    it.eventType == "REMIND_ME" ||
                    (!it.category.equals("Birthday", ignoreCase = true) &&
                     !it.category.equals("Anniversary", ignoreCase = true) &&
                     it.eventType != "IMPORTANT_DATE")
                }
            }
        }

        baseModeItems.filter { item ->
            val isPast = item.eventTimestamp < nowMillis && !item.category.equals("Birthday", ignoreCase = true) && !item.category.equals("Anniversary", ignoreCase = true)
            !isPast
        }.sortedWith(
            compareBy<EventEntity> { it.isCompleted }
                .thenBy { it.eventTimestamp }
        )
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
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = Color(0xFFD81B60),
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier
                    .padding(bottom = 16.dp, end = 12.dp)
                    .size(40.dp)
                    .shadow(6.dp, CircleShape, ambientColor = Color(0xFFD81B60), spotColor = Color(0xFFD81B60))
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = mode.addButtonText,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        },
        floatingActionButtonPosition = FabPosition.End
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

    val dialogBackgroundBrush = Brush.verticalGradient(
        listOf(
            Color(0xFFFFFFFF),
            Color(0xFFF8FAFC),
            Color(0xFFEFF6FF)
        )
    )

    val blueHeaderGradient = Brush.horizontalGradient(
        listOf(
            Color(0xFF0284C7),
            Color(0xFF0369A1),
            Color(0xFF075985)
        )
    )

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
                    .shadow(
                        elevation = 20.dp,
                        shape = RoundedCornerShape(26.dp),
                        ambientColor = Color(0xFF0284C7).copy(alpha = 0.25f),
                        spotColor = Color(0xFF0284C7).copy(alpha = 0.35f)
                    ),
                shape = RoundedCornerShape(26.dp),
                color = Color.White,
                border = BorderStroke(1.5.dp, Color(0xFFBAE6FD))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(dialogBackgroundBrush)
                        .padding(horizontal = 18.dp, vertical = 18.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Dialog Header: Title with Cursive font & Close Button
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
                                            Brush.linearGradient(
                                                listOf(Color(0xFFE0F2FE), Color(0xFFBAE6FD))
                                            )
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (mode == DateScreenMode.IMPORTANT_DATES) Icons.Filled.Cake else Icons.Filled.NotificationsActive,
                                        contentDescription = null,
                                        tint = Color(0xFF0284C7),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = if (mode == DateScreenMode.IMPORTANT_DATES) "Add Date" else "Add Task",
                                    color = Color(0xFF0369A1),
                                    fontSize = 22.sp,
                                    fontFamily = FontFamily.Cursive,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFFE0F2FE),
                                    border = BorderStroke(1.dp, Color(0xFFBAE6FD))
                                ) {
                                    Text(
                                        text = if (mode == DateScreenMode.IMPORTANT_DATES) "Annual" else "Tasks",
                                        color = Color(0xFF0369A1),
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Cursive,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }

                                IconButton(
                                    onClick = onDismiss,
                                    modifier = Modifier.size(30.dp)
                                ) {
                                    Icon(
                                        Icons.Filled.Close,
                                        contentDescription = "Close",
                                        tint = Color(0xFF0369A1),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }

                        // Category Section
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "Category:",
                                color = Color(0xFF0369A1),
                                fontSize = 14.sp,
                                fontFamily = FontFamily.Cursive,
                                fontWeight = FontWeight.Bold
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                categories.forEach { cat ->
                                    val isSelected = selectedCategory == cat.title
                                    Surface(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(44.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .clickable { selectedCategory = cat.title },
                                        shape = RoundedCornerShape(12.dp),
                                        color = if (isSelected) Color.Transparent else Color(0xFFF0F9FF),
                                        border = BorderStroke(
                                            1.5.dp,
                                            if (isSelected) Color(0xFF0284C7) else Color(0xFFBAE6FD)
                                        )
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .then(
                                                    if (isSelected) Modifier.background(blueHeaderGradient)
                                                    else Modifier.background(Color(0xFFF0F9FF))
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 8.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.Center
                                            ) {
                                                Icon(
                                                    imageVector = cat.icon,
                                                    contentDescription = null,
                                                    tint = if (isSelected) Color.White else cat.color,
                                                    modifier = Modifier.size(17.dp)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = cat.title,
                                                    fontSize = 14.sp,
                                                    fontFamily = FontFamily.Cursive,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                    color = if (isSelected) Color.White else Color(0xFF0369A1)
                                                )
                                            }
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
                                    color = Color(0xFF0369A1),
                                    fontSize = 13.sp,
                                    fontFamily = FontFamily.Cursive
                                )
                            },
                            placeholder = {
                                Text(
                                    text = when (selectedCategory) {
                                        "Birthday" -> "e.g. Papa's Birthday, Sarah"
                                        "Anniversary" -> "e.g. Mom & Dad's Anniversary"
                                        else -> "e.g. Important reminder"
                                    },
                                    color = Color(0xFF64748B).copy(alpha = 0.6f),
                                    fontSize = 12.sp
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = if (selectedCategory == "Birthday") Icons.Filled.Cake else Icons.Filled.Favorite,
                                    contentDescription = null,
                                    tint = Color(0xFF0284C7),
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color(0xFF0C4A6E),
                                unfocusedTextColor = Color(0xFF0C4A6E),
                                focusedBorderColor = Color(0xFF0284C7),
                                unfocusedBorderColor = Color(0xFFBAE6FD),
                                focusedContainerColor = Color(0xFFF8FAFC),
                                unfocusedContainerColor = Color(0xFFF8FAFC)
                            )
                        )

                        // Selected Date & Time Controls
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            color = Color(0xFFF0F9FF),
                            border = BorderStroke(1.2.dp, Color(0xFFBAE6FD))
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
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
                                                .size(32.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFFE0F2FE)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                Icons.Filled.CalendarMonth,
                                                contentDescription = null,
                                                tint = Color(0xFF0284C7),
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(
                                                text = if (mode == DateScreenMode.IMPORTANT_DATES) "Date (Day & Month)" else "Selected Date",
                                                fontSize = 11.sp,
                                                color = Color(0xFF0369A1),
                                                fontFamily = FontFamily.Cursive,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = formattedSelectedDate,
                                                fontSize = 13.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF0C4A6E)
                                            )
                                        }
                                    }
                                    Surface(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable {
                                                showDatePickerDialog(context, pickedCalendar.value.timeInMillis) { newTs ->
                                                    val cal = Calendar.getInstance().apply { timeInMillis = newTs }
                                                    cal.set(Calendar.HOUR_OF_DAY, pickedCalendar.value.get(Calendar.HOUR_OF_DAY))
                                                    cal.set(Calendar.MINUTE, pickedCalendar.value.get(Calendar.MINUTE))
                                                    pickedCalendar.value = cal
                                                }
                                            },
                                        shape = RoundedCornerShape(8.dp),
                                        color = Color.Transparent,
                                        border = BorderStroke(1.dp, Color(0xFF0284C7))
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .background(blueHeaderGradient)
                                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Filled.CalendarToday, contentDescription = null, tint = Color.White, modifier = Modifier.size(13.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Change", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }

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
                                                .size(32.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFFE0F2FE)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                Icons.Filled.AccessTime,
                                                contentDescription = null,
                                                tint = Color(0xFF0284C7),
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(
                                                text = "Reminder Time",
                                                fontSize = 11.sp,
                                                color = Color(0xFF0369A1),
                                                fontFamily = FontFamily.Cursive,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = timeFormat.format(pickedCalendar.value.time),
                                                fontSize = 13.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF0C4A6E)
                                            )
                                        }
                                    }
                                    Surface(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
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
                                        shape = RoundedCornerShape(8.dp),
                                        color = Color.Transparent,
                                        border = BorderStroke(1.dp, Color(0xFF0284C7))
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .background(blueHeaderGradient)
                                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Filled.Schedule, contentDescription = null, tint = Color.White, modifier = Modifier.size(13.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Set Time", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                            }
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
                                    color = Color(0xFF0369A1),
                                    fontSize = 13.sp,
                                    fontFamily = FontFamily.Cursive
                                )
                            },
                            placeholder = {
                                Text(
                                    "e.g. Gift ideas, dinner plans, favorite cake",
                                    color = Color(0xFF64748B).copy(alpha = 0.6f),
                                    fontSize = 12.sp
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.AutoMirrored.Filled.Notes,
                                    contentDescription = null,
                                    tint = Color(0xFF0284C7),
                                    modifier = Modifier.size(17.dp)
                                )
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color(0xFF0C4A6E),
                                unfocusedTextColor = Color(0xFF0C4A6E),
                                focusedBorderColor = Color(0xFF0284C7),
                                unfocusedBorderColor = Color(0xFFBAE6FD),
                                focusedContainerColor = Color(0xFFF8FAFC),
                                unfocusedContainerColor = Color(0xFFF8FAFC)
                            )
                        )

                        // Notification Alert Card
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { notifyAlert = !notifyAlert },
                            shape = RoundedCornerShape(10.dp),
                            color = if (notifyAlert) Color(0xFFE0F2FE) else Color(0xFFF8FAFC),
                            border = BorderStroke(
                                1.dp,
                                if (notifyAlert) Color(0xFF0284C7) else Color(0xFFBAE6FD)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = notifyAlert,
                                    onCheckedChange = { notifyAlert = it },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = Color(0xFF0284C7),
                                        checkmarkColor = Color.White,
                                        uncheckedColor = Color(0xFF94A3B8)
                                    )
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text(
                                        text = if (mode == DateScreenMode.IMPORTANT_DATES) "Annual reminder alert" else "Task reminder alert",
                                        fontSize = 13.sp,
                                        fontFamily = FontFamily.Cursive,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF0369A1)
                                    )
                                    Text(
                                        text = if (mode == DateScreenMode.IMPORTANT_DATES) "Receive scheduled notification every year" else "Receive notification at the scheduled time",
                                        fontSize = 10.sp,
                                        color = Color(0xFF64748B)
                                    )
                                }
                            }
                        }

                        // Save Date Button in Vibrant Blue Gradient
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
                                .height(50.dp)
                                .shadow(
                                    elevation = 8.dp,
                                    shape = RoundedCornerShape(14.dp),
                                    ambientColor = Color(0xFF0284C7).copy(alpha = 0.35f),
                                    spotColor = Color(0xFF0284C7).copy(alpha = 0.5f)
                                ),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF0284C7),
                                contentColor = Color.White
                            )
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(blueHeaderGradient),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        Icons.Filled.Check,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp),
                                        tint = Color.White
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (mode == DateScreenMode.IMPORTANT_DATES) "Save Date" else "Save Task",
                                        color = Color.White,
                                        fontSize = 16.sp,
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
                elevation = 4.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = Color(0xFFFFD56B).copy(alpha = 0.35f),
                spotColor = Color(0xFFFFD700).copy(alpha = 0.4f)
            ),
        shape = RoundedCornerShape(16.dp),
        color = Color.White.copy(alpha = 0.92f),
        border = BorderStroke(1.2.dp, Color(0xFFFFD56B).copy(alpha = 0.85f))
    ) {
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Background Image: Organize Today App Icon
            Image(
                painter = painterResource(id = R.drawable.app_icon),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                alpha = 0.22f,
                modifier = Modifier.matchParentSize()
            )

            // Warm Glass Overlay for high contrast with Black fonts
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                Color.White.copy(alpha = 0.85f),
                                Color(0xFFFFF9E6).copy(alpha = 0.88f)
                            )
                        )
                    )
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 1. Organize Today App Icon Badge
                OrganizeTodayBrandBadge()

                Spacer(modifier = Modifier.width(10.dp))

                // 2. Content (Title, Subtitle, Date Row) in BLACK fonts
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = item.title,
                            fontSize = 15.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A), // Crisp BLACK font
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFFFFBE8),
                            border = BorderStroke(1.dp, Color(0xFFFFD56B))
                        ) {
                            Text(
                                text = badgeLabel,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFB45309),
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                            )
                        }
                    }

                    if (item.locationOrNote.isNotBlank()) {
                        Text(
                            text = item.locationOrNote,
                            fontSize = 12.sp,
                            color = Color(0xFF334155), // Black / dark grey font
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.CalendarToday,
                            contentDescription = "Date",
                            tint = Color(0xFFD97706),
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = formattedDate,
                            fontSize = 12.sp,
                            color = Color(0xFF0F172A), // Crisp BLACK font
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                // 3. Right side: Delete & Notification Icon
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    IconButton(
                        onClick = { onDelete() },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Delete,
                            contentDescription = "Delete",
                            tint = Color(0xFF475569),
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    if (onToggleComplete != null) {
                        IconButton(
                            onClick = onToggleComplete,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = if (item.notifyMe) Icons.Outlined.Notifications else Icons.Outlined.NotificationsOff,
                                contentDescription = "Notification",
                                tint = if (item.notifyMe) Color(0xFFD97706) else Color(0xFF94A3B8),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Custom Compose implementation of the "Organize Today" badge
 */
@Composable
private fun OrganizeTodayBrandBadge() {
    Surface(
        modifier = Modifier.size(40.dp),
        shape = RoundedCornerShape(10.dp),
        color = Color(0xFFFFFBE8),
        border = BorderStroke(1.dp, Color(0xFFFFE599)),
        shadowElevation = 2.dp
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.app_icon),
                contentDescription = "Organize Today",
                modifier = Modifier.size(26.dp)
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
