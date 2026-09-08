import re

with open("app/src/main/java/com/example/ui/screens/VaultScreen.kt", "r") as f:
    content = f.read()

old_col = 'text = {\n                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {'
new_col = 'text = {\n                    Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.verticalScroll(rememberScrollState())) {'

content = content.replace(old_col, new_col)

with open("app/src/main/java/com/example/ui/screens/VaultScreen.kt", "w") as f:
    f.write(content)
