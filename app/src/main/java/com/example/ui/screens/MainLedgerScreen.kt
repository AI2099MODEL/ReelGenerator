package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.LedgerSection
import com.example.ui.LedgerViewModel
import com.example.ui.components.LedgerBinderBottomBar
import com.example.ui.components.LedgerBinderNavRail
import com.example.ui.components.TabBackgroundView
import com.example.ui.components.ColoredNotificationBannerHost
import com.example.ui.components.LocalNotificationService
import com.example.ui.components.NotificationService
import androidx.compose.ui.Alignment
import androidx.compose.ui.zIndex
import kotlinx.coroutines.launch

@Composable
fun MainLedgerScreen(
    viewModel: LedgerViewModel,
    modifier: Modifier = Modifier
) {
    val notificationService = remember { NotificationService() }

    CompositionLocalProvider(
        LocalLayoutDirection provides LayoutDirection.Ltr,
        LocalNotificationService provides notificationService
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            MainLedgerScreenContent(viewModel = viewModel, modifier = modifier)
            
            // Global Notification Banner
            ColoredNotificationBannerHost(
                notification = notificationService.activeNotification,
                onDismiss = { notificationService.dismiss() },
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 16.dp, start = 16.dp, end = 16.dp)
                    .zIndex(100f)
            )
        }
    }
}

@Composable
private fun MainLedgerScreenContent(
    viewModel: LedgerViewModel,
    modifier: Modifier = Modifier
) {
    val currentSection by viewModel.selectedSection.collectAsStateWithLifecycle()
    val events by viewModel.events.collectAsStateWithLifecycle()
    val vaultDocs by viewModel.vaultDocuments.collectAsStateWithLifecycle()
    val dailySchedules by viewModel.dailySchedules.collectAsStateWithLifecycle()
    val globalSettings by viewModel.globalSettings.collectAsStateWithLifecycle()

    val sections = remember {
        listOf(
            LedgerSection.DAILY_SCHEDULE,
            LedgerSection.IMPORTANT_DATES,
            LedgerSection.REMIND_ME,
            LedgerSection.IMAGES,
            LedgerSection.VAULT
        )
    }

    val currentSectionIndex = remember(currentSection) {
        val idx = sections.indexOf(currentSection)
        if (idx >= 0) idx else 0
    }

    val pagerState = rememberPagerState(
        initialPage = currentSectionIndex,
        pageCount = { sections.size }
    )
    val coroutineScope = rememberCoroutineScope()

    // Sync pager when ViewModel currentSection changes
    LaunchedEffect(currentSection) {
        val targetIdx = sections.indexOf(currentSection)
        if (targetIdx >= 0 && pagerState.currentPage != targetIdx && !pagerState.isScrollInProgress) {
            pagerState.animateScrollToPage(targetIdx)
        }
    }

    // Sync ViewModel when user completes a swipe gesture
    LaunchedEffect(pagerState.currentPage) {
        if (pagerState.currentPage in sections.indices) {
            val targetSection = sections[pagerState.currentPage]
            if (currentSection != targetSection) {
                viewModel.setSection(targetSection)
            }
        }
    }

    BackHandler(enabled = pagerState.currentPage != 0) {
        coroutineScope.launch {
            pagerState.animateScrollToPage(0)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
    ) {
        BoxWithConstraints(
            modifier = Modifier
                    .fillMaxSize()
            ) {
                val currentDisplaySection = sections.getOrElse(pagerState.currentPage) { LedgerSection.DAILY_SCHEDULE }
                TabBackgroundView(currentSection = currentDisplaySection)

                val isTablet = maxWidth >= 600.dp
                if (isTablet) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                    ) {
                        LedgerBinderNavRail(
                            currentSection = currentDisplaySection,
                            onSectionSelected = { targetSection ->
                                val targetIdx = sections.indexOf(targetSection)
                                if (targetIdx >= 0) {
                                    coroutineScope.launch {
                                        pagerState.animateScrollToPage(targetIdx)
                                    }
                                }
                            },
                        )
                        Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                            ScreenPagerContent(
                                pagerState = pagerState,
                                sections = sections,
                                viewModel = viewModel,
                                events = events,
                                vaultDocs = vaultDocs,
                                dailySchedules = dailySchedules
                            )
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                    ) {
                        Scaffold(
                            bottomBar = {
                                LedgerBinderBottomBar(
                                    currentSection = currentDisplaySection,
                                    onSectionSelected = { targetSection ->
                                        val targetIdx = sections.indexOf(targetSection)
                                        if (targetIdx >= 0) {
                                            coroutineScope.launch {
                                                pagerState.animateScrollToPage(targetIdx)
                                            }
                                        }
                                    }
                                )
                            },
                            containerColor = Color.Transparent
                        ) { innerPadding ->
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(innerPadding)
                                    .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                            ) {
                                ScreenPagerContent(
                                    pagerState = pagerState,
                                    sections = sections,
                                    viewModel = viewModel,
                                    events = events,
                                    vaultDocs = vaultDocs,
                                    dailySchedules = dailySchedules
                                )
                            }
                        }
                    }
                }
            }
        }
}

@Composable
private fun ScreenPagerContent(
    pagerState: androidx.compose.foundation.pager.PagerState,
    sections: List<LedgerSection>,
    viewModel: LedgerViewModel,
    events: List<com.example.data.model.EventEntity>,
    vaultDocs: List<com.example.data.model.VaultDocumentEntity>,
    dailySchedules: List<com.example.data.model.DailyScheduleEntity>
) {
    HorizontalPager(
        state = pagerState,
        modifier = Modifier.fillMaxSize(),
        beyondViewportPageCount = 1
    ) { page ->
        val section = sections[page]
        Box(modifier = Modifier.fillMaxSize()) {
            TabBackgroundView(currentSection = section)
            when (section) {
                LedgerSection.IMAGES -> {
                    ImageStudioScreen(
                        onHomeClick = null,
                        onMenuClick = null,
                        onOpenGlobalSettings = {}
                    )
                }
                LedgerSection.IMPORTANT_DATES -> {
                    RemindMeDatesScreen(
                        mode = DateScreenMode.IMPORTANT_DATES,
                        events = events,
                        onAddEvent = { title, location, notify, includeYear, eventTime, category, eventType ->
                            viewModel.addEvent(
                                title = title,
                                locationOrNote = location,
                                eventTimestamp = eventTime,
                                notifyMe = notify,
                                category = category,
                                includeYear = includeYear,
                                eventType = eventType
                            )
                        },
                        onDeleteEvent = { viewModel.deleteEvent(it) },
                        onToggleComplete = { viewModel.toggleEventCompleted(it) },
                        onHomeClick = null
                    )
                }
                LedgerSection.REMIND_ME -> {
                    RemindMeDatesScreen(
                        mode = DateScreenMode.REMIND_ME,
                        events = events,
                        onAddEvent = { title, location, notify, includeYear, eventTime, category, eventType ->
                            viewModel.addEvent(
                                title = title,
                                locationOrNote = location,
                                eventTimestamp = eventTime,
                                notifyMe = notify,
                                category = category,
                                includeYear = includeYear,
                                eventType = eventType
                            )
                        },
                        onDeleteEvent = { viewModel.deleteEvent(it) },
                        onToggleComplete = { viewModel.toggleEventCompleted(it) },
                        onHomeClick = null
                    )
                }
                LedgerSection.DAILY_SCHEDULE -> {
                    DailyScheduleScreen(
                        schedules = dailySchedules,
                        onAddSchedule = { sched ->
                            viewModel.addDailySchedule(sched)
                        },
                        onToggleComplete = { viewModel.toggleDailyScheduleComplete(it) },
                        onUpdateSchedule = { viewModel.updateDailySchedule(it) },
                        onDeleteSchedule = { viewModel.deleteDailySchedule(it) },
                        onLoadSampleRoutine = { viewModel.loadSampleRoutine() }
                    )
                }
                LedgerSection.VAULT -> {
                    VaultScreen(
                        vaultDocs = vaultDocs,
                        onAddDocument = { title, filename, uriStr, type, category, bytes, notes ->
                            viewModel.addVaultDocument(title, filename, uriStr, type, category, bytes, notes)
                        },
                        onDeleteDocument = { viewModel.deleteVaultDocument(it) },
                        onHomeClick = null
                    )
                }
            }
        }
    }
}
