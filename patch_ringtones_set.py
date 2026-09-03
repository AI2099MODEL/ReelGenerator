with open('app/src/main/java/com/example/ui/screens/RingtonesScreen.kt', 'r') as f:
    text = f.read()

replacement = """                IconButton(onClick = { com.example.util.RingtoneUtil.setRingtone(context, track.uriString) }) {
                    Icon(
                        imageVector = Icons.Filled.NotificationsActive,
                        contentDescription = "Set as Ringtone",
                        tint = RoseQuartzPrimary
                    )
                }
                IconButton(onClick = onToggleFavorite) {"""

text = text.replace('                IconButton(onClick = onToggleFavorite) {', replacement)

if 'import com.example.util.RingtoneUtil' not in text:
    text = text.replace('import com.example.data.model.MusicTrackEntity', 'import com.example.data.model.MusicTrackEntity\nimport com.example.util.RingtoneUtil')

with open('app/src/main/java/com/example/ui/screens/RingtonesScreen.kt', 'w') as f:
    f.write(text)
