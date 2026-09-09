package com.example.ui.screens

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ads.AdManager
import com.example.ui.LedgerSection
import com.example.ui.LedgerViewModel
import com.example.ui.components.AdMobBanner
import com.example.ui.components.ColoredNotificationBannerHost
import com.example.ui.components.LedgerBinderBottomBar
import com.example.ui.components.LedgerBinderNavRail
import com.example.ui.components.LocalNotificationService
import com.example.ui.components.NotificationService
import com.example.ui.components.TabBackgroundView
import kotlinx.coroutines.flow.flowOf
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
    val context = LocalContext.current
    val activity = context as? Activity
    val adManager = remember { AdManager.get() }
    val isImageStudioUnlocked by (adManager?.isImageStudioUnlocked ?: flowOf(false)).collectAsStateWithLifecycle(false)
    var showUnlockDialog by remember { mutableStateOf(false) }

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
            LedgerSection.VAULT,
            LedgerSection.IMAGES
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

    fun navigateToSection(targetSection: LedgerSection) {
        activity?.let { adManager?.checkAndShowPeriodicInterstitial(it) }
        val targetIdx = sections.indexOf(targetSection)
        if (targetIdx >= 0) {
            if (targetSection == LedgerSection.IMAGES && !isImageStudioUnlocked) {
                showUnlockDialog = true
            } else {
                coroutineScope.launch {
                    pagerState.animateScrollToPage(targetIdx)
                }
            }
        }
    }

    // Sync pager when ViewModel currentSection changes
    LaunchedEffect(currentSection) {
        val targetIdx = sections.indexOf(currentSection)
        if (targetIdx >= 0 && pagerState.currentPage != targetIdx && !pagerState.isScrollInProgress) {
            if (currentSection == LedgerSection.IMAGES && !isImageStudioUnlocked) {
                showUnlockDialog = true
            } else {
                pagerState.animateScrollToPage(targetIdx)
            }
        }
    }

    // Sync ViewModel when user completes a swipe gesture
    LaunchedEffect(pagerState.currentPage) {
        if (pagerState.currentPage in sections.indices) {
            val targetSection = sections[pagerState.currentPage]
            if (currentSection != targetSection) {
                viewModel.setSection(targetSection)
                activity?.let { adManager?.checkAndShowPeriodicInterstitial(it) }
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
                                navigateToSection(targetSection)
                            },
                        )
                        Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                            ScreenPagerContent(
                                pagerState = pagerState,
                                sections = sections,
                                viewModel = viewModel,
                                events = events,
                                vaultDocs = vaultDocs,
                                dailySchedules = dailySchedules,
                                isImageStudioUnlocked = isImageStudioUnlocked,
                                onRequestUnlock = {
                                    activity?.let { act ->
                                        adManager?.showRewardedInterstitialAd(
                                            activity = act,
                                            onRewardEarned = {
                                                val imgIdx = sections.indexOf(LedgerSection.IMAGES)
                                                if (imgIdx >= 0) {
                                                    coroutineScope.launch { pagerState.animateScrollToPage(imgIdx) }
                                                }
                                            },
                                            onDismissedOrFailed = {}
                                        )
                                    }
                                }
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
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .navigationBarsPadding(),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    LedgerBinderBottomBar(
                                        currentSection = currentDisplaySection,
                                        onSectionSelected = { targetSection ->
                                            navigateToSection(targetSection)
                                        }
                                    )
                                    AdMobBanner(
                                        adUnitId = "ca-app-pub-8815300826143812/1631030419",
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 2.dp, bottom = 4.dp)
                                    )
                                }
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
                                    dailySchedules = dailySchedules,
                                    isImageStudioUnlocked = isImageStudioUnlocked,
                                    onRequestUnlock = {
                                        activity?.let { act ->
                                            adManager?.showRewardedInterstitialAd(
                                                activity = act,
                                                onRewardEarned = {
                                                    val imgIdx = sections.indexOf(LedgerSection.IMAGES)
                                                    if (imgIdx >= 0) {
                                                        coroutineScope.launch { pagerState.animateScrollToPage(imgIdx) }
                                                    }
                                                },
                                                onDismissedOrFailed = {}
                                            )
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

    // Rewarded Interstitial Opt-In Dialog compliant with AdMob Policies
    if (showUnlockDialog) {
        AlertDialog(
            onDismissRequest = { showUnlockDialog = false },
            icon = {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(
                            Brush.linearGradient(listOf(Color(0xFFFFB74D), Color(0xFFFF9800))),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(30.dp)
                    )
                }
            },
            title = {
                Text(
                    text = "Unlock AI Image Studio",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Watch a short sponsored video to unlock instant access to Text to Image prompts generation!",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = Color(0xFFF57C00),
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Instant Session Access Granted Upon Reward",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showUnlockDialog = false
                        activity?.let { act ->
                            adManager?.showRewardedInterstitialAd(
                                activity = act,
                                onRewardEarned = {
                                    val imgIdx = sections.indexOf(LedgerSection.IMAGES)
                                    if (imgIdx >= 0) {
                                        coroutineScope.launch {
                                            pagerState.animateScrollToPage(imgIdx)
                                        }
                                    }
                                },
                                onDismissedOrFailed = {}
                            )
                        } ?: run {
                            adManager?.unlockImageStudioDirectly()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFF9800),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text("Watch Video to Unlock", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showUnlockDialog = false }
                ) {
                    Text("Maybe Later", color = MaterialTheme.colorScheme.outline)
                }
            },
            shape = RoundedCornerShape(24.dp)
        )
    }
}

@Composable
private fun ScreenPagerContent(
    pagerState: androidx.compose.foundation.pager.PagerState,
    sections: List<LedgerSection>,
    viewModel: LedgerViewModel,
    events: List<com.example.data.model.EventEntity>,
    vaultDocs: List<com.example.data.model.VaultDocumentEntity>,
    dailySchedules: List<com.example.data.model.DailyScheduleEntity>,
    isImageStudioUnlocked: Boolean,
    onRequestUnlock: () -> Unit
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
                    if (isImageStudioUnlocked) {
                        ImageStudioScreen(
                            onHomeClick = null,
                            onMenuClick = null,
                            onOpenGlobalSettings = {}
                        )
                    } else {
                        ImageStudioLockedPlaceholder(
                            onWatchAdClick = onRequestUnlock
                        )
                    }
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

/**
 * High-craft Lock Screen placeholder for Text-to-Image tab when reward has not yet been collected.
 */
@Composable
private fun ImageStudioLockedPlaceholder(
    onWatchAdClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
            shadowElevation = 8.dp,
            border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 480.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(32.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(
                            Brush.linearGradient(listOf(Color(0xFFFF9800), Color(0xFFFF5722))),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Locked Feature",
                        tint = Color.White,
                        modifier = Modifier.size(40.dp)
                    )
                }

                Text(
                    text = "Text to Image Studio",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = "To access the AI Text to Image creation tools, please watch a short sponsored video reward. You will receive immediate full access for this session.",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 22.sp
                )

                Spacer(Modifier.height(8.dp))

                Button(
                    onClick = onWatchAdClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFF9800),
                        contentColor = Color.White
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Watch Video to Unlock",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    }
}
