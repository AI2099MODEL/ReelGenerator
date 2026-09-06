with open('app/src/main/java/com/example/ui/screens/VaultScreen.kt', 'r') as f:
    content = f.read()

imports = """import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.PickVisualMediaRequest
import com.example.BuildConfig
"""

content = content.replace('import androidx.compose.animation.*', imports + '\nimport androidx.compose.animation.*')

with open('app/src/main/java/com/example/ui/screens/VaultScreen.kt', 'w') as f:
    f.write(content)
