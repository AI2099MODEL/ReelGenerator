with open("app/src/main/java/com/example/ui/screens/RingtonesScreen.kt", "r") as f:
    content = f.read()

# Fix RingtoneEmptyState
content = content.replace(
    "private fun RingtoneEmptyState(\n    hasSearch: Boolean",
    "private fun RingtoneEmptyState(\n    modifier: Modifier = Modifier,\n    hasSearch: Boolean"
)

# Fix FloatingMiniPlayerBar
content = content.replace(
    "private fun FloatingMiniPlayerBar(\n    track: MusicTrackEntity",
    "private fun FloatingMiniPlayerBar(\n    modifier: Modifier = Modifier,\n    track: MusicTrackEntity"
)

with open("app/src/main/java/com/example/ui/screens/RingtonesScreen.kt", "w") as f:
    f.write(content)
