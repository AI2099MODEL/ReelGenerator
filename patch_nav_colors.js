const fs = require('fs');
let content = fs.readFileSync('app/src/main/java/com/example/ui/theme/Color.kt', 'utf8');

content = content.replace(/val NavChatBlue = Color\(0xFF2E5BFF\)/, 'val NavChatBlue = Color(0xFF0055FF)');
content = content.replace(/val NavDiaryGold = Color\(0xFFD4AF37\)/, 'val NavDiaryGold = Color(0xFFFFC107)');
content = content.replace(/val NavEventsEmerald = Color\(0xFF10B981\)/, 'val NavEventsEmerald = Color(0xFF00C853)');
content = content.replace(/val NavVaultCrimson = Color\(0xFFDC2626\)/, 'val NavVaultCrimson = Color(0xFFD81B60)');
content = content.replace(/val NavTasksViolet = Color\(0xFF8B5CF6\)/, 'val NavTasksViolet = Color(0xFF7C4DFF)');

// Also replace LedgerBrass alias
content = content.replace(/val LedgerBrass = RoseCustom/, 'val LedgerBrass = Color(0xFF4A626D) // slate secondary');

fs.writeFileSync('app/src/main/java/com/example/ui/theme/Color.kt', content);
