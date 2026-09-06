import re

with open('app/src/main/java/com/example/ui/screens/VaultScreen.kt', 'r') as f:
    content = f.read()

content = content.replace('    val notificationService = LocalNotificationService.current\n    \n    val photoPickerLauncher', '    val notificationService = LocalNotificationService.current\n    val coroutineScope = rememberCoroutineScope()\n    val photoPickerLauncher')

with open('app/src/main/java/com/example/ui/screens/VaultScreen.kt', 'w') as f:
    f.write(content)
