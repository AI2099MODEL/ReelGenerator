const fs = require('fs');
let content = fs.readFileSync('app/src/main/java/com/example/ui/screens/DiaryScreen.kt', 'utf8');

// Replace Living Water Palette with standard Slate Theme Colors
content = content.replace(/val LWParchment = Color\(0xFFFFF8EF\)/, 'val LWParchment = Color(0xFFF8FAFB)'); // surface
content = content.replace(/val LWParchmentDark = Color\(0xFFF5EDDE\)/, 'val LWParchmentDark = Color(0xFFECEEEF)'); // surface-container
content = content.replace(/val LWNavy = Color\(0xFF1B263B\)/, 'val LWNavy = Color(0xFF191C1D)'); // on-surface
content = content.replace(/val LWSurfaceVariant = Color\(0xFF45474D\)/, 'val LWSurfaceVariant = Color(0xFF42474A)'); // on-surface-variant
content = content.replace(/val LWOutline = Color\(0xFFC5C6CD\)/, 'val LWOutline = Color(0xFF73787B)'); // outline
content = content.replace(/val LWHover = Color\(0xFFE9E2D3\)/, 'val LWHover = Color(0xFFE1E3E4)'); // surface-variant

// Remove LivingWaterBackground call
content = content.replace(/LivingWaterBackground\(\)/, '// Removed LivingWaterBackground for Slate theme');

fs.writeFileSync('app/src/main/java/com/example/ui/screens/DiaryScreen.kt', content);
