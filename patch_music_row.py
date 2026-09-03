import re

with open("app/src/main/java/com/example/ui/screens/MusicScreen.kt", "r") as f:
    content = f.read()

old_row = """@Composable
private fun MusicTrackRow(
    track: MusicTrackEntity,
    isPlaying: Boolean,
    isLoaded: Boolean,
    currentPositionMs: Long,
    durationMs: Long,
    onPlayClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onDownloadLocal: () -> Unit,
    onShareGoogleDrive: () -> Unit,
    onOpenPlayerSheet: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onOpenPlayerSheet() },
        shape = RoundedCornerShape(16.dp),
        color = if (isPlaying) RoseQuartzPrimaryContainer.copy(alpha = 0.5f) else RoseQuartzContainerLowest,
        border = BorderStroke(1.dp, if (isPlaying) RoseQuartzPrimary else RoseQuartzContainerHighest),
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Play / Pause Circle
                Surface(
                    shape = CircleShape,
                    color = if (isPlaying) RoseQuartzPrimary else RoseQuartzPrimaryContainer,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .clickable { onPlayClick() }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            tint = if (isPlaying) Color.White else RoseQuartzPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                // Song details
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = track.songName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = RoseQuartzTextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        if (track.category.isNotBlank()) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = RoseQuartzContainerHigh
                            ) {
                                Text(
                                    text = track.category,
                                    fontSize = 10.sp,
                                    color = RoseQuartzPrimary,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (track.artist.isNotBlank()) {
                            Text(
                                text = track.artist,
                                fontSize = 12.sp,
                                color = RoseQuartzTextSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        if (track.albumName.isNotBlank()) {
                            Text(
                                text = "•  ${track.albumName}",
                                fontSize = 11.sp,
                                color = RoseQuartzTextMuted,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                // Favorite button
                IconButton(onClick = onToggleFavorite) {
                    Icon(
                        imageVector = if (track.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (track.isFavorite) RoseQuartzPrimary else RoseQuartzTextMuted
                    )
                }
            }

            // Progress bar if loaded or playing
            if (isLoaded) {
                Spacer(modifier = Modifier.height(8.dp))
                val progress = if (durationMs > 0) (currentPositionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f) else 0f
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = RoseQuartzPrimary,
                    trackColor = RoseQuartzContainerHighest
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = formatMs(currentPositionMs), fontSize = 10.sp, color = RoseQuartzTextMuted)
                    Text(text = formatMs(durationMs), fontSize = 10.sp, color = RoseQuartzTextMuted)
                }
            }

            // Actions row
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    AssistChip(
                        onClick = onDownloadLocal,
                        label = { Text("Save Local", fontSize = 10.sp) },
                        leadingIcon = { Icon(Icons.Filled.Download, contentDescription = null, modifier = Modifier.size(12.dp)) },
                        shape = RoundedCornerShape(6.dp)
                    )
                    AssistChip(
                        onClick = onShareGoogleDrive,
                        label = { Text("Drive", fontSize = 10.sp) },
                        leadingIcon = { Icon(Icons.Filled.CloudUpload, contentDescription = null, modifier = Modifier.size(12.dp)) },
                        shape = RoundedCornerShape(6.dp)
                    )
                }

                Row {
                    IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Outlined.Edit, contentDescription = "Edit", tint = RoseQuartzTextSecondary, modifier = Modifier.size(16.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Outlined.Delete, contentDescription = "Delete", tint = RoseQuartzTextMuted, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}"""

new_row = """@Composable
private fun MusicTrackRow(
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
    onDownloadLocal: () -> Unit,
    onShareGoogleDrive: () -> Unit,
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

                // Song details
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

            // Actions row
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth().wrapContentHeight(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    Surface(
                        onClick = onDownloadLocal,
                        shape = RoundedCornerShape(4.dp),
                        color = RoseQuartzContainerHigh,
                        modifier = Modifier.height(22.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 4.dp)) {
                            Icon(Icons.Filled.Download, contentDescription = null, tint = RoseQuartzTextSecondary, modifier = Modifier.size(10.dp))
                        }
                    }
                    Surface(
                        onClick = onShareGoogleDrive,
                        shape = RoundedCornerShape(4.dp),
                        color = RoseQuartzContainerHigh,
                        modifier = Modifier.height(22.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 4.dp)) {
                            Icon(Icons.Filled.CloudUpload, contentDescription = null, tint = RoseQuartzTextSecondary, modifier = Modifier.size(10.dp))
                        }
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
}"""

content = content.replace(old_row, new_row)

with open("app/src/main/java/com/example/ui/screens/MusicScreen.kt", "w") as f:
    f.write(content)
