const fs = require('fs');

const slateColors = `    primary = Color(0xFF2E434C),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFF455A64),
    onPrimaryContainer = Color(0xFFBAD0DC),
    secondary = Color(0xFF4A626D),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFCDE6F4),
    onSecondaryContainer = Color(0xFF506873),
    tertiary = Color(0xFF384144),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFF4F585C),
    onTertiaryContainer = Color(0xFFC5CED2),
    background = Color(0xFFF8FAFB),
    onBackground = Color(0xFF191C1D),
    surface = Color(0xFFF8FAFB),
    onSurface = Color(0xFF191C1D),
    surfaceVariant = Color(0xFFE1E3E4),
    onSurfaceVariant = Color(0xFF42474A),
    outline = Color(0xFF73787B),
    outlineVariant = Color(0xFFC2C7CA),
    error = Color(0xFFBA1A1A),
    errorContainer = Color(0xFFFFDAD6),
    onError = Color(0xFFFFFFFF),
    onErrorContainer = Color(0xFF93000A)`;

let content = fs.readFileSync('app/src/main/java/com/example/ui/theme/Theme.kt', 'utf8');

// Replace everything from private val LightColorScheme to the end of its block
content = content.replace(/private val LightColorScheme = lightColorScheme\([\s\S]*?outlineVariant = LedgerBorderTanLight\.copy\(alpha = 0\.5f\)\n\)/, 
`private val LightColorScheme = lightColorScheme(\n${slateColors}\n)`);

fs.writeFileSync('app/src/main/java/com/example/ui/theme/Theme.kt', content);
