with open('app/src/main/java/com/example/ui/screens/RingtonesScreen.kt', 'r') as f:
    text = f.read()

# Replace My Music with My Ringtones
text = text.replace('title = "My Music"', 'title = "My Ringtones"')
text = text.replace('text = "My Music"', 'text = "My Ringtones"')
text = text.replace('No Music Tracks', 'No Ringtones')
text = text.replace('Add Music Track', 'Add Ringtone')
text = text.replace('Add New Track', 'Add New Ringtone')
text = text.replace('Upload Music', 'Upload Ringtone')
text = text.replace('Favorite Tracks', 'Favorite Ringtones')
text = text.replace('All Tracks', 'All Ringtones')
text = text.replace('fun MusicScreen', 'fun RingtonesScreen')

import re
# Remove the enum to prevent duplicates
text = re.sub(r'enum class MusicSortOption\(.*?\}\n', '', text, flags=re.DOTALL)
# Remove countWords to prevent duplicates
text = re.sub(r'/\*\*.*?countWords.*?fun countWords.*?\}\n', '', text, flags=re.DOTALL)

# Add setRingtone button in MusicScoreCardRow (which has a context passed to it? No, wait)
# Wait, MusicScoreCardRow does NOT have a `context` variable. Let's add it.
text = text.replace('private fun MusicScoreCardRow(', 'private fun MusicScoreCardRow(context: android.content.Context, ')
text = text.replace('MusicScoreCardRow(\n                        track = track,', 'MusicScoreCardRow(\n                        context = context,\n                        track = track,')

# Now add the Set as Ringtone button in MusicScoreCardRow
replacement_card = """                    // Set as ringtone button
                    IconButton(
                        onClick = { com.example.util.RingtoneUtil.setRingtone(context, track.uriString) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.NotificationsActive,
                            contentDescription = "Set Ringtone",
                            tint = RoseQuartzPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    
                    // Favorite Heart Toggle"""
text = text.replace('                    // Favorite Heart Toggle', replacement_card)

# Now add the Set as Ringtone button in FullScorePlayerBottomSheet
text = text.replace('private fun FullScorePlayerBottomSheet(', 'private fun FullScorePlayerBottomSheet(context: android.content.Context, ')
text = text.replace('FullScorePlayerBottomSheet(\n                track = trackToPlay,', 'FullScorePlayerBottomSheet(\n                context = context,\n                track = trackToPlay,')

replacement_sheet = """                IconButton(onClick = { com.example.util.RingtoneUtil.setRingtone(context, track.uriString) }) {
                    Icon(
                        imageVector = Icons.Filled.NotificationsActive,
                        contentDescription = "Set as Ringtone",
                        tint = RoseQuartzPrimary
                    )
                }
                IconButton(onClick = onToggleFavorite) {"""
text = text.replace('                IconButton(onClick = onToggleFavorite) {', replacement_sheet)

with open('app/src/main/java/com/example/ui/screens/RingtonesScreen.kt', 'w') as f:
    f.write(text)
