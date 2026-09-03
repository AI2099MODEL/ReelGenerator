const fs = require('fs');
let content = fs.readFileSync('app/src/main/java/com/example/ui/screens/SocialScreen.kt', 'utf8');

// Replace hardcoded Slate colors with MaterialTheme equivalents
content = content.replace(/Color\(0xFFF8FAFB\)/g, 'MaterialTheme.colorScheme.background');
content = content.replace(/Color\(0xFF191C1D\)/g, 'MaterialTheme.colorScheme.onBackground');
content = content.replace(/Color\(0xFF42474A\)/g, 'MaterialTheme.colorScheme.onSurfaceVariant');
content = content.replace(/Color\(0xFFECEEEF\)/g, 'MaterialTheme.colorScheme.surfaceVariant');
content = content.replace(/Color\(0xFFC2C7CA\)/g, 'MaterialTheme.colorScheme.outlineVariant');
content = content.replace(/Color\(0xFF4A626D\)/g, 'MaterialTheme.colorScheme.secondary');
content = content.replace(/Color\(0xFF2E434C\)/g, 'MaterialTheme.colorScheme.primary');
content = content.replace(/Color\(0xFFFF5722\)/g, 'com.example.ui.theme.NavSocialOrange');
content = content.replace(/Color\(0xFF00C853\)/g, 'com.example.ui.theme.NavEventsEmerald');

fs.writeFileSync('app/src/main/java/com/example/ui/screens/SocialScreen.kt', content);
