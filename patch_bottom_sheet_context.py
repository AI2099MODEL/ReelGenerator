with open('app/src/main/java/com/example/ui/screens/RingtonesScreen.kt', 'r') as f:
    text = f.read()

import re

# Find FullScorePlayerBottomSheet and add context at the top of the function
text = text.replace(
'''private fun FullScorePlayerBottomSheet(
    track: MusicTrackEntity,
    isPlaying: Boolean,
    currentPositionMs: Long,
    durationMs: Long,
    isLooping: Boolean,
    isShuffle: Boolean,
    playbackSpeed: Float,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrev: () -> Unit,
    onSeek: (Long) -> Unit,
    onToggleLoop: () -> Unit,
    onToggleShuffle: () -> Unit,
    onSpeedChange: (Float) -> Unit,
    onToggleFavorite: () -> Unit,
    onDismiss: () -> Unit
) {''',
'''private fun FullScorePlayerBottomSheet(
    track: MusicTrackEntity,
    isPlaying: Boolean,
    currentPositionMs: Long,
    durationMs: Long,
    isLooping: Boolean,
    isShuffle: Boolean,
    playbackSpeed: Float,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrev: () -> Unit,
    onSeek: (Long) -> Unit,
    onToggleLoop: () -> Unit,
    onToggleShuffle: () -> Unit,
    onSpeedChange: (Float) -> Unit,
    onToggleFavorite: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current''')

with open('app/src/main/java/com/example/ui/screens/RingtonesScreen.kt', 'w') as f:
    f.write(text)
