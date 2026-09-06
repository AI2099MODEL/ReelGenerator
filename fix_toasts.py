import re
import sys

def process_file(filepath):
    with open(filepath, 'r') as f:
        content = f.read()

    # Find context = LocalContext.current and add notificationService if not present
    if "LocalNotificationService.current" not in content and "LocalContext.current" in content:
        content = content.replace("val context = LocalContext.current", "val context = LocalContext.current\n    val notificationService = LocalNotificationService.current")

    # Add imports
    imports = "import com.example.ui.components.LocalNotificationService\nimport com.example.ui.components.NotificationType\n"
    if "import com.example.ui.components.LocalNotificationService" not in content:
        content = content.replace("import androidx.compose.runtime.*", imports + "import androidx.compose.runtime.*")
    
    # regex to find Toast.makeText(context, "Message", ...).show() or Toast.makeText(activity, ...) or Toast.makeText(app, ...)
    # Group 1: string content
    pattern = r'Toast\.makeText\([^,]+,\s*([^,]+?),\s*Toast\.LENGTH_(?:SHORT|LONG)\)\.show\(\)'
    
    def replacer(match):
        msg = match.group(1).strip()
        
        # Decide title and type based on message
        type_str = "NotificationType.INFO"
        title = "Notification"
        if "fail" in msg.lower() or "error" in msg.lower() or "cannot" in msg.lower() or "unable" in msg.lower() or "needed" in msg.lower() or "not found" in msg.lower():
            type_str = "NotificationType.ERROR"
            title = "Action Failed"
        elif "success" in msg.lower() or "saved" in msg.lower() or "copied" in msg.lower() or "added" in msg.lower() or "uploaded" in msg.lower() or "captured" in msg.lower() or "enriched" in msg.lower() or "generated" in msg.lower() or "unlocked" in msg.lower():
            type_str = "NotificationType.SUCCESS"
            title = "Success"
        elif "deleted" in msg.lower() or "removed" in msg.lower():
            type_str = "NotificationType.ALERT"
            title = "Item Removed"
        elif "please" in msg.lower():
            type_str = "NotificationType.ALERT"
            title = "Attention"
        elif "switched" in msg.lower() or "syncing" in msg.lower():
            type_str = "NotificationType.INFO"
            title = "Update"
            
        return f'notificationService.show("{title}", {msg}, {type_str})'
        
    new_content = re.sub(pattern, replacer, content)
    
    with open(filepath, 'w') as f:
        f.write(new_content)

for arg in sys.argv[1:]:
    process_file(arg)
