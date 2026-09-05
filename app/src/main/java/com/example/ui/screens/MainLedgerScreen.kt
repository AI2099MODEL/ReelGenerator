package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.*
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

@Composable
fun MainLedgerScreen(
    viewModel: LedgerViewModel,
    modifier: Modifier = Modifier
) {
    val currentSection by viewModel.selectedSection.collectAsStateWithLifecycle()
    val events by viewModel.events.collectAsStateWithLifecycle()
    val vaultDocs by viewModel.vaultDocuments.collectAsStateWithLifecycle()
    val globalSettings by viewModel.globalSettings.collectAsStateWithLifecycle()

    BackHandler(enabled = currentSection != LedgerSection.IMAGES) {
        viewModel.setSection(LedgerSection.IMAGES)
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        BoxWithConstraints(
            modifier = modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(28.dp))
        ) {
            TabBackgroundView(currentSection = currentSection)

            val isTablet = maxWidth >= 600.dp
            if (isTablet) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(28.dp))
                ) {
                    LedgerBinderNavRail(
                        currentSection = currentSection,
                        onSectionSelected = { viewModel.setSection(it) },
                    )
                    Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                        ScreenContent(
                            currentSection = currentSection,
                            viewModel = viewModel,
                            events = events,
                            vaultDocs = vaultDocs
                        )
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(28.dp))
                ) {
                    Scaffold(
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
                                .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                        ) {
                            ScreenContent(
                                currentSection = currentSection,
                                viewModel = viewModel,
                                events = events,
                                vaultDocs = vaultDocs
                            )
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
    events: List<com.example.data.model.EventEntity>,
    vaultDocs: List<com.example.data.model.VaultDocumentEntity>
) {
    Crossfade(targetState = currentSection, modifier = Modifier.fillMaxSize(), label = "ledger_section_crossfade") { section ->
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
                LedgerSection.VAULT -> {
                    VaultScreen(
                        vaultDocs = vaultDocs,
                        onAddDocument = { title, filename, uriStr, type, category, bytes ->
                            viewModel.addVaultDocument(title, filename, uriStr, type, category, bytes, "")
                        },
                        onDeleteDocument = { viewModel.deleteVaultDocument(it) },
                        onHomeClick = null
                    )
                }
            }
        }
    }
}

