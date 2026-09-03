const fs = require('fs');
let content = fs.readFileSync('app/src/main/java/com/example/ui/components/BinderNavigation.kt', 'utf8');

// Remove the local enum declaration
content = content.replace(/enum class LedgerSection[\s\S]*?SOCIAL\("Social"\)\n\}/, 'import com.example.ui.LedgerSection');

// Revert Icons.AutoMirrored.Filled.MenuBook to Icons.Filled.MenuBook
content = content.replace(/Icons\.AutoMirrored\.Filled\.MenuBook/g, 'Icons.Filled.MenuBook');
content = content.replace(/Icons\.AutoMirrored\.Outlined\.MenuBook/g, 'Icons.Outlined.MenuBook');

fs.writeFileSync('app/src/main/java/com/example/ui/components/BinderNavigation.kt', content);
