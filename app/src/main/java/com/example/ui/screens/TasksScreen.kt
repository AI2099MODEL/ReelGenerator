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
import com.example.data.model.TaskEntity
import com.example.ui.components.LedgerTopHeader
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksScreen(
    tasks: List<TaskEntity>,
    onAddTask: (String, String, Boolean, Long?) -> Unit,
    onToggleComplete: (TaskEntity) -> Unit,
    onDeleteTask: (TaskEntity) -> Unit,
    onMenuClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { LedgerTopHeader(title = "Tasks to Remember") },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Filled.Add, "Add Task")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (tasks.isEmpty()) {
                item {
                    Text("No tasks added yet.", style = MaterialTheme.typography.bodyLarge)
                }
            }
            items(tasks) { task ->
                TaskItem(
                    task = task,
                    onToggleComplete = { onToggleComplete(task) },
                    onDelete = { onDeleteTask(task) }
                )
            }
        }

        if (showAddDialog) {
            var title by remember { mutableStateOf("") }
            var description by remember { mutableStateOf("") }
            var notify by remember { mutableStateOf(false) }
            // Always show year here
            val currentYear = Calendar.getInstance().get(Calendar.YEAR)
            var yearStr by remember { mutableStateOf(currentYear.toString()) }

            AlertDialog(
                onDismissRequest = { showAddDialog = false },
                title = { Text("Add Task") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = title,
                            onValueChange = { title = it },
                            label = { Text("Task Title") },
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = description,
                            onValueChange = { description = it },
                            label = { Text("Description") }
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = notify, onCheckedChange = { notify = it })
                            Text("Enable Notification")
                        }
                        OutlinedTextField(
                            value = yearStr,
                            onValueChange = { yearStr = it },
                            label = { Text("Year (Visible)") },
                            singleLine = true
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        if (title.isNotBlank()) {
                            onAddTask(title, description, notify, System.currentTimeMillis())
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
fun TaskItem(task: TaskEntity, onToggleComplete: () -> Unit, onDelete: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(checked = task.isCompleted, onCheckedChange = { onToggleComplete() })
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                if (task.description.isNotBlank()) {
                    Text(task.description, style = MaterialTheme.typography.bodyMedium)
                }
                if (task.scheduledTimestamp != null) {
                    val sdf = SimpleDateFormat("MMM dd, yyyy - hh:mm a", Locale.getDefault())
                    Text("Date: ${sdf.format(Date(task.scheduledTimestamp))}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                }
                if (task.notifyMe) {
                    Text("🔔 Notification Enabled", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.tertiary)
                }
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.Delete, contentDescription = "Delete Task")
            }
        }
    }
}
