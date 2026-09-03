import re

def patch_file(filepath, header_pattern):
    with open(filepath, "r") as f:
        content = f.read()

    # 1. Remove the "All" string from defaultList
    content = content.replace('val defaultList = listOf("All", "Favorites ❤️")', 'val defaultList = listOf("Favorites ❤️")')
    content = content.replace('val defaultList = listOf("All", "Favorites ❤️",', 'val defaultList = listOf("Favorites ❤️",')

    # 2. Update the onClick for category pills to toggle off
    content = content.replace('onClick = { selectedCategoryFilter = category }', 'onClick = { selectedCategoryFilter = if (selectedCategoryFilter == category) "All" else category }')

    # 3. Remove the header item block entirely
    content = re.sub(header_pattern, "", content, flags=re.DOTALL)

    with open(filepath, "w") as f:
        f.write(content)

music_header = r"""                // Header with Count
                item \{
                    Row\(
                        modifier = Modifier
                            \.fillMaxWidth\(\)
                            \.padding\(top = 4\.dp\),
                        horizontalArrangement = Arrangement\.SpaceBetween,
                        verticalAlignment = Alignment\.CenterVertically
                    \) \{
                        Text\(
                            text = "Music Tracks \(\$\{processedTracks\.size\}\)",
                            fontFamily = FontFamily\.Serif,
                            fontWeight = FontWeight\.Bold,
                            fontSize = 17\.sp,
                            color = RoseQuartzTextPrimary
                        \)
                        Text\(
                            text = "Sorted by \$\{currentSortOption\.label\}",
                            fontSize = 11\.sp,
                            color = RoseQuartzTextSecondary
                        \)
                    \}
                \}
"""

ringtone_header = r"""                // Header with Count
                item \{
                    Row\(
                        modifier = Modifier
                            \.fillMaxWidth\(\)
                            \.padding\(top = 4\.dp\),
                        horizontalArrangement = Arrangement\.SpaceBetween,
                        verticalAlignment = Alignment\.CenterVertically
                    \) \{
                        Text\(
                            text = "Ringtone Collection \(\$\{processedTracks\.size\}\)",
                            fontFamily = FontFamily\.Serif,
                            fontWeight = FontWeight\.Bold,
                            fontSize = 17\.sp,
                            color = RoseQuartzTextPrimary
                        \)
                    \}
                \}
"""

patch_file("app/src/main/java/com/example/ui/screens/MusicScreen.kt", music_header)
patch_file("app/src/main/java/com/example/ui/screens/RingtonesScreen.kt", ringtone_header)

