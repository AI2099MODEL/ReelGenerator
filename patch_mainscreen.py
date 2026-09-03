with open('app/src/main/java/com/example/ui/screens/MainLedgerScreen.kt', 'r') as f:
    text = f.read()

# Make sure RingtonesScreen is imported if needed, but it's in the same package.

ringtone_case = """            LedgerSection.RINGTONES -> {
                RingtonesScreen(
                    tracks = musicTracks.filter { it.category == "Ringtone" },
                    onAddTrack = { songName, albumName, category, artist, uriStr, fileName, fileSize, duration, sourceType, notes ->
                        viewModel.addMusicTrack(songName, albumName, category, artist, uriStr, fileName, fileSize, duration, sourceType, notes)
                    },
                    onUpdateTrack = { viewModel.updateMusicTrack(it) },
                    onDeleteTrack = { viewModel.deleteMusicTrack(it) },
                    onToggleFavorite = { id, fav -> viewModel.toggleMusicFavorite(id, fav) },
                    onMenuClick = onOpenDrawer
                )
            }"""

if "LedgerSection.RINGTONES" not in text:
    text = text.replace('            LedgerSection.MUSIC -> {', ringtone_case + '\n            LedgerSection.MUSIC -> {')

# Wait, we need to filter MusicScreen to NOT show ringtones.
text = text.replace(
'''            LedgerSection.MUSIC -> {
                MusicScreen(
                    tracks = musicTracks,''',
'''            LedgerSection.MUSIC -> {
                MusicScreen(
                    tracks = musicTracks.filter { it.category != "Ringtone" },''')

with open('app/src/main/java/com/example/ui/screens/MainLedgerScreen.kt', 'w') as f:
    f.write(text)
