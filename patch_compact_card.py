import re

with open("app/src/main/java/com/example/ui/screens/RingtonesScreen.kt", "r") as f:
    content = f.read()

# Replace padding
content = content.replace(
    "                .padding(10.dp)",
    "                .padding(8.dp)"
)

# Play Button size
content = content.replace(
    ".size(36.dp)",
    ".size(32.dp)"
)
content = content.replace(
    "modifier = Modifier.size(20.dp)",
    "modifier = Modifier.size(16.dp)"
)

# Title/Artist text size (slightly smaller if needed, but 13/10 is already quite small)
content = content.replace("fontSize = 13.sp,", "fontSize = 12.sp,")
content = content.replace("fontSize = 10.sp,", "fontSize = 9.sp,")
content = content.replace("fontSize = 8.sp,", "fontSize = 8.sp,")

# Favorite icon button
content = content.replace(
    "IconButton(onClick = onToggleFavorite) {",
    "IconButton(onClick = onToggleFavorite, modifier = Modifier.size(24.dp)) {"
)
content = content.replace(
    "tint = if (track.isFavorite) RoseQuartzPrimary else RoseQuartzTextMuted\n                    )",
    "tint = if (track.isFavorite) RoseQuartzPrimary else RoseQuartzTextMuted,\n                        modifier = Modifier.size(16.dp)\n                    )"
)

# Spacers
content = content.replace(
    "Spacer(modifier = Modifier.height(8.dp))",
    "Spacer(modifier = Modifier.height(6.dp))"
)

# "Set" Button -> Surface
old_set_btn = """                Button(
                    onClick = onSetAsRingtone,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = RoseQuartzPrimary),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Icon(Icons.Filled.RingVolume, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Set", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }"""

new_set_btn = """                Surface(
                    onClick = onSetAsRingtone,
                    shape = RoundedCornerShape(6.dp),
                    color = RoseQuartzPrimary,
                    modifier = Modifier.height(24.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    ) {
                        Icon(Icons.Filled.RingVolume, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Set", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }"""
content = content.replace(old_set_btn, new_set_btn)

# Edit/Delete icon buttons size
content = content.replace(
    "modifier = Modifier.size(28.dp)",
    "modifier = Modifier.size(24.dp)"
)
content = content.replace(
    "modifier = Modifier.size(16.dp)",
    "modifier = Modifier.size(14.dp)"
)


with open("app/src/main/java/com/example/ui/screens/RingtonesScreen.kt", "w") as f:
    f.write(content)
