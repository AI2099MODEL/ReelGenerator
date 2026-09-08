import re

with open("/tmp/fix_vault.py_out", "r") as f:
    content = f.read()

# Make background visible (just transparent/no fill or solid transparent color) in VaultScreen Box modifier.
bg_target = """    Box(modifier = modifier.fillMaxSize().background(
        Brush.verticalGradient(
            colors = listOf(Color(0xFF0F0B18), Color(0xFF18122B)) // Rich premium gradient
        )
    )) {"""
bg_replace = """    Box(modifier = modifier.fillMaxSize()) {"""
content = content.replace(bg_target, bg_replace)

with open("/tmp/fix_vault3.py_out", "w") as f:
    f.write(content)
