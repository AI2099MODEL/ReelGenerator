with open('app/src/main/java/com/example/ui/components/LedgerSideMenuDrawer.kt', 'r') as f:
    lines = f.readlines()

out = []
for i, line in enumerate(lines):
    if i == 213: # This is the `            ) {` line
        out.append(line)
        out.append("""                if (currentSection == LedgerSection.MUSIC) {
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
    elif i == 799: # Line 800 which is `            }` before DRAWER FOOTER. Actually index 799 is line 800
        out.append("                }\n")
        out.append(line)
    else:
        out.append(line)

with open('app/src/main/java/com/example/ui/components/LedgerSideMenuDrawer.kt', 'w') as f:
    f.writelines(out)
