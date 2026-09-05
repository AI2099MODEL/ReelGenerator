with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    content = f.read()

if "OrganiserStorageManager.initOrganiserStorage" not in content:
    content = content.replace("import com.example.util.NotificationHelper", "import com.example.util.NotificationHelper\nimport com.example.util.OrganiserStorageManager")
    content = content.replace(
        "NotificationHelper.createNotificationChannel(this)",
        "NotificationHelper.createNotificationChannel(this)\n        // Automatically create 'My Organiser' directory structure in device local storage\n        OrganiserStorageManager.initOrganiserStorage(this)"
    )

with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
    f.write(content)

print("MainActivity updated")
