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
import com.example.ui.screens.SOCIAL_CHANNELS
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
    allContacts: List<CategoryContactEntity>,
    onAddContact: (String, String, String, String) -> Unit,
    onDeleteContact: (Long) -> Unit,
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
    val coroutineScope = rememberCoroutineScope()

    // State to toggle hiding or showing mobile numbers in the side menu
    var numbersVisible by rememberSaveable { mutableStateOf(true) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryFilter by remember { mutableStateOf("All") }
    var showAddContactDialog by remember { mutableStateOf(false) }

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
                Toast.makeText(context, "No readable QR code found in selected image. Please try another image.", Toast.LENGTH_LONG).show()
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
                if (currentSection == LedgerSection.MUSIC || currentSection == LedgerSection.RINGTONES) {
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = if (currentSection == LedgerSection.MUSIC) "FAVORITE SONGS" else "FAVORITE RINGTONES",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = RoseQuartzTextMuted,
                                letterSpacing = 1.sp,
                                modifier = Modifier.padding(start = 4.dp)
                            )
                            val favoriteSongs = musicTracks.filter { it.isFavorite }
                            if (favoriteSongs.isEmpty()) {
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = RoseQuartzContainerLowest,
                                    border = BorderStroke(1.dp, RoseQuartzContainerHighest),
                                    shadowElevation = 1.dp,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = if (currentSection == LedgerSection.MUSIC) "No favorite songs yet." else "No favorite ringtones yet.",
                                        fontSize = 12.sp,
                                        color = RoseQuartzTextSecondary,
                                        modifier = Modifier.padding(14.dp)
                                    )
                                }
                            } else {
                                favoriteSongs.forEach { track ->
                                    Surface(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable {
                                                onPlayTrack?.invoke(track)
                                                onCloseDrawer()
                                            }
                                            .padding(bottom = 6.dp),
                                        shape = RoundedCornerShape(8.dp),
                                        color = RoseQuartzBg,
                                        border = BorderStroke(1.dp, RoseQuartzContainerHighest)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(10.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            Icon(Icons.Filled.Favorite, contentDescription = null, tint = RoseQuartzPrimary, modifier = Modifier.size(16.dp))
                                            Column {
                                                Text(track.songName, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = RoseQuartzTextPrimary)
                                                if (track.artist.isNotBlank()) {
                                                    Text(track.artist, fontSize = 11.sp, color = RoseQuartzTextSecondary)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = { onSectionSelected(LedgerSection.GAMES); onCloseDrawer() },
                                colors = ButtonDefaults.buttonColors(containerColor = RoseQuartzPrimary),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(if (currentSection == LedgerSection.MUSIC) "Exit Music" else "Exit Ringtones", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                } else {
                // ----------------------------------------------------
                if (currentSection == LedgerSection.SOCIAL) {
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "UPLOAD SETTINGS",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = RoseQuartzTextMuted,
                                letterSpacing = 1.sp,
                                modifier = Modifier.padding(start = 4.dp)
                            )
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = RoseQuartzContainerLowest,
                                border = BorderStroke(1.dp, RoseQuartzContainerHighest),
                                shadowElevation = 1.dp
                            ) {
                                Column {
                                    var highQuality by remember { mutableStateOf(true) }
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(14.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text("High Quality Upload", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = RoseQuartzTextPrimary)
                                            Text("Upload reels at max resolution", fontSize = 11.sp, color = RoseQuartzTextSecondary)
                                        }
                                        Switch(
                                            checked = highQuality,
                                            onCheckedChange = { highQuality = it },
                                            colors = SwitchDefaults.colors(checkedThumbColor = RoseQuartzPrimary)
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "SOCIAL CHANNELS",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = RoseQuartzTextMuted,
                                letterSpacing = 1.sp,
                                modifier = Modifier.padding(start = 4.dp)
                            )
                            SOCIAL_CHANNELS.forEach { ch ->
                                val isConnected = viewModel?.connectedSocialChannels?.contains(ch.id) == true
                                val isSelected = viewModel?.selectedSocialChannels?.contains(ch.id) == true
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isSelected) RoseQuartzPrimaryContainer else RoseQuartzContainerLowest,
                                    border = BorderStroke(1.dp, if (isSelected) RoseQuartzPrimary else RoseQuartzContainerHighest),
                                    modifier = Modifier.fillMaxWidth().clickable { 
                                        if (isConnected) {
                                            viewModel?.toggleSocialChannelSelection(ch.id)
                                        } else {
                                            Toast.makeText(context, "Please connect account first", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Surface(
                                                modifier = Modifier.size(24.dp),
                                                shape = RoundedCornerShape(4.dp),
                                                color = ch.color.copy(alpha = 0.2f)
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Text(ch.iconName, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = ch.color)
                                                }
                                            }
                                            Text(ch.name, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = RoseQuartzTextPrimary)
                                        }
                                        if (isConnected) {
                                            Checkbox(
                                                checked = isSelected,
                                                onCheckedChange = { viewModel?.toggleSocialChannelSelection(ch.id) },
                                                colors = CheckboxDefaults.colors(checkedColor = RoseQuartzPrimary)
                                            )
                                        } else {
                                            OutlinedButton(
                                                onClick = { 
                                                    coroutineScope.launch {
                                                        Toast.makeText(context, "Connecting...", Toast.LENGTH_SHORT).show()
                                                        delay(1000)
                                                        viewModel?.toggleSocialChannelConnection(ch.id)
                                                        Toast.makeText(context, "Connected to ${ch.name}", Toast.LENGTH_SHORT).show()
                                                    }
                                                },
                                                modifier = Modifier.height(28.dp),
                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                                            ) {
                                                Text("Login", fontSize = 10.sp)
                                            }
                                        }
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = { onSectionSelected(LedgerSection.GAMES); onCloseDrawer() },
                                colors = ButtonDefaults.buttonColors(containerColor = RoseQuartzPrimary),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Exit Post Reel", fontWeight = FontWeight.Bold)
                            }

                        }
                    }
                }

                // QUICK NAVIGATION SECTION
                // ----------------------------------------------------
                if (currentSection != LedgerSection.MUSIC && currentSection != LedgerSection.RINGTONES && currentSection != LedgerSection.SOCIAL) {
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "NAVIGATION HUB",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = RoseQuartzTextMuted,
                                letterSpacing = 1.sp,
                                modifier = Modifier.padding(start = 4.dp)
                            )

                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = RoseQuartzBg.copy(alpha = 0.6f),
                                border = BorderStroke(1.dp, RoseQuartzContainerHighest)
                            ) {
                                Column(modifier = Modifier.padding(6.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    DrawerNavItem(
                                        label = "Music",
                                        icon = Icons.Outlined.MusicNote,
                                        isSelected = currentSection == LedgerSection.MUSIC,
                                        onClick = {
                                            onSectionSelected(LedgerSection.MUSIC)
                                            onCloseDrawer()
                                        }
                                    )
                                    DrawerNavItem(
                                        label = "Ringtones",
                                        icon = Icons.Outlined.Notifications,
                                        isSelected = currentSection == LedgerSection.RINGTONES,
                                        onClick = {
                                            onSectionSelected(LedgerSection.RINGTONES)
                                            onCloseDrawer()
                                        }
                                    )
                                    DrawerNavItem(
                                        label = "Image Studio",
                                        icon = Icons.Outlined.AutoAwesome,
                                        isSelected = currentSection == LedgerSection.STUDIO,
                                        onClick = {
                                            onSectionSelected(LedgerSection.STUDIO)
                                            onCloseDrawer()
                                        }
                                    )
                                    DrawerNavItem(
                                        label = "Post Reel",
                                        icon = Icons.Outlined.VideoLibrary,
                                        isSelected = currentSection == LedgerSection.SOCIAL,
                                        onClick = {
                                            onSectionSelected(LedgerSection.SOCIAL)
                                            onCloseDrawer()
                                        }
                                    )
                                    if (onOpenGlobalSettings != null) {
                                        DrawerNavItem(
                                            label = "Global Settings & Translation",
                                            icon = Icons.Outlined.Public,
                                            isSelected = false,
                                            onClick = {
                                                onOpenGlobalSettings()
                                                onCloseDrawer()
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // ----------------------------------------------------
                if (currentSection != LedgerSection.SOCIAL) {
                // GOOGLE DRIVE BACKUP & CLOUD SETTINGS
                // ----------------------------------------------------
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "CLOUD BACKUP & ARCHIVE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = RoseQuartzTextMuted,
                            letterSpacing = 1.sp,
                            modifier = Modifier.padding(start = 4.dp)
                        )

                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = RoseQuartzContainerLowest,
                            border = BorderStroke(1.dp, RoseQuartzContainerHighest),
                            shadowElevation = 1.dp
                        ) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.CloudUpload,
                                        contentDescription = null,
                                        tint = RoseQuartzPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Column {
                                        Text(
                                            text = "Google Drive Backup",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = RoseQuartzTextPrimary
                                        )
                                        Text(
                                            text = "Export JSON archives & sync attachments",
                                            fontSize = 10.sp,
                                            color = RoseQuartzTextSecondary
                                        )
                                    }
                                }

                                Button(
                                    onClick = {
                                        onShareGoogleDrive?.invoke()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = RoseQuartzPrimary),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(36.dp)
                                ) {
                                    Icon(Icons.Outlined.CloudUpload, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Share Backup to Google Drive", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                // ----------------------------------------------------
                // QR SCANNER & LOCAL IMPORT SECTION
                // ----------------------------------------------------
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "QR CODE SCANNER & IMPORT",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = RoseQuartzTextMuted,
                            letterSpacing = 1.sp,
                            modifier = Modifier.padding(start = 4.dp)
                        )

                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = RoseQuartzContainerLowest,
                            border = BorderStroke(1.dp, RoseQuartzContainerHighest),
                            shadowElevation = 1.dp
                        ) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.QrCodeScanner,
                                        contentDescription = null,
                                        tint = RoseQuartzPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Column {
                                        Text(
                                            text = "Scan or Upload QR",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = RoseQuartzTextPrimary
                                        )
                                        Text(
                                            text = "Import mobile numbers from camera or storage",
                                            fontSize = 10.sp,
                                            color = RoseQuartzTextSecondary
                                        )
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = { showCameraScanner = true },
                                        colors = ButtonDefaults.buttonColors(containerColor = RoseQuartzPrimary),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(36.dp)
                                    ) {
                                        Icon(Icons.Filled.CameraAlt, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Scan QR", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }

                                    OutlinedButton(
                                        onClick = { qrImagePickerLauncher.launch("image/*") },
                                        shape = RoundedCornerShape(8.dp),
                                        border = BorderStroke(1.dp, RoseQuartzPrimary),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                                        modifier = Modifier
                                            .weight(1.1f)
                                            .height(36.dp)
                                    ) {
                                        Icon(Icons.Filled.PhotoLibrary, contentDescription = null, modifier = Modifier.size(14.dp), tint = RoseQuartzPrimary)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Upload QR", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = RoseQuartzPrimary)
                                    }
                                }
                            }
                        }
                    }
                }

                // ----------------------------------------------------
                // MOBILE NUMBERS DIRECTORY (CAN BE HIDDEN OR SHOWN)
                // ----------------------------------------------------
                item {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = RoseQuartzContainerLowest,
                        border = BorderStroke(1.dp, RoseQuartzContainerHighest),
                        shadowElevation = 1.dp
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            // Section header with master Hide/Show toggle switch
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = if (numbersVisible) Icons.Filled.PhoneInTalk else Icons.Filled.PhonePaused,
                                        contentDescription = null,
                                        tint = RoseQuartzPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Column {
                                        Text(
                                            text = "Mobile Numbers",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = RoseQuartzTextPrimary
                                        )
                                        Text(
                                            text = if (numbersVisible) "${allContacts.size} numbers visible" else "Numbers hidden",
                                            fontSize = 10.sp,
                                            color = RoseQuartzTextSecondary
                                        )
                                    }
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    IconButton(
                                        onClick = { numbersVisible = !numbersVisible },
                                        modifier = Modifier.size(30.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (numbersVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                            contentDescription = if (numbersVisible) "Hide Numbers" else "Show Numbers",
                                            tint = if (numbersVisible) RoseQuartzPrimary else RoseQuartzTextMuted,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }

                                    Switch(
                                        checked = numbersVisible,
                                        onCheckedChange = { numbersVisible = it },
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = RoseQuartzOnPrimary,
                                            checkedTrackColor = RoseQuartzPrimary,
                                            uncheckedThumbColor = RoseQuartzTextMuted,
                                            uncheckedTrackColor = RoseQuartzContainerHigh
                                        ),
                                        modifier = Modifier.height(24.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // CONTENT: SHOWN OR HIDDEN
                            AnimatedVisibility(
                                visible = numbersVisible,
                                enter = fadeIn() + expandVertically(),
                                exit = fadeOut() + shrinkVertically()
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    // Search bar
                                    OutlinedTextField(
                                        value = searchQuery,
                                        onValueChange = { searchQuery = it },
                                        placeholder = { Text("Search contact or number...", fontSize = 11.sp, color = RoseQuartzTextMuted) },
                                        leadingIcon = {
                                            Icon(Icons.Outlined.Search, contentDescription = "Search", modifier = Modifier.size(16.dp), tint = RoseQuartzTextMuted)
                                        },
                                        trailingIcon = {
                                            if (searchQuery.isNotEmpty()) {
                                                IconButton(onClick = { searchQuery = "" }, modifier = Modifier.size(20.dp)) {
                                                    Icon(Icons.Filled.Clear, contentDescription = "Clear", modifier = Modifier.size(14.dp))
                                                }
                                            }
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(44.dp),
                                        shape = RoundedCornerShape(8.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = RoseQuartzPrimary,
                                            unfocusedBorderColor = RoseQuartzContainerHighest,
                                            focusedContainerColor = RoseQuartzBg.copy(alpha = 0.5f),
                                            unfocusedContainerColor = RoseQuartzBg.copy(alpha = 0.5f),
                                            focusedTextColor = RoseQuartzTextPrimary,
                                            unfocusedTextColor = RoseQuartzTextPrimary
                                        ),
                                        singleLine = true
                                    )

                                    // Category Filter Pills
                                    LazyRow(
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        items(categories) { cat ->
                                            val isSelected = selectedCategoryFilter.equals(cat, ignoreCase = true)
                                            FilterChip(
                                                selected = isSelected,
                                                onClick = { selectedCategoryFilter = cat },
                                                label = {
                                                    val emoji = when (cat) {
                                                        "Family" -> "🏡 "
                                                        "Work" -> "💼 "
                                                        "Personal" -> "🌿 "
                                                        "Utility" -> "⚡ "
                                                        else -> "✨ "
                                                    }
                                                    Text(text = "$emoji$cat", fontSize = 10.sp)
                                                },
                                                colors = FilterChipDefaults.filterChipColors(
                                                    selectedContainerColor = RoseQuartzPrimary,
                                                    selectedLabelColor = RoseQuartzOnPrimary,
                                                    containerColor = RoseQuartzBg,
                                                    labelColor = RoseQuartzTextPrimary
                                                ),
                                                border = FilterChipDefaults.filterChipBorder(
                                                    enabled = true,
                                                    selected = isSelected,
                                                    borderColor = if (isSelected) RoseQuartzPrimary else RoseQuartzContainerHighest
                                                ),
                                                shape = RoundedCornerShape(6.dp),
                                                modifier = Modifier.height(28.dp)
                                            )
                                        }
                                    }

                                    // Add Mobile Number Button with QR Shortcut
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Button(
                                            onClick = { showAddContactDialog = true },
                                            colors = ButtonDefaults.buttonColors(containerColor = RoseQuartzPrimary),
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(34.dp)
                                        ) {
                                            Icon(Icons.Filled.PersonAdd, contentDescription = null, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Add Mobile Number", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }

                                    // List of Contacts
                                    if (filteredContacts.isEmpty()) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 12.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = if (searchQuery.isNotBlank()) "No contacts match '$searchQuery'" else "No mobile numbers in this category",
                                                fontSize = 11.sp,
                                                color = RoseQuartzTextMuted
                                            )
                                        }
                                    } else {
                                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                            filteredContacts.forEach { contact ->
                                                SideMenuContactCard(
                                                    contact = contact,
                                                    onDelete = { onDeleteContact(contact.id) },
                                                    onShowQr = { selectedContactForQr = contact },
                                                    onCall = {
                                                        val dialIntent = Intent(Intent.ACTION_DIAL).apply {
                                                            data = Uri.parse("tel:${contact.phoneNumber.replace(" ", "")}")
                                                        }
                                                        context.startActivity(dialIntent)
                                                    },
                                                    onSms = {
                                                        val smsIntent = Intent(Intent.ACTION_SENDTO).apply {
                                                            data = Uri.parse("smsto:${contact.phoneNumber.replace(" ", "")}")
                                                        }
                                                        context.startActivity(smsIntent)
                                                    },
                                                    onCopy = {
                                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                                        clipboard.setPrimaryClip(ClipData.newPlainText("Phone Number", contact.phoneNumber))
                                                        Toast.makeText(context, "Copied ${contact.name}'s number: ${contact.phoneNumber}", Toast.LENGTH_SHORT).show()
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // HIDDEN STATE PLACEHOLDER
                            AnimatedVisibility(
                                visible = !numbersVisible,
                                enter = fadeIn() + expandVertically(),
                                exit = fadeOut() + shrinkVertically()
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = RoseQuartzBg.copy(alpha = 0.6f),
                                    border = BorderStroke(1.dp, RoseQuartzContainerHighest),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 4.dp)
                                 ) {
                                    Column(
                                        modifier = Modifier.padding(14.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Lock,
                                            contentDescription = "Hidden",
                                            tint = RoseQuartzTextMuted,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Text(
                                            text = "Mobile Numbers Hidden",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = RoseQuartzTextPrimary
                                        )
                                        Text(
                                            text = "Tap below to reveal and manage your Family, Work, Personal, and Utility contacts.",
                                            fontSize = 10.sp,
                                            color = RoseQuartzTextSecondary,
                                            lineHeight = 14.sp,
                                            modifier = Modifier.padding(horizontal = 4.dp)
                                        )
                                        Button(
                                            onClick = { numbersVisible = true },
                                            shape = RoundedCornerShape(8.dp),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = RoseQuartzPrimaryContainer,
                                                contentColor = RoseQuartzPrimary
                                            ),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                            modifier = Modifier.padding(top = 4.dp)
                                        ) {
                                            Icon(Icons.Filled.Visibility, contentDescription = null, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Show Mobile Numbers", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                }
            }

            // ----------------------------------------------------
                }
            // DRAWER FOOTER
            // ----------------------------------------------------
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = RoseQuartzContainerLowest,
                border = BorderStroke(1.dp, RoseQuartzContainerHighest)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🔒 On-Device Room Storage",
                        fontSize = 10.sp,
                        color = RoseQuartzTextMuted
                    )
                    Text(
                        text = "v2.5 Rose Quartz",
                        fontSize = 10.sp,
                        color = RoseQuartzTextMuted,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }

    // ----------------------------------------------------
    // DIALOG: ADD MOBILE CONTACT FROM SIDE MENU
    // ----------------------------------------------------
    if (showAddContactDialog) {
        var addName by remember { mutableStateOf("") }
        var addPhone by remember { mutableStateOf("") }
        var addNote by remember { mutableStateOf("") }
        var addCategory by remember { mutableStateOf(if (selectedCategoryFilter != "All") selectedCategoryFilter else "Family") }

        AlertDialog(
            onDismissRequest = { showAddContactDialog = false },
            containerColor = RoseQuartzContainerLowest,
            shape = RoundedCornerShape(16.dp),
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.PersonAdd,
                        contentDescription = null,
                        tint = RoseQuartzPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                    Text(
                        text = "Add Mobile Number",
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        color = RoseQuartzTextPrimary,
                        fontSize = 17.sp
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Choose a category and enter details manually, or scan/upload a QR code to fill automatically.",
                        fontSize = 11.sp,
                        color = RoseQuartzTextSecondary
                    )

                    // Shortcut buttons to auto-fill via QR scan or storage image
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                showAddContactDialog = false
                                showCameraScanner = true
                            },
                            shape = RoundedCornerShape(6.dp),
                            border = BorderStroke(1.dp, RoseQuartzPrimary),
                            modifier = Modifier
                                .weight(1f)
                                .height(32.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Icon(Icons.Filled.QrCodeScanner, contentDescription = null, modifier = Modifier.size(13.dp), tint = RoseQuartzPrimary)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Scan QR", fontSize = 10.sp, color = RoseQuartzPrimary, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = {
                                showAddContactDialog = false
                                qrImagePickerLauncher.launch("image/*")
                            },
                            shape = RoundedCornerShape(6.dp),
                            border = BorderStroke(1.dp, RoseQuartzPrimary),
                            modifier = Modifier
                                .weight(1.1f)
                                .height(32.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Icon(Icons.Filled.PhotoLibrary, contentDescription = null, modifier = Modifier.size(13.dp), tint = RoseQuartzPrimary)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Upload QR", fontSize = 10.sp, color = RoseQuartzPrimary, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Category Selector with Capacity Badges (Max 10 per category)
                    Text("Category (Max 10 numbers per category):", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = RoseQuartzTextPrimary)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf("Family", "Work", "Personal", "Utility").forEach { cat ->
                            val isSel = addCategory.equals(cat, ignoreCase = true)
                            val catCount = allContacts.count { it.category.equals(cat, ignoreCase = true) }
                            val isFull = catCount >= 10

                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(6.dp))
                                    .clickable { addCategory = cat },
                                shape = RoundedCornerShape(6.dp),
                                color = if (isSel) RoseQuartzPrimary else RoseQuartzBg,
                                border = BorderStroke(1.dp, if (isSel) RoseQuartzPrimary else RoseQuartzContainerHighest)
                            ) {
                                val emoji = when (cat) {
                                    "Family" -> "🏡"
                                    "Work" -> "💼"
                                    "Personal" -> "🌿"
                                    else -> "⚡"
                                }
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                ) {
                                    Text(text = emoji, fontSize = 12.sp)
                                    Text(
                                        text = cat,
                                        fontSize = 9.sp,
                                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSel) RoseQuartzOnPrimary else RoseQuartzTextPrimary
                                    )
                                    Text(
                                        text = "$catCount/10",
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSel) RoseQuartzOnPrimary.copy(alpha = 0.9f) else if (isFull) RoseQuartzAccentAmber else RoseQuartzTextMuted
                                    )
                                }
                            }
                        }
                    }

                    val currentCategoryCount = allContacts.count { it.category.equals(addCategory, ignoreCase = true) }
                    if (currentCategoryCount >= 10) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = RoseQuartzPrimaryContainer,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "⚠️ Limit Reached: Category '$addCategory' already has 10 numbers (Maximum allowed per category). Please remove a number to add a new one.",
                                fontSize = 10.sp,
                                color = RoseQuartzPrimary,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(6.dp)
                            )
                        }
                    }

                    OutlinedTextField(
                        value = addName,
                        onValueChange = { addName = it },
                        label = { Text("Contact Name") },
                        placeholder = { Text("e.g. Mom, Doctor, Project Lead") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = RoseQuartzPrimary,
                            unfocusedBorderColor = RoseQuartzContainerHighest,
                            focusedTextColor = RoseQuartzTextPrimary,
                            unfocusedTextColor = RoseQuartzTextPrimary
                        )
                    )

                    OutlinedTextField(
                        value = addPhone,
                        onValueChange = { addPhone = it },
                        label = { Text("Mobile Number") },
                        placeholder = { Text("+1 (555) 000-0000") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = RoseQuartzPrimary,
                            unfocusedBorderColor = RoseQuartzContainerHighest,
                            focusedTextColor = RoseQuartzTextPrimary,
                            unfocusedTextColor = RoseQuartzTextPrimary
                        )
                    )

                    OutlinedTextField(
                        value = addNote,
                        onValueChange = { addNote = it },
                        label = { Text("Note / Role (Optional)") },
                        placeholder = { Text("e.g. Emergency, Support Desk") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = RoseQuartzPrimary,
                            unfocusedBorderColor = RoseQuartzContainerHighest,
                            focusedTextColor = RoseQuartzTextPrimary,
                            unfocusedTextColor = RoseQuartzTextPrimary
                        )
                    )
                }
            },
            confirmButton = {
                val currentCategoryCount = allContacts.count { it.category.equals(addCategory, ignoreCase = true) }
                val isLimitReached = currentCategoryCount >= 10
                Button(
                    onClick = {
                        if (isLimitReached) {
                            Toast.makeText(context, "Cannot add: '$addCategory' already has the maximum 10 mobile numbers allowed.", Toast.LENGTH_LONG).show()
                            return@Button
                        }
                        if (addName.isNotBlank() && addPhone.isNotBlank()) {
                            onAddContact(addCategory, addName.trim(), addPhone.trim(), addNote.trim())
                            showAddContactDialog = false
                        }
                    },
                    enabled = !isLimitReached && addName.isNotBlank() && addPhone.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = RoseQuartzPrimary,
                        contentColor = RoseQuartzOnPrimary,
                        disabledContainerColor = RoseQuartzContainerHigh,
                        disabledContentColor = RoseQuartzTextMuted
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(if (isLimitReached) "Limit Reached (10/10)" else "Save Contact", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddContactDialog = false }) {
                    Text("Cancel", color = RoseQuartzTextSecondary)
                }
            }
        )
    }

    // ----------------------------------------------------
    // LIVE CAMERA QR SCANNER DIALOG
    // ----------------------------------------------------
    if (showCameraScanner) {
        CameraQrScannerDialog(
            onDismiss = { showCameraScanner = false },
            onQrScanned = { result ->
                showCameraScanner = false
                decodedQrResult = result
            },
            onPickFromStorage = {
                qrImagePickerLauncher.launch("image/*")
            }
        )
    }

    // ----------------------------------------------------
    // DECODED QR RESULT DIALOG (ALLOWS 1-TAP ADD TO CONTACTS)
    // ----------------------------------------------------
    decodedQrResult?.let { result ->
        QrResultDialog(
            result = result,
            onDismiss = { decodedQrResult = null },
            onAddContact = { cat, name, phone, note ->
                onAddContact(cat, name, phone, note)
            }
        )
    }

    // ----------------------------------------------------
    // DISPLAY CONTACT QR CODE DIALOG
    // ----------------------------------------------------
    selectedContactForQr?.let { contact ->
        ContactQrDisplayDialog(
            name = contact.name,
            phoneNumber = contact.phoneNumber,
            category = contact.category,
            onDismiss = { selectedContactForQr = null }
        )
    }

    // ----------------------------------------------------
    // NETWORK & QUEUE INSPECTOR DIALOG
    // ----------------------------------------------------
    if (showNetworkDialog && networkState != null) {
        NetworkQueueDialog(
            networkState = networkState,
            pendingSyncCount = pendingSyncCount,
            onSyncNow = {
                onSyncQueuedMessages?.invoke()
            },
            onSetSimulationMode = { mode ->
                onSetSimulationMode?.invoke(mode)
            },
            onDismiss = { showNetworkDialog = false }
        )
    }
}

@Composable
private fun DrawerNavItem(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() },
        color = if (isSelected) RoseQuartzPrimaryContainer else Color.Transparent,
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isSelected) RoseQuartzPrimary else RoseQuartzTextSecondary,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) RoseQuartzPrimary else RoseQuartzTextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun SideMenuContactCard(
    contact: CategoryContactEntity,
    onDelete: () -> Unit,
    onShowQr: () -> Unit,
    onCall: () -> Unit,
    onSms: () -> Unit,
    onCopy: () -> Unit
) {
    val categoryEmoji = when (contact.category.lowercase()) {
        "family" -> "🏡"
        "work" -> "💼"
        "personal" -> "🌿"
        "utility" -> "⚡"
        else -> "📌"
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = RoseQuartzBg.copy(alpha = 0.85f),
        border = BorderStroke(1.dp, RoseQuartzContainerHighest),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Row 1: Emoji, Name & Category Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(text = categoryEmoji, fontSize = 14.sp)
                Text(
                    text = contact.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = RoseQuartzTextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = RoseQuartzContainerHigh
                ) {
                    Text(
                        text = contact.category,
                        fontSize = 9.sp,
                        color = RoseQuartzPrimary,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            // Row 2: Action Buttons (QR, Call, SMS, Delete) cleanly centered/spread
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onCall,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = RoseQuartzPrimary),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.weight(1f).height(32.dp)
                ) {
                    Icon(Icons.Filled.Call, contentDescription = "Call", modifier = Modifier.size(13.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Call", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onSms,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = RoseQuartzPrimaryContainer),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.weight(1f).height(32.dp)
                ) {
                    Icon(Icons.Filled.Sms, contentDescription = "SMS", tint = RoseQuartzPrimary, modifier = Modifier.size(13.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("SMS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = RoseQuartzPrimary)
                }

                IconButton(
                    onClick = onShowQr,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(RoseQuartzContainerHigh)
                ) {
                    Icon(
                        imageVector = Icons.Filled.QrCode2,
                        contentDescription = "Show QR",
                        tint = RoseQuartzPrimary,
                        modifier = Modifier.size(15.dp)
                    )
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(RoseQuartzContainerHigh)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = "Delete",
                        tint = RoseQuartzTextMuted,
                        modifier = Modifier.size(15.dp)
                    )
                }
            }

            // Row 3: Phone number & Note
            Column(
                verticalArrangement = Arrangement.spacedBy(2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .clickable { onCopy() }
                    .padding(vertical = 2.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Phone,
                        contentDescription = null,
                        tint = RoseQuartzPrimary,
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = contact.phoneNumber,
                        fontSize = 12.sp,
                        color = RoseQuartzTextSecondary,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        imageVector = Icons.Outlined.ContentCopy,
                        contentDescription = "Copy",
                        tint = RoseQuartzTextMuted,
                        modifier = Modifier.size(12.dp)
                    )
                }

                if (contact.note.isNotBlank()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Info,
                            contentDescription = null,
                            tint = RoseQuartzTextMuted,
                            modifier = Modifier.size(11.dp)
                        )
                        Text(
                            text = contact.note,
                            fontSize = 11.sp,
                            color = RoseQuartzTextMuted,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}
