import re

with open('app/src/main/java/com/example/ui/screens/VaultScreen.kt', 'r') as f:
    content = f.read()

# 1. Remove the accidental launcher injection from VaultLockScreen
lock_screen_launcher = r'    val coroutineScope = rememberCoroutineScope\(\)\n    val notificationService = LocalNotificationService\.current\s*val photoPickerLauncher = rememberLauncherForActivityResult.*?(?=\n    var authErrorMsg)'
content = re.sub(r'    val coroutineScope = rememberCoroutineScope\(\)\n    val notificationService = LocalNotificationService\.current\s*val photoPickerLauncher = rememberLauncherForActivityResult.*?        \}\n    \)\n(?=\s*var authErrorMsg)', '    val notificationService = LocalNotificationService.current\n', content, flags=re.DOTALL)

# 2. Hoist the state variables to VaultContentScreen level, before the launcher
state_vars = """
    var docTitle by remember { mutableStateOf("") }
    var docCategory by remember { mutableStateOf("Passport") }
    var docType by remember { mutableStateOf("PDF") }
    var isScanning by remember { mutableStateOf(false) }
"""

# We'll put them right under showAddDialog
content = content.replace('    var showAddDialog by remember { mutableStateOf(false) }', '    var showAddDialog by remember { mutableStateOf(false) }' + state_vars)

# 3. Remove them from inside the if (showAddDialog)
content = content.replace('            var docTitle by remember { mutableStateOf("") }\n', '')
content = content.replace('            var docCategory by remember { mutableStateOf("Passport") }\n', '')
content = content.replace('            var docType by remember { mutableStateOf("PDF") }\n', '')
content = content.replace('            var isScanning by remember { mutableStateOf(false) }\n', '')
# also remove any trailing ones in case they were grouped
content = re.sub(r'\s*val coroutineScope = rememberCoroutineScope\(\)', '', content, count=1) # The inner coroutine scope declaration

with open('app/src/main/java/com/example/ui/screens/VaultScreen.kt', 'w') as f:
    f.write(content)
