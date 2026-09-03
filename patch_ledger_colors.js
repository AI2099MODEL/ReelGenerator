const fs = require('fs');
let content = fs.readFileSync('app/src/main/java/com/example/ui/theme/Color.kt', 'utf8');

// Slate theme colors for common aliases
content = content.replace(/val LedgerInkNavy = Color\(0xFF1B2430\)/, 'val LedgerInkNavy = Color(0xFF191C1D)'); // onBackground
content = content.replace(/val LedgerParchment = Color\(0xFFFFF1F2\)/, 'val LedgerParchment = Color(0xFFF8FAFB)'); // background
content = content.replace(/val LedgerPaperLight = Color\.White\.copy\(alpha = 0\.6f\)/, 'val LedgerPaperLight = Color(0xFFECEEEF)'); // surfaceContainer

fs.writeFileSync('app/src/main/java/com/example/ui/theme/Color.kt', content);
