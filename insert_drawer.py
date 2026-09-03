import sys

with open('app/src/main/java/com/example/ui/components/LedgerSideMenuDrawer.kt', 'r') as f:
    lines = f.readlines()

out = []
in_lazy_column = False
braces_count = 0
found_lazy_column = False

for i, line in enumerate(lines):
    if not found_lazy_column and "LazyColumn(" in line:
        found_lazy_column = True
    
    if found_lazy_column and "verticalArrangement = Arrangement.spacedBy(16.dp)" in line:
        out.append(line)
        # Next line is `) {`
        out.append(lines[i+1])
        # Insert our block
        out.append("""
                if (currentSection == LedgerSection.MUSIC) {
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "FAVORITE SONGS",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = RoseQuartzTextMuted,
                                letterSpacing = 1.sp,
                                modifier = Modifier.padding(start = 4.dp)
                            )
                            val favoriteSongs = musicTracks.filter { it.isFavorite }
                            if (favoriteSongs.isEmpty()) {
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = RoseQuartzContainerLowest,
                                    border = BorderStroke(1.dp, RoseQuartzContainerHighest),
                                    shadowElevation = 1.dp,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("No favorite songs yet.", fontSize = 12.sp, color = RoseQuartzTextSecondary, modifier = Modifier.padding(14.dp))
                                }
                            } else {
                                favoriteSongs.forEach { track ->
                                    Surface(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable {
                                                onPlayTrack?.invoke(track)
                                                onCloseDrawer()
                                            }
                                            .padding(bottom = 6.dp),
                                        shape = RoundedCornerShape(8.dp),
                                        color = RoseQuartzBg,
                                        border = BorderStroke(1.dp, RoseQuartzContainerHighest)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(10.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            Icon(Icons.Filled.Favorite, contentDescription = null, tint = RoseQuartzPrimary, modifier = Modifier.size(16.dp))
                                            Column {
                                                Text(track.songName, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = RoseQuartzTextPrimary)
                                                if (track.artist.isNotBlank()) {
                                                    Text(track.artist, fontSize = 11.sp, color = RoseQuartzTextSecondary)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = { onSectionSelected(LedgerSection.DIARY); onCloseDrawer() },
                                colors = ButtonDefaults.buttonColors(containerColor = RoseQuartzPrimary),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Exit My Music", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                } else {
""")
        # Skip the original `) {` line since we added it
        continue
    
    if found_lazy_column and i > 215:
        # Find the end of LazyColumn (line 800)
        # Line 800 is `            }` before `            // DRAWER FOOTER`
        if line.strip() == "}" and "DRAWER FOOTER" in lines[i+3]:
            out.append("                }\n") # close else
            out.append(line)
            continue
            
    if i != 211: # just to skip the `) {` check for the manual append
        out.append(line)

with open('app/src/main/java/com/example/ui/components/LedgerSideMenuDrawer.kt', 'w') as f:
    f.writelines(out)

