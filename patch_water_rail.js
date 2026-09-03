const fs = require('fs');
let content = fs.readFileSync('app/src/main/java/com/example/ui/components/BinderNavigation.kt', 'utf8');

content = content.replace(/Surface\(\s*modifier = modifier\s*\.fillMaxHeight\(\)\s*\.width\(88\.dp\),\s*color = GlassBg,\s*border = androidx\.compose\.foundation\.BorderStroke\(1\.dp, GlassBorder\)\s*\)\s*\{/, `Surface(
        modifier = modifier
            .fillMaxHeight()
            .width(88.dp),
        color = Color.Transparent,
        border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder)
    ) {
        Box(modifier = Modifier.fillMaxHeight()) {
            NavWaterFlowBackground()`);

content = content.replace(/Column\(\s*modifier = Modifier\s*\.fillMaxSize\(\)/, `Column(
                modifier = Modifier
                    .fillMaxSize()`);

// We need to close the Box at the end of LedgerBinderNavRail.
// LedgerBinderNavRail ends right before `@Composable \n private fun BinderTabItem`
// or just find the closing bracket of the Column inside LedgerBinderNavRail.
content = content.replace(/LedgerSection\.values\(\)\.forEach \{ section ->[\s\S]*?\}\s*\}\s*\}/, match => match + "\n        }");

fs.writeFileSync('app/src/main/java/com/example/ui/components/BinderNavigation.kt', content);
