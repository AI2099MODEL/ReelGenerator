const fs = require('fs');

const slateColors = `
    primary = Color(0xFF2e434c),
    onPrimary = Color(0xFFffffff),
    primaryContainer = Color(0xFF455a64),
    onPrimaryContainer = Color(0xFFbad0dc),
    secondary = Color(0xFF4a626d),
    onSecondary = Color(0xFFffffff),
    secondaryContainer = Color(0xFFcde6f4),
    onSecondaryContainer = Color(0xFF506873),
    tertiary = Color(0xFF384144),
    onTertiary = Color(0xFFffffff),
    tertiaryContainer = Color(0xFF4f585c),
    onTertiaryContainer = Color(0xFFc5ced2),
    background = Color(0xFFf8fafb),
    onBackground = Color(0xFF191c1d),
    surface = Color(0xFFf8fafb),
    onSurface = Color(0xFF191c1d),
    surfaceVariant = Color(0xFFe1e3e4),
    onSurfaceVariant = Color(0xFF42474a),
    outline = Color(0xFF73787b),
    outlineVariant = Color(0xFFc2c7ca),
    error = Color(0xFFba1a1a),
    errorContainer = Color(0xFFffdad6),
    onError = Color(0xFFffffff),
    onErrorContainer = Color(0xFF93000a)
`;

let themeKt = fs.readFileSync('app/src/main/java/com/example/ui/theme/Theme.kt', 'utf8');

// Replace LightColorScheme
themeKt = themeKt.replace(/private val LightColorScheme = lightColorScheme\([\s\S]*?\)/, 
\`private val LightColorScheme = lightColorScheme(
\${slateColors}
)\`);

fs.writeFileSync('app/src/main/java/com/example/ui/theme/Theme.kt', themeKt);
