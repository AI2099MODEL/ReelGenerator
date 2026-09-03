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
import com.example.ui.components.AmbientWaterFlowBackground
import com.example.ui.components.GlobalSettingsDialog
import com.example.ui.components.LedgerBinderBottomBar
import com.example.ui.components.LedgerBinderNavRail
import com.example.ui.components.LedgerSideMenuDrawer
import kotlinx.coroutines.launch

@Composable
fun MainLedgerScreen(
    viewModel: LedgerViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    val currentSection by viewModel.selectedSection.collectAsStateWithLifecycle()
    val globalSettings by viewModel.globalSettings.collectAsStateWithLifecycle()
    val networkState by viewModel.networkState.collectAsStateWithLifecycle()
    val pendingSyncCount by viewModel.pendingSyncCount.collectAsStateWithLifecycle()
    val chatThreads by viewModel.chatThreads.collectAsStateWithLifecycle()
    val activeChatThreadKey by viewModel.activeChatThreadKey.collectAsStateWithLifecycle()
    val activeThreadMessages by viewModel.activeThreadMessages.collectAsStateWithLifecycle()
    val activeArchivedMessages by viewModel.activeArchivedMessages.collectAsStateWithLifecycle()
    val allArchivedMessages by viewModel.allArchivedMessages.collectAsStateWithLifecycle()
    val archivedCount by viewModel.archivedCount.collectAsStateWithLifecycle()
    val activeCategoryContacts by viewModel.activeCategoryContacts.collectAsStateWithLifecycle()
    val allCategoryContacts by viewModel.allCategoryContacts.collectAsStateWithLifecycle()
    val diaryEntries by viewModel.diaryEntries.collectAsStateWithLifecycle()
    val events by viewModel.events.collectAsStateWithLifecycle()
    val vaultDocs by viewModel.vaultDocuments.collectAsStateWithLifecycle()
    val tasks by viewModel.tasks.collectAsStateWithLifecycle()
    val musicTracks by viewModel.musicTracks.collectAsStateWithLifecycle()

    var showGlobalSettingsDialog by remember { mutableStateOf(false) }

    // Android 13+ Notification Permission Flow
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ -> }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    // Auto-fetch local GPS location tracking if permission is available
    LaunchedEffect(Unit) {
        if (com.example.util.GpsLocationTracker.hasLocationPermission(context)) {
            viewModel.fetchDeviceGpsLocation(context)
        }
    }

    if (showGlobalSettingsDialog) {
        GlobalSettingsDialog(
            settings = globalSettings,
            onDismissRequest = { showGlobalSettingsDialog = false },
            onToggleLocation = { viewModel.toggleGlobalLocation(it) },
            onSetLocation = { loc, lat, lng, auto -> viewModel.setGlobalLocation(loc, lat, lng, auto) },
            onToggleTranslation = { viewModel.toggleGlobalTranslation(it) },
            onSetLanguage = { name, code -> viewModel.setGlobalLanguage(name, code) },
            onToggleAutoTranslate = { viewModel.toggleAutoTranslate(it) },
            onSetTranslationEngine = { viewModel.setTranslationEngine(it) }
        )
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        ModalNavigationDrawer(
            drawerState = drawerState,
            gesturesEnabled = true,
            drawerContent = {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    LedgerSideMenuDrawer(viewModel = viewModel, 
                        currentSection = currentSection,
                        onSectionSelected = { viewModel.setSection(it) },
                        allContacts = allCategoryContacts,
                        onAddContact = { cat, name, phone, note -> viewModel.addCategoryContact(cat, name, phone, note) },
                        onDeleteContact = { viewModel.deleteCategoryContact(it) },
                        onCloseDrawer = { coroutineScope.launch { drawerState.close() } },
                        networkState = networkState,
                        pendingSyncCount = pendingSyncCount,
                        onSyncQueuedMessages = { viewModel.syncQueuedMessages() },
                        onSetSimulationMode = { viewModel.setNetworkSimulationMode(it) },
                        onShareGoogleDrive = { viewModel.shareBackupToGoogleDrive(context) },
                        musicTracks = musicTracks,
                        onPlayTrack = { com.example.util.MusicPlayerManager.playTrack(it, musicTracks, context) },
                        onOpenGlobalSettings = { showGlobalSettingsDialog = true }
                    )
                }
            }
        ) {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                BoxWithConstraints(modifier = modifier.fillMaxSize()) {
            val isExpanded = maxWidth >= 600.dp

            // Ambient Living Water flowing wave background behind the entire app
            AmbientWaterFlowBackground()

            if (isExpanded) {
                // Adaptive Tablet / Landscape layout with Navigation Rail
                Row(
                    modifier = Modifier.fillMaxSize()
                ) {
                    LedgerBinderNavRail(
                        currentSection = currentSection,
                        onSectionSelected = { viewModel.setSection(it) }
                    )

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    ) {
                        ScreenContent(
                            currentSection = currentSection,
                            viewModel = viewModel,
                            chatThreads = chatThreads,
                            activeChatThreadKey = activeChatThreadKey,
                            activeThreadMessages = activeThreadMessages,
                            activeArchivedMessages = activeArchivedMessages,
                            allArchivedMessages = allArchivedMessages,
                            archivedCount = archivedCount,
                            activeCategoryContacts = activeCategoryContacts,
                            diaryEntries = diaryEntries,
                            events = events,
                            vaultDocs = vaultDocs,
                            tasks = tasks,
                            musicTracks = musicTracks,
                            networkState = networkState,
                            pendingSyncCount = pendingSyncCount,
                            globalSettings = globalSettings,
                            onOpenGlobalSettings = { showGlobalSettingsDialog = true },
                            onOpenDrawer = { coroutineScope.launch { drawerState.open() } }
                        )
                    }
                }
            } else {
                // Compact Mobile layout with Binder Bottom Bar
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        LedgerBinderBottomBar(
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
                            chatThreads = chatThreads,
                            activeChatThreadKey = activeChatThreadKey,
                            activeThreadMessages = activeThreadMessages,
                            activeArchivedMessages = activeArchivedMessages,
                            allArchivedMessages = allArchivedMessages,
                            archivedCount = archivedCount,
                            activeCategoryContacts = activeCategoryContacts,
                            diaryEntries = diaryEntries,
                            events = events,
                            vaultDocs = vaultDocs,
                            tasks = tasks,
                            musicTracks = musicTracks,
                            networkState = networkState,
                            pendingSyncCount = pendingSyncCount,
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
    chatThreads: List<com.example.data.model.ChatThreadEntity>,
    activeChatThreadKey: String,
    activeThreadMessages: List<com.example.data.model.ChatMessageEntity>,
    activeArchivedMessages: List<com.example.data.model.ChatMessageEntity>,
    allArchivedMessages: List<com.example.data.model.ChatMessageEntity>,
    archivedCount: Int,
    activeCategoryContacts: List<com.example.data.model.CategoryContactEntity>,
    diaryEntries: List<com.example.data.model.DiaryEntryEntity>,
    events: List<com.example.data.model.EventEntity>,
    vaultDocs: List<com.example.data.model.VaultDocumentEntity>,
    tasks: List<com.example.data.model.TaskEntity>,
    musicTracks: List<com.example.data.model.MusicTrackEntity>,
    networkState: com.example.util.NetworkState,
    pendingSyncCount: Int,
    globalSettings: com.example.ui.GlobalSettingsState,
    onOpenGlobalSettings: () -> Unit,
    onOpenDrawer: () -> Unit
) {
    val context = LocalContext.current
    Crossfade(targetState = currentSection, modifier = Modifier.fillMaxSize(), label = "ledger_section_crossfade") { section ->
        when (section) {
            LedgerSection.RINGTONES -> {
                RingtonesScreen(
                    tracks = musicTracks.filter { it.category == "Ringtone" },
                    onAddTrack = { songName, albumName, category, artist, uriStr, fileName, fileSize, duration, sourceType, notes ->
                        viewModel.addMusicTrack(songName, albumName, category, artist, uriStr, fileName, fileSize, duration, sourceType, notes)
                    },
                    onUpdateTrack = { viewModel.updateMusicTrack(it) },
                    onDeleteTrack = { viewModel.deleteMusicTrack(it) },
                    onToggleFavorite = { id, fav -> viewModel.toggleMusicFavorite(id, fav) },
                    onMenuClick = onOpenDrawer
                )
            }
            LedgerSection.MUSIC -> {
                MusicScreen(
                    tracks = musicTracks.filter { it.category != "Ringtone" },
                    onAddTrack = { songName, albumName, category, artist, uriStr, fileName, fileSize, duration, sourceType, notes ->
                        viewModel.addMusicTrack(songName, albumName, category, artist, uriStr, fileName, fileSize, duration, sourceType, notes)
                    },
                    onUpdateTrack = { viewModel.updateMusicTrack(it) },
                    onDeleteTrack = { viewModel.deleteMusicTrack(it) },
                    onToggleFavorite = { id, fav -> viewModel.toggleMusicFavorite(id, fav) },
                    onMenuClick = onOpenDrawer
                )
            }
            LedgerSection.SOCIAL -> {
                SocialScreen(viewModel = viewModel, 
                    onMenuClick = onOpenDrawer
                )
            }

            LedgerSection.STUDIO -> {
                ImageStudioScreen(
                    onMenuClick = onOpenDrawer,
                    globalSettings = globalSettings,
                    onOpenGlobalSettings = onOpenGlobalSettings
                )
            }

            LedgerSection.GAMES -> {
                GamesScreen(
                    onMenuClick = onOpenDrawer,
                    globalSettings = globalSettings,
                    onOpenGlobalSettings = onOpenGlobalSettings
                )
            }
        }
    }
}

