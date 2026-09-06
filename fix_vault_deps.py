with open('app/src/main/java/com/example/ui/screens/VaultScreen.kt', 'r') as f:
    content = f.read()

imports_to_add = """
import com.example.ui.components.LocalNotificationService
import com.example.ui.components.NotificationType
import androidx.compose.ui.geometry.Offset
"""

colors_to_add = """
// Vault Colors
val GoldPrimary = Color(0xFFFFD700)
val GoldHighlight = Color(0xFFFFF2B2)
val GoldAccent = Color(0xFFB8860B)
val GoldLight = Color(0xFFF0E68C)
val MetallicGoldBrush = Brush.linearGradient(
    colors = listOf(Color(0xFFFFDF00), Color(0xFFD4AF37), Color(0xFF996515), Color(0xFFD4AF37), Color(0xFFFFDF00)),
    start = Offset(0f, 0f),
    end = Offset(1000f, 1000f)
)
val SunsetPhoneWallpaperGradient = Brush.verticalGradient(
    colors = listOf(Color(0xFF2C1B4D), Color(0xFF702D6C), Color(0xFFD36B5F), Color(0xFFF9C87B))
)
"""

if 'import com.example.ui.components.LocalNotificationService' not in content:
    content = content.replace('import com.example.ui.theme.*', 'import com.example.ui.theme.*\n' + imports_to_add)
    
if 'val GoldPrimary' not in content:
    content = content.replace('@Composable\nfun VaultScreen(', colors_to_add + '\n@Composable\nfun VaultScreen(')

with open('app/src/main/java/com/example/ui/screens/VaultScreen.kt', 'w') as f:
    f.write(content)
