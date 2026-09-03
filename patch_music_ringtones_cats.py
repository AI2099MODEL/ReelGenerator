import re

# MUSIC SCREEN
with open("app/src/main/java/com/example/ui/screens/MusicScreen.kt", "r") as f:
    music_content = f.read()

music_old = """    val allCategories = remember(tracks) {
        val dynamicCats = tracks.map { it.category.trim() }.filter { it.isNotBlank() }.distinct()
        val defaultList = listOf("All", "Favorites ❤️", "🎵 Pop", "🎶 Song", "🎼 Classical", "🎧 Lofi", "🎸 Rock", "🎹 Piano", "🥁 Beats", "🎷 Jazz", "🎻 Strings", "❤️ Romantic", "🔥 Trending", "✨ Chill")
        (defaultList + dynamicCats).distinct()
    }"""
music_new = """    val allCategories = remember(tracks) {
        val dynamicCats = tracks.map { it.category.trim() }.filter { it.isNotBlank() }.distinct()
        val defaultList = listOf("All", "Favorites ❤️")
        (defaultList + dynamicCats).distinct()
    }"""
music_content = music_content.replace(music_old, music_new)

with open("app/src/main/java/com/example/ui/screens/MusicScreen.kt", "w") as f:
    f.write(music_content)


# RINGTONES SCREEN
with open("app/src/main/java/com/example/ui/screens/RingtonesScreen.kt", "r") as f:
    ring_content = f.read()

ring_old = """    val allCategories = remember(tracks) {
        val dynamicCats = tracks.map { it.category.trim() }.filter { it.isNotBlank() }.distinct()
        val defaultList = listOf("All", "Favorites ❤️", "🔔 Ringtone", "🔥 Loud", "🎶 Melody", "🎼 Classical", "🎸 Acoustic", "📻 Retro", "❤️ Romantic", "✨ Soft")
        (defaultList + dynamicCats).distinct()
    }"""
ring_new = """    val allCategories = remember(tracks) {
        val dynamicCats = tracks.map { it.category.trim() }.filter { it.isNotBlank() }.distinct()
        val defaultList = listOf("All", "Favorites ❤️")
        (defaultList + dynamicCats).distinct()
    }"""
ring_content = ring_content.replace(ring_old, ring_new)

with open("app/src/main/java/com/example/ui/screens/RingtonesScreen.kt", "w") as f:
    f.write(ring_content)

