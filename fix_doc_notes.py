import re

with open('app/src/main/java/com/example/ui/screens/VaultScreen.kt', 'r') as f:
    content = f.read()

old_column = """            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = doc.title,
                    color = GoldHighlight,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = doc.fileType,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = GoldLight
                    )
                    Text(
                        text = " • ${doc.category}",
                        fontSize = 11.sp,
                        color = GoldLight.copy(alpha = 0.7f)
                    )
                }
                Text(
                    text = "Saved in: Documents/My Organiser/Vault",
                    color = GoldLight.copy(alpha = 0.6f),
                    fontSize = 9.5.sp
                )
            }"""

new_column = """            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = doc.title,
                    color = GoldHighlight,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = doc.fileType,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = GoldLight
                    )
                    Text(
                        text = " • ${doc.category}",
                        fontSize = 11.sp,
                        color = GoldLight.copy(alpha = 0.7f)
                    )
                }
                
                if (doc.notes.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = doc.notes,
                        color = GoldLight.copy(alpha = 0.85f),
                        fontSize = 10.5.sp,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                Text(
                    text = "Saved in: Documents/My Organiser/Vault",
                    color = GoldLight.copy(alpha = 0.6f),
                    fontSize = 9.5.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }"""

content = content.replace(old_column, new_column)

with open('app/src/main/java/com/example/ui/screens/VaultScreen.kt', 'w') as f:
    f.write(content)
