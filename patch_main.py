import re

with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    content = f.read()

content = content.replace("OrganiserStorageManager.initOrganiserStorage(this)", 
"""try {
            OrganiserStorageManager.initOrganiserStorage(this)
        } catch (e: Exception) {
            e.printStackTrace()
        }""")

with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
    f.write(content)
