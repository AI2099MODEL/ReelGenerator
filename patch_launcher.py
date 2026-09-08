import re

with open("app/src/main/java/com/example/ui/screens/VaultScreen.kt", "r") as f:
    content = f.read()

# Insert before photoPickerLauncher
target = """    val photoPickerLauncher = rememberLauncherForActivityResult"""
replacement = """    val avatarPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if (uri != null) {
                avatarUri = uri
            }
        }
    )
    
    val photoPickerLauncher = rememberLauncherForActivityResult"""

content = content.replace(target, replacement)

with open("app/src/main/java/com/example/ui/screens/VaultScreen.kt", "w") as f:
    f.write(content)
