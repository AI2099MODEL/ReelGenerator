import re

with open("app/src/main/java/com/example/ui/screens/VaultScreen.kt", "r") as f:
    content = f.read()

content = content.replace(
"""if (filteredDocs.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize().padding(bottom = 80.dp), contentAlignment = Alignment.Center) {""", 
"""if (filteredDocs.isEmpty()) {
                    Box(modifier = Modifier.weight(1f).fillMaxWidth().padding(bottom = 80.dp), contentAlignment = Alignment.Center) {""")

with open("app/src/main/java/com/example/ui/screens/VaultScreen.kt", "w") as f:
    f.write(content)
