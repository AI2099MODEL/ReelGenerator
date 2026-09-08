import re

with open("app/src/main/java/com/example/ui/screens/VaultScreen.kt", "r") as f:
    content = f.read()

# I noticed the background Box is around the Scaffold
bg_target = """    Box(modifier = modifier.fillMaxSize().background(
        Brush.verticalGradient(
            colors = listOf(Color(0xFF0F0B18), Color(0xFF18122B)) // Rich premium gradient
        )
    )) {"""

# Replace it with simple Box(modifier = modifier.fillMaxSize()) {
# I will use a simple find/replace on just the background text if it's there
content = content.replace(
    "Box(modifier = modifier.fillMaxSize().background(\n        Brush.verticalGradient(\n            colors = listOf(Color(0xFF0F0B18), Color(0xFF18122B)) // Rich premium gradient\n        )\n    ))",
    "Box(modifier = modifier.fillMaxSize())"
)

with open("app/src/main/java/com/example/ui/screens/VaultScreen.kt", "w") as f:
    f.write(content)
