const fs = require('fs');
let content = fs.readFileSync('app/src/main/java/com/example/ui/screens/DiaryScreen.kt', 'utf8');

content = content.replace(/val LWParchment = Color\(0xFFF8FAFB\)/, 'val LWParchment = Color(0xFFFFF8EF)');
content = content.replace(/val LWParchmentDark = Color\(0xFFECEEEF\)/, 'val LWParchmentDark = Color(0xFFF5EDDE)');
content = content.replace(/val LWNavy = Color\(0xFF191C1D\)/, 'val LWNavy = Color(0xFF1B263B)');
content = content.replace(/val LWSurfaceVariant = Color\(0xFF42474A\)/, 'val LWSurfaceVariant = Color(0xFF45474D)');
content = content.replace(/val LWOutline = Color\(0xFF73787B\)/, 'val LWOutline = Color(0xFFC5C6CD)');
content = content.replace(/val LWHover = Color\(0xFFE1E3E4\)/, 'val LWHover = Color(0xFFE9E2D3)');

fs.writeFileSync('app/src/main/java/com/example/ui/screens/DiaryScreen.kt', content);
