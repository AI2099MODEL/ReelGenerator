with open('app/src/main/java/com/example/data/Repository.kt', 'r') as f:
    text = f.read()

new_ringtones = """                MusicTrackEntity(
                    songName = "Classic Marimba (English)",
                    albumName = "Standard Ringtones",
                    category = "Ringtone",
                    artist = "System",
                    uriString = "",
                    fileName = "marimba_classic.mp3",
                    fileSizeBytes = 300000L,
                    durationMs = 15000L,
                    isFavorite = true,
                    sourceType = "LOCAL_STORAGE",
                    notes = "Classic phone marimba."
                ),
                MusicTrackEntity(
                    songName = "Bollywood Romance Flute (Hindi)",
                    albumName = "Desi Melodies",
                    category = "Ringtone",
                    artist = "Rhythm & Flute",
                    uriString = "",
                    fileName = "bollywood_flute.mp3",
                    fileSizeBytes = 450000L,
                    durationMs = 25000L,
                    isFavorite = false,
                    sourceType = "LOCAL_STORAGE",
                    notes = "Soothing Hindi flute melody."
                ),
                MusicTrackEntity(
                    songName = "Digital Bell (English)",
                    albumName = "Standard Ringtones",
                    category = "Ringtone",
                    artist = "System",
                    uriString = "",
                    fileName = "digital_bell.mp3",
                    fileSizeBytes = 250000L,
                    durationMs = 12000L,
                    isFavorite = false,
                    sourceType = "LOCAL_STORAGE",
                    notes = "Modern digital ringtone."
                ),
                MusicTrackEntity(
                    songName = "Desi Beats (Hindi)",
                    albumName = "Desi Melodies",
                    category = "Ringtone",
                    artist = "Dholak Masters",
                    uriString = "",
                    fileName = "desi_beats.mp3",
                    fileSizeBytes = 500000L,
                    durationMs = 20000L,
                    isFavorite = true,
                    sourceType = "LOCAL_STORAGE",
                    notes = "Upbeat Hindi dholak beats."
                )"""

if "Classic Marimba" not in text:
    text = text.replace('            val sampleTracks = listOf(', '            val sampleTracks = listOf(\n' + new_ringtones + ',')

with open('app/src/main/java/com/example/data/Repository.kt', 'w') as f:
    f.write(text)
