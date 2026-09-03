const fs = require('fs');
let content = fs.readFileSync('app/src/main/java/com/example/ui/components/BinderNavigation.kt', 'utf8');

const imports = `
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import kotlin.math.sin
`;

if (!content.includes('import kotlin.math.sin')) {
    content = content.replace('import androidx.compose.ui.graphics.vector.ImageVector', 'import androidx.compose.ui.graphics.vector.ImageVector' + imports);
}

const waterBgCode = `
@Composable
fun NavWaterFlowBackground() {
    val infiniteTransition = rememberInfiniteTransition(label = "water")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 100f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "time"
    )
    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val c1 = Offset(w * (0.5f + 0.3f * sin(time * 0.1f)), h * (0.5f + 0.2f * sin(time * 0.13f)))
        val c2 = Offset(w * (0.2f + 0.4f * sin(time * 0.15f)), h * (0.8f + 0.3f * sin(time * 0.08f)))
        
        drawRect(color = GlassBg)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFF81C784).copy(alpha = 0.2f), Color.Transparent),
                center = c1,
                radius = w * 0.8f
            ),
            center = c1,
            radius = w * 0.8f
        )
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFF4FC3F7).copy(alpha = 0.2f), Color.Transparent),
                center = c2,
                radius = w * 0.9f
            ),
            center = c2,
            radius = w * 0.9f
        )
    }
}
`;

if (!content.includes('NavWaterFlowBackground')) {
    content = content + waterBgCode;
}

content = content.replace(/Surface\(\s*modifier = modifier\s*\.fillMaxWidth\(\)\s*\.navigationBarsPadding\(\),\s*color = GlassBg,\s*border = androidx\.compose\.foundation\.BorderStroke\(1\.dp, GlassBorder\)\s*\)\s*\{/g, `Surface(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
        color = Color.Transparent,
        border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            NavWaterFlowBackground()`);

content = content.replace(/Row\(\s*modifier = Modifier\s*\.fillMaxWidth\(\)\s*\.padding\(vertical = 4\.dp\)/, `Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)`);

// Close the Box in the LedgerBinderBottomBar
content = content.replace(/LedgerSection\.values\(\)\.forEach \{ section ->[\s\S]*?\}\s*\)\s*\}/, match => match + "\n        }\n");

fs.writeFileSync('app/src/main/java/com/example/ui/components/BinderNavigation.kt', content);
