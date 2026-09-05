package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.EventEntity
import com.example.ui.components.LedgerTopHeader
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventsScreen(
    events: List<EventEntity>,
    onAddEvent: (String, String, Boolean, Boolean, Long) -> Unit,
    onDeleteEvent: (EventEntity) -> Unit,
    onMenuClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { LedgerTopHeader(title = "Event Dates") },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Filled.Add, "Add Event")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (events.isEmpty()) {
                item {
                    Text("No events (birthdays, anniversaries) added yet.", style = MaterialTheme.typography.bodyLarge)
                }
            }
            items(events) { event ->
                EventItem(event = event, onDelete = { onDeleteEvent(event) })
            }
        }

        if (showAddDialog) {
            var title by remember { mutableStateOf("") }
            var note by remember { mutableStateOf("") }
            var notify by remember { mutableStateOf(true) }

            AlertDialog(
                onDismissRequest = { showAddDialog = false },
                title = { Text("Add Birthday / Anniversary") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = title,
                            onValueChange = { title = it },
                            label = { Text("Event Title") },
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = note,
                            onValueChange = { note = it },
                            label = { Text("Notes (optional)") }
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = notify, onCheckedChange = { notify = it })
                            Text("Enable Notification")
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        if (title.isNotBlank()) {
                            // Automatically setting includeYear = false as requested
                            onAddEvent(title, note, notify, false, System.currentTimeMillis())
                            showAddDialog = false
                        }
                    }) { Text("Save") }
                },
                dismissButton = {
                    TextButton(onClick = { showAddDialog = false }) { Text("Cancel") }
                }
            )
        }
    }
}

@Composable
fun EventItem(event: EventEntity, onDelete: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Filled.Cake, contentDescription = null, modifier = Modifier.size(32.dp), tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = event.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                if (event.locationOrNote.isNotBlank()) {
                    Text(event.locationOrNote, style = MaterialTheme.typography.bodyMedium)
                }
                
                // Exclude year format as requested
                val sdf = SimpleDateFormat("MMM dd", Locale.getDefault())
                Text("Date: ${sdf.format(Date(event.eventTimestamp))}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.secondary)
                
                if (event.notifyMe) {
                    Text("🔔 Notification Enabled", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.tertiary)
                }
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.Delete, contentDescription = "Delete Event")
            }
        }
    }
}
