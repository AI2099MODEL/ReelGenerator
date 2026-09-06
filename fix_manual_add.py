import re

with open('app/src/main/java/com/example/ui/screens/VaultScreen.kt', 'r') as f:
    content = f.read()

old_manual = """                                onAddDocument(
                                    docTitle.trim(),
                                    filename,
                                    "file://vault/$filename",
                                    docType,
                                    docCategory,
                                    1024L
                                )"""

new_manual = """                                onAddDocument(
                                    docTitle.trim(),
                                    filename,
                                    "file://vault/$filename",
                                    docType,
                                    docCategory,
                                    1024L,
                                    docNotes.trim()
                                )"""

content = content.replace(old_manual, new_manual)

with open('app/src/main/java/com/example/ui/screens/VaultScreen.kt', 'w') as f:
    f.write(content)
