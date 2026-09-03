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

# We need to add 'Set as Ringtone' functionality.
# First, add the import
if 'import com.example.util.RingtoneUtil' not in text:
    text = text.replace('import com.example.data.model.MusicTrackEntity', 'import com.example.data.model.MusicTrackEntity\nimport com.example.util.RingtoneUtil')

# Locate CompactTrackItem and TrackCard to add the button
text = text.replace(
'''                                IconButton(onClick = onToggleFavorite) {
                                    Icon(
                                        imageVector = if (track.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                        contentDescription = "Favorite",
                                        tint = RoseQuartzPrimary
                                    )
                                }''', 
'''                                IconButton(onClick = { RingtoneUtil.setRingtone(context, track.uriString) }) {
                                    Icon(
                                        imageVector = Icons.Filled.NotificationsActive,
                                        contentDescription = "Set as Ringtone",
                                        tint = RoseQuartzPrimary
                                    )
                                }
                                IconButton(onClick = onToggleFavorite) {
                                    Icon(
                                        imageVector = if (track.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                        contentDescription = "Favorite",
                                        tint = RoseQuartzPrimary
                                    )
                                }''')

with open('app/src/main/java/com/example/ui/screens/RingtonesScreen.kt', 'w') as f:
    f.write(text)
