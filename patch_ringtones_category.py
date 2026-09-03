with open('app/src/main/java/com/example/ui/screens/RingtonesScreen.kt', 'r') as f:
    text = f.read()

# When we add a ringtone, it asks for category, let's hardcode it to "Ringtone" instead of what the user inputs.
text = text.replace(
'''                                        onAddTrack(
                                            songName,
                                            albumName,
                                            category,
                                            artist,
                                            uriString,
                                            fileName,
                                            fileSizeBytes,
                                            durationMs,
                                            sourceType,
                                            notes
                                        )''',
'''                                        onAddTrack(
                                            songName,
                                            albumName,
                                            "Ringtone", // Force Ringtone category
                                            artist,
                                            uriString,
                                            fileName,
                                            fileSizeBytes,
                                            durationMs,
                                            sourceType,
                                            notes
                                        )''')

with open('app/src/main/java/com/example/ui/screens/RingtonesScreen.kt', 'w') as f:
    f.write(text)
