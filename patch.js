const fs = require('fs');
let content = fs.readFileSync('app/src/main/java/com/example/ui/screens/DiaryScreen.kt', 'utf8');
content = content.replace(/Image\(\s*painter = painterResource\(id = R\.drawable\.bg_fern\),[\s\S]*?alpha = 0\.15f\s*\)/, `Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Color(0xFF81C784).copy(alpha=0.3f), Color(0xFF388E3C).copy(alpha=0.3f))
                        )
                    )
            )`);
fs.writeFileSync('app/src/main/java/com/example/ui/screens/DiaryScreen.kt', content);
