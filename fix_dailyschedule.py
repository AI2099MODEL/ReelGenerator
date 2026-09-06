import sys

with open('app/src/main/java/com/example/ui/screens/DailyScheduleScreen.kt', 'r') as f:
    content = f.read()

# Replace local activeNotification state with LocalNotificationService
# Find:
#    var activeNotification by remember { mutableStateOf<AppNotification?>(null) }
#    fun showNotification(title: String, message: String, type: NotificationType) {
#        activeNotification = AppNotification(title = title, message = message, type = type)
#    }
#
# And replace usages of showNotification with notificationService.show

if 'val notificationService = LocalNotificationService.current' not in content:
    content = content.replace('fun DailyScheduleScreen(', 'fun DailyScheduleScreen(\n    notificationService: com.example.ui.components.NotificationService = com.example.ui.components.LocalNotificationService.current,')

content = content.replace('var activeNotification by remember { mutableStateOf<AppNotification?>(null) }', '')

# Remove showNotification function entirely
import re
content = re.sub(r'fun showNotification\([^)]+\)\s*\{[^}]+\}', '', content)

# Replace showNotification calls with notificationService.show
content = content.replace('showNotification(', 'notificationService.show(')

# Remove local ColoredNotificationBannerHost
content = re.sub(r'ColoredNotificationBannerHost\(\s*notification = activeNotification,\s*onDismiss = \{ activeNotification = null \},\s*modifier = Modifier\s*\.align\(Alignment\.TopCenter\)\s*\.padding\(top = 16\.dp,\s*start = 16\.dp,\s*end = 16\.dp\)\s*\.zIndex\(100f\)\s*\)', '', content)

with open('app/src/main/java/com/example/ui/screens/DailyScheduleScreen.kt', 'w') as f:
    f.write(content)
