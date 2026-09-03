package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MusicTrackEntity
import com.example.ui.components.LedgerTopHeader
import com.example.ui.theme.*
import com.example.util.MusicPlayerManager
import com.example.util.RingtoneUtil
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RingtonesScreen(
    tracks: List<MusicTrackEntity>,
    onAddTrack: (
        songName: String,
        albumName: String,
        category: String,
        artist: String,
        uriString: String,
        fileName: String,
        fileSizeBytes: Long,
        durationMs: Long,
        sourceType: String,
        notes: String
    ) -> Unit,
    onUpdateTrack: (MusicTrackEntity) -> Unit,
    onDeleteTrack: (MusicTrackEntity) -> Unit,
    onToggleFavorite: (Long, Boolean) -> Unit,
    onMenuClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryFilter by remember { mutableStateOf("All") }
    var currentSortOption by remember { mutableStateOf(MusicSortOption.RECENT) }
    var showSortMenu by remember { mutableStateOf(false) }

    // Dialog States
    var showAddDialog by remember { mutableStateOf(false) }
    var editingTrack by remember { mutableStateOf<MusicTrackEntity?>(null) }
    var trackToDelete by remember { mutableStateOf<MusicTrackEntity?>(null) }
    var showFullPlayerSheet by remember { mutableStateOf(false) }

    // Player state from MusicPlayerManager
    val currentlyPlayingTrack by MusicPlayerManager.currentTrack.collectAsState()
    val isAudioPlaying by MusicPlayerManager.isPlaying.collectAsState()
    val playPositionMs by MusicPlayerManager.currentPositionMs.collectAsState()
    val totalDurationMs by MusicPlayerManager.durationMs.collectAsState()
    val isLooping by MusicPlayerManager.isLooping.collectAsState()
    val isShuffle by MusicPlayerManager.isShuffle.collectAsState()
    val playbackSpeed by MusicPlayerManager.playbackSpeed.collectAsState()

    // Export / Download to Local Storage Document Creator
    var trackToDownload by remember { mutableStateOf<MusicTrackEntity?>(null) }
    val saveFileToStorageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("audio/mpeg")
    ) { destUri: Uri? ->
        if (destUri != null && trackToDownload != null) {
            val track = trackToDownload!!
            exportTrackDataToUri(context, track, destUri)
            Toast.makeText(context, "Saved '${track.songName}' to Local Storage!", Toast.LENGTH_LONG).show()
            trackToDownload = null
        }
    }

    // Extract categories
    val allCategories = remember(tracks) {
        val dynamicCats = tracks.map { it.category.trim() }.filter { it.isNotBlank() }.distinct()
        val defaultList = listOf("Favorites ❤️")
        (defaultList + dynamicCats).distinct()
    }

    // Filtered & Sorted Tracks
    val processedTracks = remember(tracks, searchQuery, selectedCategoryFilter, currentSortOption) {
        var list = tracks

        // Search Filter
        if (searchQuery.isNotBlank()) {
            val q = searchQuery.trim().lowercase()
            list = list.filter {
                it.songName.lowercase().contains(q) ||
                        it.albumName.lowercase().contains(q) ||
                        it.category.lowercase().contains(q) ||
                        it.artist.lowercase().contains(q)
            }
        }

        // Category Filter
        if (selectedCategoryFilter != "All") {
            list = if (selectedCategoryFilter == "Favorites ❤️") {
                list.filter { it.isFavorite }
            } else {
                list.filter { it.category.equals(selectedCategoryFilter, ignoreCase = true) }
            }
        }

        // Sorting
        when (currentSortOption) {
            MusicSortOption.ALBUM_ASC -> list.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.albumName })
            MusicSortOption.ALBUM_DESC -> list.sortedWith(compareByDescending(String.CASE_INSENSITIVE_ORDER) { it.albumName })
            MusicSortOption.FAVORITES_FIRST -> list.sortedWith(compareByDescending<MusicTrackEntity> { it.isFavorite }.thenBy { it.songName.lowercase() })
            MusicSortOption.CATEGORY -> list.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.category })
            MusicSortOption.SONG_ASC -> list.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.songName })
            MusicSortOption.RECENT -> list.sortedByDescending { it.addedTimestamp }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top Header
            LedgerTopHeader(
                title = "Ringtones",
                actionIcon = Icons.Filled.AddAlert,
                onActionClick = {
                    showAddDialog = true
                },
                onMenuClick = onMenuClick
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(top = 4.dp, bottom = if (currentlyPlayingTrack != null) 90.dp else 24.dp)
            ) {
                // Info Header Ribbon
                item {
                    RingtonesInfoRibbon(
                        totalRingtones = tracks.size,
                        favoriteCount = tracks.count { it.isFavorite }
                    )
                }

                // Search & Sort Bar
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Search ringtones...", fontSize = 13.sp) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Search",
                                    tint = RoseQuartzPrimary
                                )
                            },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { searchQuery = "" }) {
                                        Icon(Icons.Default.Close, contentDescription = "Clear search", tint = RoseQuartzTextSecondary)
                                    }
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = RoseQuartzPrimary,
                                unfocusedBorderColor = RoseQuartzContainerHighest,
                                focusedContainerColor = RoseQuartzContainerLowest,
                                unfocusedContainerColor = RoseQuartzContainerLowest
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp)
                        )

                        // Sort Dropdown Button
                        Box {
                            OutlinedButton(
                                onClick = { showSortMenu = true },
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, RoseQuartzContainerHighest),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = RoseQuartzContainerLowest
                                ),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                                modifier = Modifier.height(52.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = currentSortOption.icon,
                                        contentDescription = "Sort",
                                        tint = RoseQuartzPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = "Sort",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = RoseQuartzTextPrimary
                                    )
                                }
                            }

                            DropdownMenu(
                                expanded = showSortMenu,
                                onDismissRequest = { showSortMenu = false },
                                modifier = Modifier.background(RoseQuartzContainerLowest)
                            ) {
                                MusicSortOption.values().forEach { option ->
                                    DropdownMenuItem(
                                        text = {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Icon(
                                                    imageVector = option.icon,
                                                    contentDescription = null,
                                                    tint = if (currentSortOption == option) RoseQuartzPrimary else RoseQuartzTextSecondary,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                                Text(
                                                    text = option.label,
                                                    fontWeight = if (currentSortOption == option) FontWeight.Bold else FontWeight.Normal,
                                                    color = if (currentSortOption == option) RoseQuartzPrimary else RoseQuartzTextPrimary,
                                                    fontSize = 13.sp
                                                )
                                            }
                                        },
                                        onClick = {
                                            currentSortOption = option
                                            showSortMenu = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                // Category Filter Pills
                item {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(allCategories) { category ->
                            val isSelected = selectedCategoryFilter == category
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedCategoryFilter = if (selectedCategoryFilter == category) "All" else category },
                                label = {
                                    Text(
                                        text = category,
                                        fontSize = 9.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = RoseQuartzPrimary,
                                    selectedLabelColor = Color.White,
                                    containerColor = RoseQuartzContainerLowest,
                                    labelColor = RoseQuartzTextPrimary
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = isSelected,
                                    borderColor = if (isSelected) RoseQuartzPrimary else RoseQuartzContainerHighest
                                )
                            )
                        }
                    }
                }


                // Empty State
                if (processedTracks.isEmpty()) {
                    item {
                        RingtoneEmptyState(
                            hasSearch = searchQuery.isNotBlank() || selectedCategoryFilter != "All",
                            onReset = {
                                searchQuery = ""
                                selectedCategoryFilter = "All"
                            },
                            onAddNew = {
                                showAddDialog = true
                            }
                        )
                    }
                }

                // Ringtones List (2 columns)
                val chunkedTracks = processedTracks.chunked(2)
                items(chunkedTracks, key = { chunk -> chunk.map { it.id }.joinToString("_") }) { rowTracks ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        for (track in rowTracks) {
                            val isThisTrackPlaying = currentlyPlayingTrack?.id == track.id && isAudioPlaying
                            val isThisTrackLoaded = currentlyPlayingTrack?.id == track.id

                            RingtoneTrackRow(
                                modifier = Modifier.weight(1f),
                                track = track,
                                isPlaying = isThisTrackPlaying,
                                isLoaded = isThisTrackLoaded,
                                currentPositionMs = if (isThisTrackLoaded) playPositionMs else 0L,
                                durationMs = if (isThisTrackLoaded && totalDurationMs > 0) totalDurationMs else track.durationMs,
                                onPlayClick = {
                                    if (isThisTrackPlaying) {
                                        MusicPlayerManager.pause()
                                    } else if (isThisTrackLoaded) {
                                        MusicPlayerManager.resume(context)
                                    } else {
                                        MusicPlayerManager.playTrack(track, processedTracks, context)
                                    }
                                },
                                onToggleFavorite = { onToggleFavorite(track.id, !track.isFavorite) },
                                onEdit = { editingTrack = track },
                                onDelete = { trackToDelete = track },
                                onSetAsRingtone = {
                                    RingtoneUtil.setRingtone(context, track.uriString)
                                },
                                onOpenPlayerSheet = {
                                    if (!isThisTrackLoaded) {
                                        MusicPlayerManager.playTrack(track, processedTracks, context)
                                    }
                                    showFullPlayerSheet = true
                                }
                            )
                        }
                        if (rowTracks.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }

        // Persistent Mini Player Floating Bar
        if (currentlyPlayingTrack != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                FloatingMiniPlayerBar(
                    track = currentlyPlayingTrack!!,
                    isPlaying = isAudioPlaying,
                    currentPositionMs = playPositionMs,
                    durationMs = totalDurationMs,
                    onPlayPause = { MusicPlayerManager.togglePlayPause(context) },
                    onNext = { MusicPlayerManager.playNext(context) },
                    onPrev = { MusicPlayerManager.playPrevious(context) },
                    onClick = { showFullPlayerSheet = true }
                )
            }
        }
    }

    // Add Ringtone Dialog
    if (showAddDialog) {
        RingtoneEditDialog(
            initialTrack = null,
            onDismiss = { showAddDialog = false },
            onSave = { songName, albumName, category, artist, uriStr, fileName, fileSize, duration, source, notes ->
                onAddTrack(songName, albumName, category, artist, uriStr, fileName, fileSize, duration, source, notes)
                showAddDialog = false
                Toast.makeText(context, "Added ringtone '$songName'!", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Edit Ringtone Dialog
    if (editingTrack != null) {
        RingtoneEditDialog(
            initialTrack = editingTrack,
            onDismiss = { editingTrack = null },
            onSave = { songName, albumName, category, artist, uriStr, fileName, fileSize, duration, source, notes ->
                val updated = editingTrack!!.copy(
                    songName = songName,
                    albumName = albumName,
                    category = category,
                    artist = artist,
                    uriString = uriStr.ifBlank { editingTrack!!.uriString },
                    fileName = fileName.ifBlank { editingTrack!!.fileName },
                    fileSizeBytes = if (fileSize > 0) fileSize else editingTrack!!.fileSizeBytes,
                    sourceType = source,
                    notes = notes
                )
                onUpdateTrack(updated)
                editingTrack = null
                Toast.makeText(context, "Updated ringtone details!", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Delete Confirmation Dialog
    if (trackToDelete != null) {
        AlertDialog(
            onDismissRequest = { trackToDelete = null },
            shape = RoundedCornerShape(16.dp),
            containerColor = RoseQuartzContainerLowest,
            title = {
                Text(
                    text = "Delete Ringtone?",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = RoseQuartzTextPrimary
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to delete '${trackToDelete?.songName}' from your ringtone library?",
                    fontSize = 14.sp,
                    color = RoseQuartzTextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val track = trackToDelete!!
                        if (currentlyPlayingTrack?.id == track.id) {
                            MusicPlayerManager.stop()
                        }
                        onDeleteTrack(track)
                        trackToDelete = null
                        Toast.makeText(context, "Ringtone deleted", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { trackToDelete = null }) {
                    Text("Cancel", color = RoseQuartzTextSecondary)
                }
            }
        )
    }
}

@Composable
private fun RingtonesInfoRibbon(
    totalRingtones: Int,
    favoriteCount: Int
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = RoseQuartzPrimaryContainer,
        border = BorderStroke(1.dp, RoseQuartzPrimary.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Filled.NotificationsActive, contentDescription = null, tint = RoseQuartzPrimary, modifier = Modifier.size(14.dp))
                Column {
                    Text(text = "$totalRingtones Ringtones", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = RoseQuartzPrimary)
                    Text(text = "Available Tones", fontSize = 8.sp, color = RoseQuartzTextSecondary)
                }
            }

            Box(modifier = Modifier.height(24.dp).width(1.dp).background(RoseQuartzPrimary.copy(alpha = 0.3f)))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Filled.Favorite, contentDescription = null, tint = RoseQuartzPrimary, modifier = Modifier.size(14.dp))
                Column {
                    Text(text = "$favoriteCount Loved", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = RoseQuartzPrimary)
                    Text(text = "Favorites", fontSize = 8.sp, color = RoseQuartzTextSecondary)
                }
            }
        }
    }
}

@Composable
private fun RingtoneEmptyState(
    modifier: Modifier = Modifier,
    hasSearch: Boolean,
    onReset: () -> Unit,
    onAddNew: () -> Unit
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        shape = RoundedCornerShape(16.dp),
        color = RoseQuartzContainerLowest,
        border = BorderStroke(1.dp, RoseQuartzContainerHighest)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.NotificationsNone,
                contentDescription = null,
                tint = RoseQuartzPrimary,
                modifier = Modifier.size(48.dp)
            )

            Text(
                text = if (hasSearch) "No Matching Ringtones Found" else "No Ringtones Added Yet",
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = RoseQuartzTextPrimary
            )

            Text(
                text = if (hasSearch) "Try adjusting your search query." else "Upload short audio files or pick Hindi/English ringtones.",
                fontSize = 9.sp,
                color = RoseQuartzTextSecondary,
                textAlign = TextAlign.Center
            )

            if (hasSearch) {
                OutlinedButton(onClick = onReset, shape = RoundedCornerShape(10.dp)) {
                    Text("Clear Filters", color = RoseQuartzPrimary)
                }
            } else {
                Button(
                    onClick = onAddNew,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = RoseQuartzPrimary)
                ) {
                    Icon(Icons.Filled.AddAlert, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Upload Ringtone")
                }
            }
        }
    }
}

@Composable
private fun RingtoneTrackRow(
    modifier: Modifier = Modifier,
    track: MusicTrackEntity,
    isPlaying: Boolean,
    isLoaded: Boolean,
    currentPositionMs: Long,
    durationMs: Long,
    onPlayClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onSetAsRingtone: () -> Unit,
    onOpenPlayerSheet: () -> Unit
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onOpenPlayerSheet() },
        shape = RoundedCornerShape(12.dp),
        color = if (isPlaying) RoseQuartzPrimaryContainer.copy(alpha = 0.5f) else RoseQuartzContainerLowest,
        border = BorderStroke(1.dp, if (isPlaying) RoseQuartzPrimary else RoseQuartzContainerHighest),
        shadowElevation = 0.5.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().wrapContentHeight(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Play Button
                Surface(
                    shape = CircleShape,
                    color = if (isPlaying) RoseQuartzPrimary else RoseQuartzPrimaryContainer,
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .clickable { onPlayClick() }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            tint = if (isPlaying) Color.White else RoseQuartzPrimary,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                // Ringtone title details
                Column(modifier = Modifier.weight(1f).wrapContentHeight()) {
                    Text(
                        text = track.songName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = RoseQuartzTextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    if (track.artist.isNotBlank()) {
                        Text(
                            text = track.artist,
                            fontSize = 9.sp,
                            color = RoseQuartzTextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                IconButton(onClick = onToggleFavorite, modifier = Modifier.size(24.dp)) {
                    Icon(
                        imageVector = if (track.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (track.isFavorite) RoseQuartzPrimary else RoseQuartzTextMuted,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // Progress bar
            if (isLoaded) {
                Spacer(modifier = Modifier.height(4.dp))
                val progress = if (durationMs > 0) (currentPositionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f) else 0f
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp)
                        .clip(RoundedCornerShape(1.dp)),
                    color = RoseQuartzPrimary,
                    trackColor = RoseQuartzContainerHighest
                )
            }

            // Actions
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth().wrapContentHeight(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    onClick = onSetAsRingtone,
                    shape = RoundedCornerShape(6.dp),
                    color = RoseQuartzPrimary,
                    modifier = Modifier.height(22.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 6.dp)
                    ) {
                        Icon(Icons.Filled.RingVolume, contentDescription = null, tint = Color.White, modifier = Modifier.size(10.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Set", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    IconButton(onClick = onEdit, modifier = Modifier.size(22.dp)) {
                        Icon(Icons.Outlined.Edit, contentDescription = "Edit", tint = RoseQuartzTextSecondary, modifier = Modifier.size(14.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(22.dp)) {
                        Icon(Icons.Outlined.Delete, contentDescription = "Delete", tint = RoseQuartzTextMuted, modifier = Modifier.size(14.dp))
                    }
                }
            }
        }
    }
}

/**
 * Ringtone Edit Dialog.
 * Only Song Name is mandatory!
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RingtoneEditDialog(
    initialTrack: MusicTrackEntity?,
    onDismiss: () -> Unit,
    onSave: (
        songName: String,
        albumName: String,
        category: String,
        artist: String,
        uriStr: String,
        fileName: String,
        fileSize: Long,
        duration: Long,
        source: String,
        notes: String
    ) -> Unit
) {
    val context = LocalContext.current
    var songName by remember { mutableStateOf(initialTrack?.songName ?: "") }
    var albumName by remember { mutableStateOf(initialTrack?.albumName ?: "Ringtones") }
    var category by remember { mutableStateOf(initialTrack?.category ?: "🔔 Ringtone") }
    var artist by remember { mutableStateOf(initialTrack?.artist ?: "") }
    var sourceType by remember { mutableStateOf(initialTrack?.sourceType ?: "LOCAL_STORAGE") }
    var notes by remember { mutableStateOf(initialTrack?.notes ?: "") }
    var selectedUriStr by remember { mutableStateOf(initialTrack?.uriString ?: "") }
    var selectedFileName by remember { mutableStateOf(initialTrack?.fileName ?: "") }
    var selectedFileSize by remember { mutableStateOf(initialTrack?.fileSizeBytes ?: 0L) }

    val emojiCategories = listOf("🔔 Ringtone", "🔥 Loud", "🎶 Melody", "🎼 Classical", "🎸 Acoustic", "📻 Retro", "❤️ Romantic", "✨ Soft")

    // Document Picker for Audio files
    val audioPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (e: Exception) {
                // Ignore
            }

            selectedUriStr = uri.toString()
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val nameIdx = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    val sizeIdx = it.getColumnIndex(OpenableColumns.SIZE)
                    if (nameIdx != -1) {
                        val name = it.getString(nameIdx)
                        selectedFileName = name
                        if (songName.isBlank()) {
                            songName = name.substringBeforeLast(".")
                        }
                    }
                    if (sizeIdx != -1) {
                        selectedFileSize = it.getLong(sizeIdx)
                    }
                }
            }
            Toast.makeText(context, "Ringtone file attached successfully!", Toast.LENGTH_SHORT).show()
        }
    }

    // MANDATORY field: songName is not blank!
    val canSave = songName.isNotBlank()

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(20.dp),
        containerColor = RoseQuartzContainerLowest,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.NotificationsActive,
                    contentDescription = null,
                    tint = RoseQuartzPrimary
                )
                Text(
                    text = if (initialTrack != null) "Edit Ringtone" else "New Ringtone",
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = RoseQuartzTextPrimary
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // File Selection
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = RoseQuartzPrimaryContainer.copy(alpha = 0.5f),
                    border = BorderStroke(1.dp, RoseQuartzPrimary.copy(alpha = 0.4f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            audioPickerLauncher.launch(arrayOf("audio/*"))
                        }
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.AudioFile,
                                contentDescription = null,
                                tint = RoseQuartzPrimary,
                                modifier = Modifier.size(22.dp)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (selectedFileName.isNotBlank()) "Attached: $selectedFileName" else "Select Ringtone File (Local / Drive)",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = RoseQuartzPrimary
                                )
                                if (selectedFileSize > 0) {
                                    Text(
                                        text = "${selectedFileSize / 1024} KB",
                                        fontSize = 8.sp,
                                        color = RoseQuartzTextSecondary
                                    )
                                }
                            }
                            Button(
                                onClick = { audioPickerLauncher.launch(arrayOf("audio/*")) },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = RoseQuartzPrimary),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                modifier = Modifier.height(30.dp)
                            ) {
                                Text("Browse", fontSize = 11.sp)
                            }
                        }
                    }
                }

                // Ringtone Name (MANDATORY)
                Column {
                    Text("Ringtone Name *", fontSize = 9.sp, fontWeight = FontWeight.SemiBold, color = RoseQuartzTextPrimary)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = songName,
                        onValueChange = { songName = it },
                        placeholder = { Text("e.g. Flute Ringtone") },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = RoseQuartzPrimary,
                            unfocusedBorderColor = RoseQuartzContainerHighest,
                            focusedContainerColor = RoseQuartzContainerLowest,
                            unfocusedContainerColor = RoseQuartzContainerLowest
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Category Selector with Emoji Chips
                Column {
                    Text("Category & Emoji", fontSize = 9.sp, fontWeight = FontWeight.SemiBold, color = RoseQuartzTextPrimary)
                    Spacer(modifier = Modifier.height(4.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(emojiCategories) { preset ->
                            val isSelected = category.equals(preset, ignoreCase = true)
                            FilterChip(
                                selected = isSelected,
                                onClick = { category = preset },
                                label = { Text(preset, fontSize = 11.sp) },
                                shape = RoundedCornerShape(8.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = RoseQuartzPrimary,
                                    selectedLabelColor = Color.White,
                                    containerColor = RoseQuartzContainerMedium,
                                    labelColor = RoseQuartzTextPrimary
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = category,
                        onValueChange = { category = it },
                        placeholder = { Text("e.g. 🔔 Ringtone, 🔥 Loud") },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = RoseQuartzPrimary,
                            unfocusedBorderColor = RoseQuartzContainerHighest,
                            focusedContainerColor = RoseQuartzContainerLowest,
                            unfocusedContainerColor = RoseQuartzContainerLowest
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Artist / Source Name
                Column {
                    Text("Artist / Movie Name (Optional)", fontSize = 9.sp, fontWeight = FontWeight.SemiBold, color = RoseQuartzTextPrimary)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = artist,
                        onValueChange = { artist = it },
                        placeholder = { Text("e.g. A.R. Rahman") },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = RoseQuartzPrimary,
                            unfocusedBorderColor = RoseQuartzContainerHighest,
                            focusedContainerColor = RoseQuartzContainerLowest,
                            unfocusedContainerColor = RoseQuartzContainerLowest
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Source Type Selector
                Column {
                    Text("Source Provider", fontSize = 9.sp, fontWeight = FontWeight.SemiBold, color = RoseQuartzTextPrimary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (sourceType == "LOCAL_STORAGE") RoseQuartzPrimaryContainer else RoseQuartzContainerMedium,
                            border = BorderStroke(1.dp, if (sourceType == "LOCAL_STORAGE") RoseQuartzPrimary else RoseQuartzContainerHighest),
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    sourceType = "LOCAL_STORAGE"
                                    audioPickerLauncher.launch(arrayOf("audio/*"))
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                RadioButton(
                                    selected = sourceType == "LOCAL_STORAGE",
                                    onClick = {
                                        sourceType = "LOCAL_STORAGE"
                                        audioPickerLauncher.launch(arrayOf("audio/*"))
                                    },
                                    colors = RadioButtonDefaults.colors(selectedColor = RoseQuartzPrimary)
                                )
                                Text("📱 Local Storage", fontSize = 9.sp, color = RoseQuartzTextPrimary)
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (sourceType == "GOOGLE_DRIVE") RoseQuartzPrimaryContainer else RoseQuartzContainerMedium,
                            border = BorderStroke(1.dp, if (sourceType == "GOOGLE_DRIVE") RoseQuartzPrimary else RoseQuartzContainerHighest),
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    sourceType = "GOOGLE_DRIVE"
                                    audioPickerLauncher.launch(arrayOf("audio/*"))
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                RadioButton(
                                    selected = sourceType == "GOOGLE_DRIVE",
                                    onClick = {
                                        sourceType = "GOOGLE_DRIVE"
                                        audioPickerLauncher.launch(arrayOf("audio/*"))
                                    },
                                    colors = RadioButtonDefaults.colors(selectedColor = RoseQuartzPrimary)
                                )
                                Text("☁️ Google Drive", fontSize = 9.sp, color = RoseQuartzTextPrimary)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        songName.trim(),
                        albumName.trim().ifBlank { "Ringtones" },
                        category.trim().ifBlank { "🔔 Ringtone" },
                        artist.trim(),
                        selectedUriStr,
                        selectedFileName,
                        selectedFileSize,
                        if (initialTrack != null) initialTrack.durationMs else 30000L,
                        sourceType,
                        notes.trim()
                    )
                },
                enabled = canSave,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = RoseQuartzPrimary,
                    disabledContainerColor = RoseQuartzContainerHighest
                )
            ) {
                Text(
                    text = if (initialTrack != null) "Update Ringtone" else "Save Ringtone",
                    color = if (canSave) Color.White else RoseQuartzTextSecondary
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = RoseQuartzTextSecondary)
            }
        }
    )
}

@Composable
private fun FloatingMiniPlayerBar(
    modifier: Modifier = Modifier,
    track: MusicTrackEntity,
    isPlaying: Boolean,
    currentPositionMs: Long,
    durationMs: Long,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrev: () -> Unit,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        color = RoseQuartzPrimaryContainer,
        border = BorderStroke(1.dp, RoseQuartzPrimary.copy(alpha = 0.5f)),
        shadowElevation = 6.dp
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.NotificationsActive,
                    contentDescription = null,
                    tint = RoseQuartzPrimary,
                    modifier = Modifier.size(14.dp)
                )

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = track.songName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = RoseQuartzPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (track.artist.isNotBlank()) {
                        Text(
                            text = track.artist,
                            fontSize = 11.sp,
                            color = RoseQuartzTextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                IconButton(onClick = onPlayPause, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = "Play/Pause",
                        tint = RoseQuartzPrimary,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            val progress = if (durationMs > 0) (currentPositionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f) else 0f
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp),
                color = RoseQuartzPrimary,
                trackColor = RoseQuartzContainerHighest
            )
        }
    }
}

private fun exportTrackDataToUri(context: Context, track: MusicTrackEntity, uri: Uri) {
    try {
        context.contentResolver.openOutputStream(uri)?.use { stream ->
            val info = "Ringtone: ${track.songName}\nCategory: ${track.category}\nArtist: ${track.artist}\nNotes: ${track.notes}\n"
            stream.write(info.toByteArray())
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}
