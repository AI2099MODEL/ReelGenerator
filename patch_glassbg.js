const fs = require('fs');
let content = fs.readFileSync('app/src/main/java/com/example/ui/theme/Color.kt', 'utf8');

content = content.replace(/val GlassBg = Color\.White\.copy\(alpha = 0\.4f\)/, 'val GlassBg = Color(0xFFF8FAFB).copy(alpha = 0.8f)');
content = content.replace(/val GlassBorder = Color\.White\.copy\(alpha = 0\.6f\)/, 'val GlassBorder = Color(0xFFC2C7CA).copy(alpha = 0.3f)');

fs.writeFileSync('app/src/main/java/com/example/ui/theme/Color.kt', content);
