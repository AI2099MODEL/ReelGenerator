import re

with open("app/src/main/java/com/example/ui/screens/RingtonesScreen.kt", "r") as f:
    content = f.read()

# Modify RingtoneTrackRow signature
content = content.replace(
    "private fun RingtoneTrackRow(\n    track: MusicTrackEntity,",
    "private fun RingtoneTrackRow(\n    modifier: Modifier = Modifier,\n    track: MusicTrackEntity,"
)

# Apply modifier to Surface
content = content.replace(
    "    Surface(\n        modifier = Modifier\n            .fillMaxWidth()",
    "    Surface(\n        modifier = modifier\n            .fillMaxWidth()"
)

# Reduce padding
content = content.replace(
    "                .padding(14.dp)",
    "                .padding(10.dp)"
)

content = content.replace(
    "horizontalArrangement = Arrangement.spacedBy(12.dp)",
    "horizontalArrangement = Arrangement.spacedBy(8.dp)"
)

# Play button size
content = content.replace(
    ".size(44.dp)",
    ".size(36.dp)"
)
content = content.replace(
    "modifier = Modifier.size(24.dp)",
    "modifier = Modifier.size(20.dp)"
)

# Title font size
content = content.replace(
    "fontSize = 15.sp,",
    "fontSize = 13.sp,"
)

# Category tag font size
content = content.replace(
    "fontSize = 10.sp,",
    "fontSize = 8.sp,"
)

# Artist font size
content = content.replace(
    "fontSize = 12.sp,",
    "fontSize = 10.sp,"
)

# Button text
content = content.replace(
    "Text(\"Set as Ringtone\", fontSize = 11.sp, fontWeight = FontWeight.Bold)",
    "Text(\"Set\", fontSize = 10.sp, fontWeight = FontWeight.Bold)"
)

# Icon buttons size
content = content.replace(
    "modifier = Modifier.size(32.dp)",
    "modifier = Modifier.size(28.dp)"
)

# Now, the items block
items_block_old = """                // Ringtones List
                items(processedTracks, key = { it.id }) { track ->
                    val isThisTrackPlaying = currentlyPlayingTrack?.id == track.id && isAudioPlaying
                    val isThisTrackLoaded = currentlyPlayingTrack?.id == track.id

                    RingtoneTrackRow(
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
                }"""

items_block_new = """                // Ringtones List (2 columns)
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
                }"""

content = content.replace(items_block_old, items_block_new)

with open("app/src/main/java/com/example/ui/screens/RingtonesScreen.kt", "w") as f:
    f.write(content)

