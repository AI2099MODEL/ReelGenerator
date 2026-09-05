#!/bin/bash
cat << 'INNER_EOF' > app/src/main/java/com/example/ui/screens/MainLedgerScreen.kt
package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.LedgerSection
import com.example.ui.LedgerViewModel
import com.example.ui.components.*
import kotlinx.coroutines.launch

@Composable
fun MainLedgerScreen(
    viewModel: LedgerViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    val currentSection by viewModel.selectedSection.collectAsStateWithLifecycle()
    val tasks by viewModel.tasks.collectAsStateWithLifecycle()
    val events by viewModel.events.collectAsStateWithLifecycle()
    val vaultDocs by viewModel.vaultDocuments.collectAsStateWithLifecycle()
    val globalSettings by viewModel.globalSettings.collectAsStateWithLifecycle()

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    var showGlobalSettingsDialog by remember { mutableStateOf(false) }

    if (showGlobalSettingsDialog) {
        GlobalSettingsDialog(
            settings = globalSettings,
            onDismiss = { showGlobalSettingsDialog = false },
            onSave = { updated ->
                viewModel.globalSettings.value = updated
                showGlobalSettingsDialog = false
            }
        )
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                ModalDrawerSheet(
                    drawerContainerColor = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.width(320.dp)
                ) {
                    LedgerSideMenuDrawer(
                        currentSection = currentSection,
                        onSectionSelected = { viewModel.setSection(it) },
                        onCloseDrawer = { coroutineScope.launch { drawerState.close() } },
                        globalSettings = globalSettings,
                        onOpenGlobalSettings = { showGlobalSettingsDialog = true }
                    )
                }
            }
        ) {
            BoxWithConstraints(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
                val isTablet = maxWidth >= 600.dp
                if (isTablet) {
                    Row(modifier = Modifier.fillMaxSize()) {
                        BinderNavigationRail(
                            currentSection = currentSection,
                            onSectionSelected = { viewModel.setSection(it) },
                            onMenuClick = { coroutineScope.launch { drawerState.open() } }
                        )
                        Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                            ScreenContent(
                                currentSection = currentSection,
                                viewModel = viewModel,
                                tasks = tasks,
                                events = events,
                                vaultDocs = vaultDocs,
                                globalSettings = globalSettings,
                                onOpenGlobalSettings = { showGlobalSettingsDialog = true },
                                onOpenDrawer = { coroutineScope.launch { drawerState.open() } }
                            )
                        }
                    }
                } else {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Scaffold(
                            bottomBar = {
                                BinderBottomNavigation(
                                    currentSection = currentSection,
                                    onSectionSelected = { viewModel.setSection(it) }
                                )
                            },
                            containerColor = Color.Transparent
                        ) { innerPadding ->
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(innerPadding)
                            ) {
                                ScreenContent(
                                    currentSection = currentSection,
                                    viewModel = viewModel,
                                    tasks = tasks,
                                    events = events,
                                    vaultDocs = vaultDocs,
                                    globalSettings = globalSettings,
                                    onOpenGlobalSettings = { showGlobalSettingsDialog = true },
                                    onOpenDrawer = { coroutineScope.launch { drawerState.open() } }
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
private fun ScreenContent(
    currentSection: LedgerSection,
    viewModel: LedgerViewModel,
    tasks: List<com.example.data.model.TaskEntity>,
    events: List<com.example.data.model.EventEntity>,
    vaultDocs: List<com.example.data.model.VaultDocumentEntity>,
    globalSettings: com.example.ui.GlobalSettingsState,
    onOpenGlobalSettings: () -> Unit,
    onOpenDrawer: () -> Unit
) {
    val context = LocalContext.current
    Crossfade(targetState = currentSection, modifier = Modifier.fillMaxSize(), label = "ledger_section_crossfade") { section ->
        when (section) {
            LedgerSection.HOME -> {
                HomeScreen(
                    events = events,
                    tasks = tasks,
                    vaultDocs = vaultDocs,
                    onNavigateToSection = { viewModel.setSection(it) },
                    onMenuClick = onOpenDrawer
                )
            }
            LedgerSection.TASKS -> {
                TasksScreen(
                    tasks = tasks,
                    onAddTask = { title, description, notify, scheduledTime -> 
                        val task = com.example.data.model.TaskEntity(
                            title = title,
                            description = description,
                            notifyMe = notify,
                            scheduledTimestamp = scheduledTime,
                            category = "General"
                        )
                        viewModel.addTask(task)
                    },
                    onToggleComplete = { viewModel.toggleTaskComplete(it) },
                    onDeleteTask = { viewModel.deleteTask(it) },
                    onMenuClick = onOpenDrawer
                )
            }
            LedgerSection.EVENTS -> {
                EventsScreen(
                    events = events,
                    onAddEvent = { title, location, notify, includeYear, eventTime ->
                        viewModel.addEvent(
                            title = title,
                            locationOrNote = location,
                            eventTimestamp = eventTime,
                            notifyMe = notify,
                            includeYear = includeYear
                        )
                    },
                    onDeleteEvent = { viewModel.deleteEvent(it) },
                    onMenuClick = onOpenDrawer
                )
            }
            LedgerSection.IMAGES -> {
                ImageStudioScreen(
                    onMenuClick = onOpenDrawer,
                    globalSettings = globalSettings,
                    onOpenGlobalSettings = onOpenGlobalSettings
                )
            }
            LedgerSection.VAULT -> {
                VaultScreen(
                    vaultDocs = vaultDocs,
                    onAddDocument = { title, filename, uriStr, type, category, bytes ->
                        viewModel.addVaultDocument(title, filename, uriStr, type, category, bytes)
                    },
                    onDeleteDocument = { viewModel.deleteVaultDocument(it) },
                    onMenuClick = onOpenDrawer
                )
            }
        }
    }
}
INNER_EOF
