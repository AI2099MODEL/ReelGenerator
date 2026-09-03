const fs = require('fs');
let content = fs.readFileSync('app/src/main/java/com/example/ui/theme/Color.kt', 'utf8');

content = content.replace(/val GlassBubbleReceived = Color\(0xFFFFE4E6\)\.copy\(alpha = 0\.6f\)/, 'val GlassBubbleReceived = Color(0xFFE1E3E4).copy(alpha = 0.6f)'); // slate surfaceVariant
content = content.replace(/val GlassBubbleSent = Color\(0xFFD81B60\)\.copy\(alpha = 0\.85f\)/, 'val GlassBubbleSent = Color(0xFF2E434C).copy(alpha = 0.85f)'); // slate primary
content = content.replace(/val RoseCustom = Color\(0xFFD81B60\)/, 'val RoseCustom = Color(0xFF2E434C)');
content = content.replace(/val RoseCustomDark = Color\(0xFFAD144A\)/, 'val RoseCustomDark = Color(0xFF191C1D)');
content = content.replace(/val RoseCustomLight = Color\(0xFFFCE4EC\)/, 'val RoseCustomLight = Color(0xFFCDE6F4)'); // secondary container

fs.writeFileSync('app/src/main/java/com/example/ui/theme/Color.kt', content);
