import re

with open("app/src/main/java/com/example/ui/screens/VaultScreen.kt", "r") as f:
    content = f.read()

target = """    Box(modifier = modifier.fillMaxSize().background(
        Brush.verticalGradient(
            colors = listOf(Color(0xFF0F0B18), Color(0xFF18122B)) // Rich premium gradient
        )
    )) {
        Scaffold("""
replacement = """    Box(modifier = modifier.fillMaxSize()) {
        Scaffold("""
content = content.replace(target, replacement)

with open("app/src/main/java/com/example/ui/screens/VaultScreen.kt", "w") as f:
    f.write(content)
