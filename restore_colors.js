const fs = require('fs');
let content = fs.readFileSync('app/src/main/java/com/example/ui/theme/Color.kt', 'utf8');

content = content.replace(/val RoseCustom = Color\(0xFF2E434C\)/, 'val RoseCustom = Color(0xFFD81B60)');
content = content.replace(/val RoseCustomDark = Color\(0xFF191C1D\)/, 'val RoseCustomDark = Color(0xFFAD144A)');
content = content.replace(/val RoseCustomLight = Color\(0xFFCDE6F4\)/, 'val RoseCustomLight = Color(0xFFFCE4EC)');
content = content.replace(/val GlassBg = Color\(0xFFF8FAFB\)\.copy\(alpha = 0\.8f\)/, 'val GlassBg = Color.White.copy(alpha = 0.4f)');
content = content.replace(/val GlassBorder = Color\(0xFFC2C7CA\)\.copy\(alpha = 0\.3f\)/, 'val GlassBorder = Color.White.copy(alpha = 0.6f)');
content = content.replace(/val GlassBubbleReceived = Color\(0xFFE1E3E4\)\.copy\(alpha = 0\.6f\)/, 'val GlassBubbleReceived = Color(0xFFFFE4E6).copy(alpha = 0.6f)');
content = content.replace(/val GlassBubbleSent = Color\(0xFF2E434C\)\.copy\(alpha = 0\.85f\)/, 'val GlassBubbleSent = Color(0xFFD81B60).copy(alpha = 0.85f)');

content = content.replace(/val LedgerInkNavy = Color\(0xFF191C1D\)/, 'val LedgerInkNavy = Color(0xFF1B2430)');
content = content.replace(/val LedgerParchment = Color\(0xFFF8FAFB\)/, 'val LedgerParchment = Color(0xFFFFF1F2)');
content = content.replace(/val LedgerPaperLight = Color\(0xFFECEEEF\)/, 'val LedgerPaperLight = Color.White.copy(alpha = 0.6f)');
content = content.replace(/val LedgerBrass = Color\(0xFF4A626D\) \/\/ slate secondary/, 'val LedgerBrass = RoseCustom');

content = content.replace(/val NavChatBlue = Color\(0xFF0055FF\)/, 'val NavChatBlue = Color(0xFF2E5BFF)');
content = content.replace(/val NavDiaryGold = Color\(0xFFFFC107\)/, 'val NavDiaryGold = Color(0xFFD4AF37)');
content = content.replace(/val NavEventsEmerald = Color\(0xFF00C853\)/, 'val NavEventsEmerald = Color(0xFF10B981)');
content = content.replace(/val NavVaultCrimson = Color\(0xFFD81B60\)/, 'val NavVaultCrimson = Color(0xFFDC2626)');
content = content.replace(/val NavTasksViolet = Color\(0xFF7C4DFF\)/, 'val NavTasksViolet = Color(0xFF8B5CF6)');

fs.writeFileSync('app/src/main/java/com/example/ui/theme/Color.kt', content);
