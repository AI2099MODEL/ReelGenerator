package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import com.example.ui.components.LocalNotificationService
import com.example.ui.components.NotificationType
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CategoryContactEntity
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import com.example.data.model.MusicTrackEntity
import com.example.ui.LedgerSection
import com.example.ui.theme.*
import com.example.util.DecodedQrResult
import com.example.util.NetworkSimulationMode
import com.example.util.NetworkState
import com.example.util.QRCodeDecoder

@Composable
fun LedgerSideMenuDrawer(
    currentSection: LedgerSection,
    onSectionSelected: (LedgerSection) -> Unit,
    allContacts: List<CategoryContactEntity> = emptyList(),
    onAddContact: ((String, String, String, String) -> Unit)? = null,
    onDeleteContact: ((Long) -> Unit)? = null,
    onCloseDrawer: () -> Unit,
    networkState: NetworkState? = null,
    pendingSyncCount: Int = 0,
    onSyncQueuedMessages: (() -> Unit)? = null,
    onSetSimulationMode: ((NetworkSimulationMode) -> Unit)? = null,
    onShareGoogleDrive: (() -> Unit)? = null,
    onRestoreBackup: ((String, (com.example.util.BackupRestoreResult) -> Unit) -> Unit)? = null,
    onTriggerAutoArchive: (((Int) -> Unit) -> Unit)? = null,
    archivedCount: Int = 0,
    musicTracks: List<MusicTrackEntity> = emptyList(),
    onPlayTrack: ((MusicTrackEntity) -> Unit)? = null,
    onOpenGlobalSettings: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    viewModel: com.example.ui.LedgerViewModel? = null
) {
    val context = LocalContext.current
    val notificationService = LocalNotificationService.current
    val coroutineScope = rememberCoroutineScope()

    // State to toggle hiding or showing mobile numbers in the side menu
    var numbersVisible by rememberSaveable { mutableStateOf(true) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryFilter by remember { mutableStateOf("All") }
    var showAddContactDialog by remember { mutableStateOf(false) }
    var showPrivacyPolicyDialog by remember { mutableStateOf(false) }

    // QR Scanning & Uploading States
    var showCameraScanner by remember { mutableStateOf(false) }
    var decodedQrResult by remember { mutableStateOf<DecodedQrResult?>(null) }
    var selectedContactForQr by remember { mutableStateOf<CategoryContactEntity?>(null) }
    var showNetworkDialog by remember { mutableStateOf(false) }

    // Launcher to pick QR code image from local mobile or laptop storage
    val qrImagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val result = QRCodeDecoder.decodeFromUri(context, uri)
            if (result != null) {
                decodedQrResult = result
            } else {
                notificationService.show("Attention", "No readable QR code found in selected image. Please try another image.", NotificationType.ALERT)
            }
        }
    }

    val categories = listOf("All", "Family", "Work", "Personal", "Utility")

    val filteredContacts = remember(allContacts, searchQuery, selectedCategoryFilter) {
        allContacts.filter { contact ->
            val matchesCategory = selectedCategoryFilter == "All" || contact.category.equals(selectedCategoryFilter, ignoreCase = true)
            val matchesSearch = searchQuery.isBlank() ||
                    contact.name.contains(searchQuery, ignoreCase = true) ||
                    contact.phoneNumber.contains(searchQuery, ignoreCase = true) ||
                    contact.note.contains(searchQuery, ignoreCase = true) ||
                    contact.category.contains(searchQuery, ignoreCase = true)
            matchesCategory && matchesSearch
        }
    }

    ModalDrawerSheet(
        modifier = modifier
            .width(320.dp)
            .fillMaxHeight(),
        drawerContainerColor = RoseQuartzContainerLowest,
        drawerContentColor = RoseQuartzTextPrimary,
        drawerShape = RoundedCornerShape(topEnd = 20.dp, bottomEnd = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // ----------------------------------------------------
            // DRAWER HEADER WITH APP TITLE & OFFLINE / SIGNAL BADGE
            // ----------------------------------------------------
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = RoseQuartzPrimaryContainer.copy(alpha = 0.6f),
                border = BorderStroke(0.dp, Color.Transparent)
            ) {
                Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            AppHeaderLogo(size = 36.dp)

                            Column {
                                Text(
                                    text = "Lyfe",
                                    fontFamily = FontFamily.Serif,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 17.sp,
                                    color = RoseQuartzTextPrimary
                                )
                                Text(
                                    text = "Personal Directory & Hub",
                                    fontSize = 10.sp,
                                    color = RoseQuartzPrimary,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        IconButton(
                            onClick = onCloseDrawer,
                            modifier = Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                                .background(RoseQuartzContainerHighest)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "Close Menu",
                                tint = RoseQuartzTextPrimary,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }

                    // Network & Offline Status Chip in Drawer Header
                    if (networkState != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        NetworkStatusChip(
                            networkState = networkState,
                            pendingSyncCount = pendingSyncCount,
                            onOpenNetworkDialog = { showNetworkDialog = true },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // ----------------------------------------------------
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "SECTIONS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = RoseQuartzTextMuted,
                            letterSpacing = 1.sp,
                            modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                        )
                        DrawerNavItem(
                            label = "Daily Schedule",
                            icon = Icons.Filled.CalendarMonth,
                            isSelected = currentSection == LedgerSection.DAILY_SCHEDULE,
                            onClick = {
                                onSectionSelected(LedgerSection.DAILY_SCHEDULE)
                                onCloseDrawer()
                            }
                        )
                        DrawerNavItem(
                            label = "Important Dates",
                            icon = Icons.Filled.Cake,
                            isSelected = currentSection == LedgerSection.IMPORTANT_DATES,
                            onClick = {
                                onSectionSelected(LedgerSection.IMPORTANT_DATES)
                                onCloseDrawer()
                            }
                        )
                        DrawerNavItem(
                            label = "Remind Me",
                            icon = Icons.Filled.NotificationsActive,
                            isSelected = currentSection == LedgerSection.REMIND_ME,
                            onClick = {
                                onSectionSelected(LedgerSection.REMIND_ME)
                                onCloseDrawer()
                            }
                        )
                        DrawerNavItem(
                            label = "Secure Vault",
                            icon = Icons.Filled.Lock,
                            isSelected = currentSection == LedgerSection.VAULT,
                            onClick = {
                                onSectionSelected(LedgerSection.VAULT)
                                onCloseDrawer()
                            }
                        )
                        DrawerNavItem(
                            label = "Image Studio",
                            icon = Icons.Filled.AutoAwesome,
                            isSelected = currentSection == LedgerSection.IMAGES,
                            onClick = {
                                onSectionSelected(LedgerSection.IMAGES)
                                onCloseDrawer()
                            }
                        )
                        DrawerNavItem(
                            label = "Privacy Policy",
                            icon = Icons.Filled.Security,
                            isSelected = false,
                            onClick = {
                                onCloseDrawer()
                                showPrivacyPolicyDialog = true
                            }
                        )
                        if (onOpenGlobalSettings != null) {
                            DrawerNavItem(
                                label = "Global Settings & Translation",
                                icon = Icons.Filled.Settings,
                                isSelected = false,
                                onClick = {
                                    onOpenGlobalSettings()
                                    onCloseDrawer()
                                }
                            )
                        }
                    }
                }
            } // end LazyColumn
        } // end Column
    }

    if (showPrivacyPolicyDialog) {
        PrivacyPolicyDialog(
            onDismiss = { showPrivacyPolicyDialog = false }
        )
    }
}

@Composable
fun PrivacyPolicyDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Overview", "GDPR (EU/UK)", "US Rights", "Security & Contact")
    val policyUrl = "https://ais-pre-7va6et5cmfr2bqdzzsb25k-202411574583.asia-southeast1.run.app/privacy-policy.html"

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Security,
                        contentDescription = null,
                        tint = Color(0xFF0284C7),
                        modifier = Modifier.size(24.dp)
                    )
                    Column {
                        Text(
                            text = "Privacy Policy",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color(0xFF0F172A)
                        )
                        Text(
                            text = "GDPR & US Compliant • Sept 2026",
                            fontSize = 11.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                }
                IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color(0xFF64748B))
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 480.dp)
            ) {
                // Category Tabs
                ScrollableTabRow(
                    selectedTabIndex = selectedTab,
                    edgePadding = 0.dp,
                    containerColor = Color(0xFFF0F9FF),
                    contentColor = Color(0xFF0284C7),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = {
                                Text(
                                    text = title,
                                    fontSize = 12.sp,
                                    fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Scrollable Content
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    when (selectedTab) {
                        0 -> { // Overview
                            item {
                                Surface(
                                    color = Color(0xFFEFF6FF),
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.dp, Color(0xFFBAE6FD))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("🛡️", fontSize = 18.sp)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Privacy-First & Offline-First: All tasks, reminders, contact notes, and vault documents are stored locally on your device.",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = Color(0xFF0369A1)
                                        )
                                    }
                                }
                            }
                            item {
                                Text(
                                    text = "Key Principles:",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = Color(0xFF0F172A)
                                )
                                Text(
                                    text = "• Local Storage: Data is stored inside the app sandbox on your device.\n" +
                                            "• No Selling or Sharing: We do not sell or monetize personal information.\n" +
                                            "• Biometrics: Fingerprint/Face authentication uses Android TEE hardware and never leaves your device.\n" +
                                            "• AI Generation: Prompts sent securely via TLS 1.3 only when you explicitly tap generate.",
                                    fontSize = 12.sp,
                                    color = Color(0xFF334155),
                                    lineHeight = 18.sp
                                )
                            }
                        }
                        1 -> { // GDPR
                            item {
                                Surface(
                                    color = Color(0xFFECFDF5),
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.dp, Color(0xFFA7F3D0))
                                ) {
                                    Text(
                                        text = "🇪🇺 European Union & UK GDPR Compliance (Regulation EU 2016/679)",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF065F46),
                                        modifier = Modifier.padding(8.dp)
                                    )
                                }
                            }
                            item {
                                Text(
                                    text = "Your Rights under GDPR (Articles 15-22):",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = Color(0xFF0F172A)
                                )
                                Text(
                                    text = "1. Right of Access (Art. 15): View all your data anytime directly in the app.\n" +
                                            "2. Right to Rectification (Art. 16): Edit any item at any time.\n" +
                                            "3. Right to Erasure (Art. 17): Delete any entry or clear app data via system settings.\n" +
                                            "4. Right to Data Portability (Art. 20): Export complete data backups anytime.\n" +
                                            "5. Right to Restriction & Object (Arts. 18 & 21): Full offline user control.\n" +
                                            "6. Right to Withdraw Consent: Turn off camera, audio, or notification permissions in Android settings.\n" +
                                            "7. Right to Lodge a Complaint: File with your regional Data Protection Authority (DPA).",
                                    fontSize = 12.sp,
                                    color = Color(0xFF334155),
                                    lineHeight = 18.sp
                                )
                            }
                        }
                        2 -> { // US Rights
                            item {
                                Surface(
                                    color = Color(0xFFFFFBEB),
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.dp, Color(0xFFFDE68A))
                                ) {
                                    Text(
                                        text = "🇺🇸 US State Privacy Laws (CCPA / CPRA / VCDPA / CPA / CTDPA / UCPA)",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF92400E),
                                        modifier = Modifier.padding(8.dp)
                                    )
                                }
                            }
                            item {
                                Text(
                                    text = "US Consumer Rights:",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = Color(0xFF0F172A)
                                )
                                Text(
                                    text = "• Do Not Sell or Share: We do not sell or share personal data for cross-context behavioral ads.\n" +
                                            "• Right to Know & Delete: Request disclosure or complete deletion of records.\n" +
                                            "• Right to Non-Discrimination: Equal service without penalty for exercising privacy rights.\n" +
                                            "• COPPA Compliance: The app does not knowingly collect information from children under 13.",
                                    fontSize = 12.sp,
                                    color = Color(0xFF334155),
                                    lineHeight = 18.sp
                                )
                            }
                        }
                        3 -> { // Security & Contact
                            item {
                                Text(
                                    text = "Data Controller & Contact:",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = Color(0xFF0F172A)
                                )
                                Text(
                                    text = "• Controller: Lyfe Application Team\n" +
                                            "• Email: anuakku20138@gmail.com\n" +
                                            "• Policy URL: $policyUrl",
                                    fontSize = 12.sp,
                                    color = Color(0xFF334155),
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Link & Share Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("Privacy Policy Link", policyUrl)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "Privacy Policy Link copied!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, Color(0xFFBAE6FD)),
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color(0xFF0284C7))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Copy Link", fontSize = 12.sp, color = Color(0xFF0284C7), fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            try {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(policyUrl))
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                Toast.makeText(context, "Could not open browser", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF0284C7),
                            contentColor = Color.White
                        ),
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.OpenInBrowser, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Open Online", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = Color(0xFF0284C7), fontWeight = FontWeight.Bold)
            }
        },
        shape = RoundedCornerShape(16.dp),
        containerColor = Color.White
    )
}

@Composable
fun DrawerNavItem(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val bgColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
    val contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(androidx.compose.foundation.shape.RoundedCornerShape(8.dp))
            .clickable { onClick() },
        color = bgColor
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = contentColor,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = label,
                color = contentColor,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}
