import re

with open("app/src/main/java/com/example/ui/screens/MusicScreen.kt", "r") as f:
    content = f.read()

items_block_old = """                // Music Tracks List
                items(processedTracks, key = { it.id }) { track ->
                    val isThisTrackPlaying = currentlyPlayingTrack?.id == track.id && isAudioPlaying
                    val isThisTrackLoaded = currentlyPlayingTrack?.id == track.id

                    MusicTrackRow(
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
                        onDownloadLocal = {
                            trackToDownload = track
                            val safeName = track.fileName.ifBlank { "${track.songName.replace(" ", "_")}.mp3" }
                            saveFileToStorageLauncher.launch(safeName)
                        },
                        onShareGoogleDrive = {
                            shareTrackToGoogleDrive(context, track)
                        },
                        onOpenPlayerSheet = {
                            if (!isThisTrackLoaded) {
                                MusicPlayerManager.playTrack(track, processedTracks, context)
                            }
                            showFullPlayerSheet = true
                        }
                    )
                }"""

items_block_new = """                // Music Tracks List (2 columns)
                val chunkedTracks = processedTracks.chunked(2)
                items(chunkedTracks, key = { chunk -> chunk.map { it.id }.joinToString("_") }) { rowTracks ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        for (track in rowTracks) {
                            val isThisTrackPlaying = currentlyPlayingTrack?.id == track.id && isAudioPlaying
                            val isThisTrackLoaded = currentlyPlayingTrack?.id == track.id

                            MusicTrackRow(
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
                                onDownloadLocal = {
                                    trackToDownload = track
                                    val safeName = track.fileName.ifBlank { "${track.songName.replace(" ", "_")}.mp3" }
                                    saveFileToStorageLauncher.launch(safeName)
                                },
                                onShareGoogleDrive = {
                                    shareTrackToGoogleDrive(context, track)
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

with open("app/src/main/java/com/example/ui/screens/MusicScreen.kt", "w") as f:
    f.write(content)
