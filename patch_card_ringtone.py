with open('app/src/main/java/com/example/ui/screens/RingtonesScreen.kt', 'r') as f:
    text = f.read()

replacement = """                    // Set as ringtone button
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

text = text.replace('                    // Favorite Heart Toggle', replacement)

with open('app/src/main/java/com/example/ui/screens/RingtonesScreen.kt', 'w') as f:
    f.write(text)
