with open('app/src/main/java/com/example/ui/screens/RingtonesScreen.kt', 'r') as f:
    text = f.read()

# Change `val context = androidx.compose.ui.platform.LocalContext.current` to `val ctx = androidx.compose.ui.platform.LocalContext.current`
text = text.replace('val context = androidx.compose.ui.platform.LocalContext.current', 'val ctx = androidx.compose.ui.platform.LocalContext.current')
text = text.replace('com.example.util.RingtoneUtil.setRingtone(context, track.uriString)', 'com.example.util.RingtoneUtil.setRingtone(ctx, track.uriString)')

# Let's also do it for the other one just in case
text = text.replace('val context = LocalContext.current', 'val ctx = LocalContext.current')
text = text.replace('MusicPlayerManager.resume(context)', 'MusicPlayerManager.resume(ctx)')
text = text.replace('MusicPlayerManager.playTrack(track, processedTracks, context)', 'MusicPlayerManager.playTrack(track, processedTracks, ctx)')

with open('app/src/main/java/com/example/ui/screens/RingtonesScreen.kt', 'w') as f:
    f.write(text)
