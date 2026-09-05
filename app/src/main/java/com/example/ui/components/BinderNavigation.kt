package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.geometry.Offset
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.LedgerSection
import com.example.ui.theme.*

data class NavTabItem(
    val section: LedgerSection,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val tabColor: Color
)

private val NAV_ITEMS = listOf(
    NavTabItem(
        section = LedgerSection.IMAGES,
        label = "Collage",
        selectedIcon = Icons.Filled.PhotoLibrary,
        unselectedIcon = Icons.Outlined.PhotoLibrary,
        tabColor = Color(0xFF38BDF8)
    ),
    NavTabItem(
        section = LedgerSection.HOME,
        label = "Home",
        selectedIcon = Icons.Filled.Home,
        unselectedIcon = Icons.Outlined.Home,
        tabColor = Color(0xFF0284C7)
    ),
    NavTabItem(
        section = LedgerSection.TASKS,
        label = "Tasks",
        selectedIcon = Icons.Filled.TaskAlt,
        unselectedIcon = Icons.Outlined.TaskAlt,
        tabColor = Color(0xFF0EA5E9)
    ),
    NavTabItem(
        section = LedgerSection.EVENTS,
        label = "Events",
        selectedIcon = Icons.Filled.Cake,
        unselectedIcon = Icons.Outlined.Cake,
        tabColor = Color(0xFF0284C7)
    ),
    NavTabItem(
        section = LedgerSection.VAULT,
        label = "Vault",
        selectedIcon = Icons.Filled.Lock,
        unselectedIcon = Icons.Outlined.Lock,
        tabColor = Color(0xFF0369A1)
    )
)

/**
 * Animated Light Blue Watercolor Background with Dynamic Ambient Water Ripple Effects and Swimming Fishes
 */
@Composable
fun TabBarWaterBackground(
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "tab_bar_watercolor_water")

    // Continuous smooth wave phase
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(14000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    // Continuous horizontal color gradient shift
    val colorShift by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(18000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "color_shift"
    )

    // Tail wiggle gentle continuous oscillation for fish swimming (slower & smoother)
    val tailWiggle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(750, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "fish_tail_wiggle"
    )

    // Fish 1: Golden Orange Koi (swimming Left -> Right across upper-mid stream - slower)
    val fish1Progress by infiniteTransition.animateFloat(
        initialValue = -0.15f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(24000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "fish1"
    )

    // Fish 2: Ruby / Rose Quartz Fish (swimming Right -> Left across lower-mid stream - slower)
    val fish2Progress by infiniteTransition.animateFloat(
        initialValue = 1.15f,
        targetValue = -0.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(30000, delayMillis = 1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "fish2"
    )

    // Fish 3: Small Azure Water Baby Fish (swimming Left -> Right following Fish 1 - slower)
    val fish3Progress by infiniteTransition.animateFloat(
        initialValue = -0.2f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(19000, delayMillis = 4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "fish3"
    )

    // Fish 4: Coral Gold Fish (swimming Left -> Right across lower stream - slower)
    val fish4Progress by infiniteTransition.animateFloat(
        initialValue = -0.15f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(32000, delayMillis = 6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "fish4"
    )

    // Fish 5: Bright Sky Blue Fish (swimming Right -> Left across upper stream - slower)
    val fish5Progress by infiniteTransition.animateFloat(
        initialValue = 1.2f,
        targetValue = -0.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(27000, delayMillis = 8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "fish5"
    )

    // Small Tortoise: Swimming Left -> Right across lower stream paddling with flippers
    val tortoiseProgress by infiniteTransition.animateFloat(
        initialValue = -0.18f,
        targetValue = 1.18f,
        animationSpec = infiniteRepeatable(
            animation = tween(38000, delayMillis = 3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "tortoise"
    )
    val tortoiseFlipper by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "tortoise_flipper"
    )

    // Small Octopus: Floating / swimming Right -> Left across mid stream with waving tentacles
    val octopusProgress by infiniteTransition.animateFloat(
        initialValue = 1.2f,
        targetValue = -0.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(34000, delayMillis = 10000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "octopus"
    )
    val octopusTentacle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "octopus_tentacle"
    )

    // Pulsing background ambient ripple 1 (Left-center ripple origin)
    val ripple1Progress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rip1"
    )

    // Pulsing background ambient ripple 2 (Right-center ripple origin)
    val ripple2Progress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, delayMillis = 2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rip2"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        // Light grey palette for tab base background
        val lightGreyBase = listOf(
            Color.Transparent,
            Color.Transparent
        )

        // 2. Light Blue Watercolor Animated Ambient Water Ripple Rings
        val r1Center = Offset(width * 0.25f, height * 0.5f)
        val r1MaxRadius = width * 0.5f
        val r1Alpha = (1f - ripple1Progress) * 0.50f
        val r1Radius = (r1MaxRadius * ripple1Progress).coerceAtLeast(1f)
        if (r1Alpha > 0.01f) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFFBAE6FD).copy(alpha = r1Alpha * 0.45f), // Soft Light Blue Water Glow
                        Color(0xFF7DD3FC).copy(alpha = r1Alpha * 0.35f), // Aquamarine Water Ring
                        Color.Transparent
                    ),
                    center = r1Center,
                    radius = r1Radius
                ),
                radius = r1Radius,
                center = r1Center
            )
            drawCircle(
                color = Color(0xFF38BDF8).copy(alpha = r1Alpha * 0.45f), // Light Blue Water Edge
                radius = r1Radius,
                center = r1Center,
                style = Stroke(width = 2.dp.toPx())
            )
        }

        val r2Center = Offset(width * 0.75f, height * 0.5f)
        val r2MaxRadius = width * 0.5f
        val r2Alpha = (1f - ripple2Progress) * 0.50f
        val r2Radius = (r2MaxRadius * ripple2Progress).coerceAtLeast(1f)
        if (r2Alpha > 0.01f) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFFE0F2FE).copy(alpha = r2Alpha * 0.50f), // Sky Water Glow
                        Color(0xFF38BDF8).copy(alpha = r2Alpha * 0.35f), // Light Blue Ring
                        Color.Transparent
                    ),
                    center = r2Center,
                    radius = r2Radius
                ),
                radius = r2Radius,
                center = r2Center
            )
            drawCircle(
                color = Color(0xFF0284C7).copy(alpha = r2Alpha * 0.40f), // Cerulean Water Edge
                radius = r2Radius,
                center = r2Center,
                style = Stroke(width = 2.dp.toPx())
            )
        }

        // 3. Fluid Light Blue Watercolor Ribbon Layer
        val path = Path()
        path.moveTo(0f, height * 0.15f)

        var x = 0f
        val step = 8f
        while (x <= width) {
            val y = height * 0.35f + kotlin.math.sin((x / width * 3 * Math.PI + phase).toDouble()).toFloat() * 7f
            path.lineTo(x, y)
            x += step
        }
        path.lineTo(width, height)
        path.lineTo(0f, height)
        path.close()

        val lightBlueWatercolorGradient = Brush.horizontalGradient(
            colors = listOf(
                Color(0xFFE0F2FE).copy(alpha = 0.40f), // Light Sky Blue Water
                Color(0xFFBAE6FD).copy(alpha = 0.50f), // Soft Aquamarine Water
                Color(0xFF7DD3FC).copy(alpha = 0.45f), // Light Blue Water
                Color(0xFF38BDF8).copy(alpha = 0.35f), // Bright Water Blue
                Color(0xFF0284C7).copy(alpha = 0.25f), // Ocean Water Accent
                Color(0xFFE0F2FE).copy(alpha = 0.40f)  // Light Sky Blue Water
            ),
            startX = width * (1f - colorShift),
            endX = width * (2f - colorShift)
        )

        drawPath(
            path = path,
            brush = lightBlueWatercolorGradient
        )

        // 4. ANIMATED SWIMMING FISHES IN THE WATER STREAM
        // Fish 1: Golden Orange Koi Swimming Right
        val f1X = width * fish1Progress
        val f1Y = height * 0.32f + kotlin.math.sin((fish1Progress * 4 * Math.PI).toDouble()).toFloat() * (height * 0.12f)
        drawSwimmingFish(
            centerX = f1X,
            centerY = f1Y,
            length = 26.dp.toPx(),
            height = 8.5.dp.toPx(),
            facingRight = true,
            tailWiggle = tailWiggle,
            primaryColor = Color(0xFFEA580C),
            secondaryColor = Color(0xFFFBBF24),
            alpha = 0.82f
        )

        // Fish 2: Ruby / Rose Quartz Swimming Left
        val f2X = width * fish2Progress
        val f2Y = height * 0.68f + kotlin.math.cos((fish2Progress * 3.5 * Math.PI).toDouble()).toFloat() * (height * 0.10f)
        drawSwimmingFish(
            centerX = f2X,
            centerY = f2Y,
            length = 24.dp.toPx(),
            height = 8.dp.toPx(),
            facingRight = false,
            tailWiggle = tailWiggle + 1.2f,
            primaryColor = Color(0xFFE11D48),
            secondaryColor = Color(0xFFFDA4AF),
            alpha = 0.80f
        )

        // Fish 3: Azure Water Baby Fish Swimming Right
        val f3X = width * fish3Progress
        val f3Y = height * 0.46f + kotlin.math.sin((fish3Progress * 5 * Math.PI).toDouble()).toFloat() * (height * 0.08f)
        drawSwimmingFish(
            centerX = f3X,
            centerY = f3Y,
            length = 18.dp.toPx(),
            height = 6.dp.toPx(),
            facingRight = true,
            tailWiggle = tailWiggle * 1.4f,
            primaryColor = Color(0xFF0284C7),
            secondaryColor = Color(0xFF7DD3FC),
            alpha = 0.78f
        )

        // Fish 4: Coral Sunset Fish Swimming Right
        val f4X = width * fish4Progress
        val f4Y = height * 0.78f + kotlin.math.sin((fish4Progress * 3 * Math.PI).toDouble()).toFloat() * (height * 0.09f)
        drawSwimmingFish(
            centerX = f4X,
            centerY = f4Y,
            length = 22.dp.toPx(),
            height = 7.5.dp.toPx(),
            facingRight = true,
            tailWiggle = tailWiggle + 2.5f,
            primaryColor = Color(0xFFF97316),
            secondaryColor = Color(0xFFFED7AA),
            alpha = 0.75f
        )

        // Fish 5: Bright Cyan / Sky Blue Fish Swimming Left
        val f5X = width * fish5Progress
        val f5Y = height * 0.22f + kotlin.math.cos((fish5Progress * 4 * Math.PI).toDouble()).toFloat() * (height * 0.08f)
        drawSwimmingFish(
            centerX = f5X,
            centerY = f5Y,
            length = 20.dp.toPx(),
            height = 6.5.dp.toPx(),
            facingRight = false,
            tailWiggle = tailWiggle + 0.8f,
            primaryColor = Color(0xFF06B6D4),
            secondaryColor = Color(0xFFA5F3FC),
            alpha = 0.80f
        )

        // 5. ANIMATED SMALL TORTOISE SWIMMING WITH FISHES
        val tortX = width * tortoiseProgress
        val tortY = height * 0.74f + kotlin.math.sin((tortoiseProgress * 3 * Math.PI).toDouble()).toFloat() * (height * 0.08f)
        drawSwimmingTortoise(
            centerX = tortX,
            centerY = tortY,
            flipperAngle = kotlin.math.sin(tortoiseFlipper.toDouble()).toFloat(),
            facingRight = true,
            alpha = 0.85f
        )

        // 6. ANIMATED SMALL OCTOPUS FLOATING WITH FISHES
        val octoX = width * octopusProgress
        val octoY = height * 0.38f + kotlin.math.cos((octopusProgress * 3.5 * Math.PI).toDouble()).toFloat() * (height * 0.09f)
        drawSwimmingOctopus(
            centerX = octoX,
            centerY = octoY,
            tentaclePhase = octopusTentacle,
            facingRight = false,
            alpha = 0.82f
        )
    }
}

/**
 * Renders a graceful swimming fish with wiggling caudal tail, dorsal fin, pectoral fin, and swimming trail bubbles.
 */
private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawSwimmingFish(
    centerX: Float,
    centerY: Float,
    length: Float,
    height: Float,
    facingRight: Boolean,
    tailWiggle: Float,
    primaryColor: Color,
    secondaryColor: Color,
    alpha: Float
) {
    val dir = if (facingRight) 1f else -1f

    // 1. Glistening Stream Bubbles trailing the fish
    val bubbleAlpha = (alpha * 0.45f).coerceIn(0f, 1f)
    drawCircle(
        color = Color.White.copy(alpha = bubbleAlpha),
        radius = (length * 0.09f).coerceAtLeast(1.2f),
        center = Offset(centerX - dir * length * 0.75f, centerY - 2.5f)
    )
    drawCircle(
        color = Color(0xFFBAE6FD).copy(alpha = bubbleAlpha * 0.75f),
        radius = (length * 0.06f).coerceAtLeast(0.9f),
        center = Offset(centerX - dir * length * 1.05f, centerY + 3f)
    )

    // 2. Wiggling Translucent Tail Fin
    val tailPath = Path()
    val tailBaseX = centerX - dir * (length * 0.42f)
    val tailBaseY = centerY
    val tailTipX = centerX - dir * (length * 0.82f)
    val wiggleOffset = kotlin.math.sin(tailWiggle.toDouble()).toFloat() * (height * 0.45f)

    tailPath.moveTo(tailBaseX, tailBaseY)
    tailPath.quadraticBezierTo(
        tailBaseX - dir * (length * 0.18f),
        tailBaseY - height * 0.35f + wiggleOffset * 0.5f,
        tailTipX,
        tailBaseY - height * 0.55f + wiggleOffset
    )
    tailPath.quadraticBezierTo(
        tailBaseX - dir * (length * 0.32f),
        tailBaseY + wiggleOffset * 0.3f,
        tailTipX,
        tailBaseY + height * 0.55f + wiggleOffset
    )
    tailPath.quadraticBezierTo(
        tailBaseX - dir * (length * 0.18f),
        tailBaseY + height * 0.35f + wiggleOffset * 0.5f,
        tailBaseX,
        tailBaseY
    )
    tailPath.close()

    drawPath(
        path = tailPath,
        brush = Brush.radialGradient(
            colors = listOf(
                secondaryColor.copy(alpha = alpha * 0.85f),
                primaryColor.copy(alpha = alpha * 0.65f),
                Color.Transparent
            ),
            center = Offset(tailBaseX, tailBaseY),
            radius = length * 0.48f
        )
    )

    // 3. Streamlined Fish Body Path
    val bodyPath = Path()
    val noseX = centerX + dir * (length * 0.50f)
    val noseY = centerY

    bodyPath.moveTo(noseX, noseY)
    // Top body arch
    bodyPath.cubicTo(
        centerX + dir * (length * 0.22f), noseY - height * 0.52f,
        centerX - dir * (length * 0.18f), noseY - height * 0.48f,
        tailBaseX, tailBaseY
    )
    // Bottom body arch
    bodyPath.cubicTo(
        centerX - dir * (length * 0.18f), noseY + height * 0.48f,
        centerX + dir * (length * 0.22f), noseY + height * 0.52f,
        noseX, noseY
    )
    bodyPath.close()

    // Gradient shading for 3D curved fish body
    val bodyBrush = Brush.linearGradient(
        colors = listOf(
            secondaryColor.copy(alpha = alpha * 0.95f),
            primaryColor.copy(alpha = alpha),
            secondaryColor.copy(alpha = alpha * 0.80f)
        ),
        start = Offset(noseX, noseY),
        end = Offset(tailBaseX, tailBaseY)
    )
    drawPath(path = bodyPath, brush = bodyBrush)

    // 4. Dorsal Fin (Top fin)
    val dorsalPath = Path()
    val dorsalStartX = centerX - dir * (length * 0.08f)
    val dorsalStartY = centerY - height * 0.42f
    dorsalPath.moveTo(dorsalStartX, dorsalStartY)
    dorsalPath.quadraticBezierTo(
        dorsalStartX - dir * (length * 0.12f),
        dorsalStartY - height * 0.32f,
        dorsalStartX - dir * (length * 0.28f),
        dorsalStartY + height * 0.10f
    )
    dorsalPath.close()
    drawPath(
        path = dorsalPath,
        color = secondaryColor.copy(alpha = alpha * 0.70f)
    )

    // 5. Pectoral Fin (Side fin with wiggle)
    val finStartX = centerX + dir * (length * 0.05f)
    val finStartY = centerY + height * 0.12f
    val finWiggle = kotlin.math.cos(tailWiggle.toDouble()).toFloat() * (height * 0.22f)
    val finPath = Path()
    finPath.moveTo(finStartX, finStartY)
    finPath.quadraticBezierTo(
        finStartX - dir * (length * 0.18f),
        finStartY + height * 0.32f + finWiggle,
        finStartX - dir * (length * 0.26f),
        finStartY + height * 0.18f
    )
    finPath.close()
    drawPath(
        path = finPath,
        color = Color.White.copy(alpha = alpha * 0.65f)
    )

    // 6. Fish Eye with Specular Glint
    val eyeX = centerX + dir * (length * 0.32f)
    val eyeY = centerY - height * 0.12f
    drawCircle(
        color = Color(0xFF0F172A).copy(alpha = alpha * 0.95f),
        radius = (height * 0.15f).coerceAtLeast(1.2f),
        center = Offset(eyeX, eyeY)
    )
    drawCircle(
        color = Color.White.copy(alpha = alpha),
        radius = (height * 0.06f).coerceAtLeast(0.6f),
        center = Offset(eyeX + dir * 0.5f, eyeY - 0.5f)
    )
}

/**
 * Renders an animated swimming small tortoise paddling alongside fishes in the water.
 */
private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawSwimmingTortoise(
    centerX: Float,
    centerY: Float,
    flipperAngle: Float,
    facingRight: Boolean,
    alpha: Float
) {
    val dir = if (facingRight) 1f else -1f

    // Glistening stream bubbles trailing behind tortoise
    val bubbleAlpha = (alpha * 0.45f).coerceIn(0f, 1f)
    drawCircle(
        color = Color(0xFFBAE6FD).copy(alpha = bubbleAlpha),
        radius = 2.dp.toPx(),
        center = Offset(centerX - dir * 14.dp.toPx(), centerY + 1.dp.toPx())
    )
    drawCircle(
        color = Color.White.copy(alpha = bubbleAlpha * 0.7f),
        radius = 1.4.dp.toPx(),
        center = Offset(centerX - dir * 19.dp.toPx(), centerY - 3.dp.toPx())
    )

    // Back flippers
    val backFlipperY = centerY + 4.5.dp.toPx() + flipperAngle * 1.5.dp.toPx()
    drawOval(
        color = Color(0xFF34D399).copy(alpha = alpha),
        topLeft = Offset(centerX - dir * 8.dp.toPx(), backFlipperY),
        size = androidx.compose.ui.geometry.Size(6.dp.toPx(), 3.5.dp.toPx())
    )

    // Front Paddling Flipper
    val flipperWiggle = flipperAngle * 3.5.dp.toPx()
    val topFlipper = Path().apply {
        moveTo(centerX + dir * 1.dp.toPx(), centerY - 2.5.dp.toPx())
        quadraticBezierTo(
            centerX + dir * 6.dp.toPx(), centerY - 8.dp.toPx() + flipperWiggle,
            centerX + dir * 11.dp.toPx(), centerY - 4.5.dp.toPx() + flipperWiggle
        )
        quadraticBezierTo(
            centerX + dir * 6.dp.toPx(), centerY - 1.dp.toPx(),
            centerX + dir * 1.dp.toPx(), centerY - 2.5.dp.toPx()
        )
        close()
    }
    drawPath(topFlipper, color = Color(0xFF10B981).copy(alpha = alpha))

    val botFlipper = Path().apply {
        moveTo(centerX + dir * 1.dp.toPx(), centerY + 2.5.dp.toPx())
        quadraticBezierTo(
            centerX + dir * 6.dp.toPx(), centerY + 8.dp.toPx() - flipperWiggle,
            centerX + dir * 11.dp.toPx(), centerY + 4.5.dp.toPx() - flipperWiggle
        )
        quadraticBezierTo(
            centerX + dir * 6.dp.toPx(), centerY + 1.dp.toPx(),
            centerX + dir * 1.dp.toPx(), centerY + 2.5.dp.toPx()
        )
        close()
    }
    drawPath(botFlipper, color = Color(0xFF10B981).copy(alpha = alpha))

    // Tail
    val tail = Path().apply {
        moveTo(centerX - dir * 8.dp.toPx(), centerY)
        lineTo(centerX - dir * 12.dp.toPx(), centerY - 1.dp.toPx())
        lineTo(centerX - dir * 8.dp.toPx(), centerY + 2.dp.toPx())
        close()
    }
    drawPath(tail, color = Color(0xFF34D399).copy(alpha = alpha))

    // Shell
    drawOval(
        brush = Brush.radialGradient(
            colors = listOf(
                Color(0xFF6EE7B7).copy(alpha = alpha),
                Color(0xFF10B981).copy(alpha = alpha),
                Color(0xFF047857).copy(alpha = alpha)
            ),
            center = Offset(centerX, centerY),
            radius = 9.dp.toPx()
        ),
        topLeft = Offset(centerX - 8.dp.toPx(), centerY - 6.5.dp.toPx()),
        size = androidx.compose.ui.geometry.Size(16.dp.toPx(), 13.dp.toPx())
    )

    // Shell outline
    drawOval(
        color = Color(0xFF065F46).copy(alpha = alpha * 0.7f),
        topLeft = Offset(centerX - 8.dp.toPx(), centerY - 6.5.dp.toPx()),
        size = androidx.compose.ui.geometry.Size(16.dp.toPx(), 13.dp.toPx()),
        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.dp.toPx())
    )

    // Head
    val headX = centerX + dir * 9.5.dp.toPx()
    val headY = centerY
    drawCircle(
        color = Color(0xFF10B981).copy(alpha = alpha),
        radius = 3.5.dp.toPx(),
        center = Offset(headX, headY)
    )

    // Eye
    drawCircle(
        color = Color(0xFF064E3B).copy(alpha = alpha),
        radius = 1.dp.toPx(),
        center = Offset(headX + dir * 1.2.dp.toPx(), headY - 1.dp.toPx())
    )
    drawCircle(
        color = Color.White.copy(alpha = alpha),
        radius = 0.4.dp.toPx(),
        center = Offset(headX + dir * 1.5.dp.toPx(), headY - 1.3.dp.toPx())
    )
}

/**
 * Renders an animated floating/swimming octopus with undulating tentacles alongside fishes in the water.
 */
private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawSwimmingOctopus(
    centerX: Float,
    centerY: Float,
    tentaclePhase: Float,
    facingRight: Boolean,
    alpha: Float
) {
    val dir = if (facingRight) 1f else -1f

    // Micro bubble above octopus
    drawCircle(
        color = Color(0x99BAE6FD),
        radius = 1.5.dp.toPx(),
        center = Offset(centerX + 3.dp.toPx(), centerY - 10.dp.toPx())
    )

    // Tentacles trailing behind / undulating
    val tentacleOffsets = listOf(-5.dp.toPx(), -2.5.dp.toPx(), 0f, 2.5.dp.toPx(), 5.dp.toPx())
    val tentacleBaseY = centerY + 4.5.dp.toPx()

    tentacleOffsets.forEachIndexed { idx, xOff ->
        val wave = kotlin.math.sin((tentaclePhase + idx * 0.85f).toDouble()).toFloat() * 3.dp.toPx()
        val tentaclePath = Path().apply {
            moveTo(centerX + xOff, tentacleBaseY)
            quadraticBezierTo(
                centerX + xOff + wave,
                tentacleBaseY + 4.dp.toPx(),
                centerX + xOff + wave * 0.7f - dir * 2.dp.toPx(),
                tentacleBaseY + 8.5.dp.toPx()
            )
        }
        drawPath(
            path = tentaclePath,
            color = if (idx % 2 == 0) Color(0xFFF43F5E).copy(alpha = alpha) else Color(0xFFFB7185).copy(alpha = alpha),
            style = androidx.compose.ui.graphics.drawscope.Stroke(
                width = 1.8.dp.toPx(),
                cap = androidx.compose.ui.graphics.StrokeCap.Round
            )
        )
    }

    // Octopus Dome Head
    drawOval(
        brush = Brush.radialGradient(
            colors = listOf(
                Color(0xFFFDA4AF).copy(alpha = alpha),
                Color(0xFFF43F5E).copy(alpha = alpha),
                Color(0xFFE11D48).copy(alpha = alpha)
            ),
            center = Offset(centerX, centerY - 1.dp.toPx()),
            radius = 7.5.dp.toPx()
        ),
        topLeft = Offset(centerX - 7.dp.toPx(), centerY - 7.dp.toPx()),
        size = androidx.compose.ui.geometry.Size(14.dp.toPx(), 13.dp.toPx())
    )

    // Cheeks
    drawCircle(
        color = Color(0x66FFED4A),
        radius = 1.2.dp.toPx(),
        center = Offset(centerX - 4.dp.toPx(), centerY + 2.dp.toPx())
    )
    drawCircle(
        color = Color(0x66FFED4A),
        radius = 1.2.dp.toPx(),
        center = Offset(centerX + 4.dp.toPx(), centerY + 2.dp.toPx())
    )

    // Eyes
    drawCircle(
        color = Color(0xFF1E1B4B).copy(alpha = alpha),
        radius = 1.1.dp.toPx(),
        center = Offset(centerX - 2.8.dp.toPx(), centerY)
    )
    drawCircle(
        color = Color.White.copy(alpha = alpha),
        radius = 0.5.dp.toPx(),
        center = Offset(centerX - 2.5.dp.toPx(), centerY - 0.4.dp.toPx())
    )

    drawCircle(
        color = Color(0xFF1E1B4B).copy(alpha = alpha),
        radius = 1.1.dp.toPx(),
        center = Offset(centerX + 2.8.dp.toPx(), centerY)
    )
    drawCircle(
        color = Color.White.copy(alpha = alpha),
        radius = 0.5.dp.toPx(),
        center = Offset(centerX + 3.1.dp.toPx(), centerY - 0.4.dp.toPx())
    )
}

@Composable
fun FloatingSailingBoats(
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "boat_bob")

    val wavePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave_phase"
    )

    val boat1X by infiniteTransition.animateFloat(
        initialValue = -0.1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(25000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "boat1x"
    )

    val boat2X by infiniteTransition.animateFloat(
        initialValue = 1.1f,
        targetValue = -0.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(32000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "boat2x"
    )

    val boat3X by infiniteTransition.animateFloat(
        initialValue = -0.2f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(18000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "boat3x"
    )

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .clipToBounds()
    ) {
        val width = size.width
        val height = size.height
        if (width <= 0 || height <= 0) return@Canvas

        val waterLine = height * 0.7f

        // Draw gentle water ripples underneath
        val waterPath = Path()
        waterPath.moveTo(0f, waterLine)
        var x = 0f
        val step = 10f
        while (x <= width) {
            val y = waterLine + kotlin.math.sin(x * 0.05f + wavePhase).toFloat() * 2f
            waterPath.lineTo(x, y)
            x += step
        }
        waterPath.lineTo(width, height)
        waterPath.lineTo(0f, height)
        waterPath.close()

        drawPath(
            path = waterPath,
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFFBAE6FD).copy(alpha = 0.6f),
                    Color(0xFF38BDF8).copy(alpha = 0.8f)
                ),
                startY = waterLine,
                endY = height
            )
        )

        fun drawBoat(cx: Float, scale: Float, facingRight: Boolean, colorBase: Color, colorSail: Color, phaseOffset: Float) {
            val bobY = kotlin.math.sin(wavePhase + phaseOffset).toFloat() * 2f
            val by = waterLine + bobY - 2.dp.toPx()
            val bw = 12.dp.toPx() * scale
            val bh = 4.dp.toPx() * scale
            val dir = if (facingRight) 1f else -1f

            // Hull
            val hull = Path()
            hull.moveTo(cx - bw, by - bh)
            hull.lineTo(cx + bw, by - bh)
            hull.lineTo(cx + bw * 0.6f, by)
            hull.lineTo(cx - bw * 0.6f, by)
            hull.close()
            drawPath(hull, color = colorBase)

            // Mast
            val mastX = cx + bw * 0.1f * dir
            val mastH = 16.dp.toPx() * scale
            drawLine(
                color = Color(0xFF452719),
                start = Offset(mastX, by - bh),
                end = Offset(mastX, by - bh - mastH),
                strokeWidth = 1.5.dp.toPx()
            )

            // Main Sail
            val sailMain = Path()
            sailMain.moveTo(mastX + 1.dp.toPx() * dir, by - bh - mastH * 0.95f)
            sailMain.lineTo(mastX + bw * 1.1f * dir, by - bh - 1.dp.toPx())
            sailMain.lineTo(mastX + 1.dp.toPx() * dir, by - bh - 1.dp.toPx())
            sailMain.close()
            drawPath(sailMain, color = colorSail)

            // Jib Sail
            val sailJib = Path()
            sailJib.moveTo(mastX - 1.dp.toPx() * dir, by - bh - mastH * 0.8f)
            sailJib.lineTo(mastX - bw * 0.8f * dir, by - bh - 1.dp.toPx())
            sailJib.lineTo(mastX - 1.dp.toPx() * dir, by - bh - 1.dp.toPx())
            sailJib.close()
            drawPath(sailJib, color = colorSail.copy(alpha = 0.85f))
        }

        drawBoat(width * boat1X, 1.2f, true, Color(0xFF8D6E63), Color(0xFFF9FAFB), 0f)
        drawBoat(width * boat2X, 0.8f, false, Color(0xFF5D4037), Color(0xFFE2E8F0), 2f)
        drawBoat(width * boat3X, 1.0f, true, Color(0xFF795548), Color(0xFFF1F5F9), 4f)
    }
}

/**
 * Beautiful Garden with Soil and Living Flora/Fauna, positioned directly above the tab rows.
 * Features:
 * - Rich textured organic soil bed with earth gradients, pebbles, and rootlets
 * - Lush swaying grass blades and clovers
 * - Blooming garden flowers (Roses, Sunflowers, Daisies, Lavender, Tulips)
 * - Animated fluttering butterfly and cute ladybug
 * - Shimmering morning dew drops
 */
@Composable
fun BeautifulGardenWithSoil(
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "garden_life")

    // Breeze swaying stalks and grass
    val windSway by infiniteTransition.animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "wind_sway"
    )

    // Butterfly flight path across garden
    val butterflyX by infiniteTransition.animateFloat(
        initialValue = -0.1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(18000, delayMillis = 1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "butterfly_x"
    )

    // Butterfly wing flap cycle
    val wingFlap by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(220, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "wing_flap"
    )

    // Dew drop sparkle breathing
    val dewSparkle by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dew_sparkle"
    )

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .clipToBounds()
    ) {
        val width = size.width
        val height = size.height
        if (width <= 0 || height <= 0) return@Canvas

        val soilTop = height * 0.58f

        // 1. RICH ORGANIC SOIL BED
        val soilPath = Path().apply {
            moveTo(0f, soilTop)
            // Gentle organic rolling mounds across the soil line
            val step = width / 12f
            for (i in 1..12) {
                val cx = (i - 0.5f) * step
                val cy = soilTop + (if (i % 2 == 0) -2.dp.toPx() else 1.5.dp.toPx())
                val ex = i * step
                val ey = soilTop + (if (i % 3 == 0) 1.dp.toPx() else -1.dp.toPx())
                quadraticBezierTo(cx, cy, ex, ey)
            }
            lineTo(width, height)
            lineTo(0f, height)
            close()
        }

        // Earthy Multi-Layer Soil Gradient
        val soilBrush = Brush.verticalGradient(
            colors = listOf(
                Color(0xFF8D6E63), // Light warm topsoil crumb
                Color(0xFF6D4C41), // Fertile brown soil
                Color(0xFF4E342E), // Rich dark loam
                Color(0xFF3E2723), // Deep moist earth
                Color(0xFF271A14)  // Nutrient-rich base earth
            ),
            startY = soilTop - 3.dp.toPx(),
            endY = height
        )
        drawPath(soilPath, brush = soilBrush)

        // Soil texture pebbles & granules
        val pebbleList = listOf(
            Triple(0.05f, 0.72f, 1.8f),
            Triple(0.12f, 0.82f, 2.2f),
            Triple(0.18f, 0.68f, 1.5f),
            Triple(0.27f, 0.86f, 2.0f),
            Triple(0.35f, 0.74f, 1.6f),
            Triple(0.44f, 0.88f, 2.3f),
            Triple(0.52f, 0.70f, 1.7f),
            Triple(0.61f, 0.82f, 2.1f),
            Triple(0.70f, 0.73f, 1.5f),
            Triple(0.78f, 0.85f, 2.4f),
            Triple(0.86f, 0.72f, 1.6f),
            Triple(0.93f, 0.80f, 2.0f),
            Triple(0.97f, 0.69f, 1.4f)
        )

        pebbleList.forEach { (xFrac, yFrac, rDp) ->
            drawCircle(
                color = Color(0xFF2E1C14).copy(alpha = 0.65f),
                radius = rDp.dp.toPx(),
                center = Offset(width * xFrac, height * yFrac)
            )
            // Pebble highlight glint
            drawCircle(
                color = Color(0xFFA1887F).copy(alpha = 0.5f),
                radius = (rDp * 0.45f).dp.toPx(),
                center = Offset(width * xFrac - 0.5.dp.toPx(), height * yFrac - 0.5.dp.toPx())
            )
        }

        // Delicate rootlets dipping into soil
        for (i in 0..8) {
            val rx = width * (0.08f + i * 0.11f)
            val ry = soilTop + 1.dp.toPx()
            val rootPath = Path().apply {
                moveTo(rx, ry)
                quadraticBezierTo(
                    rx + (if (i % 2 == 0) 3.dp.toPx() else -3.dp.toPx()),
                    ry + 5.dp.toPx(),
                    rx + (if (i % 2 == 0) 1.dp.toPx() else -2.dp.toPx()),
                    ry + 9.dp.toPx()
                )
            }
            drawPath(
                path = rootPath,
                color = Color(0xFF5D4037).copy(alpha = 0.7f),
                style = Stroke(width = 1.dp.toPx())
            )
        }

        // 2. FLOURISHING GARDEN FLOWERS & SPROUTS (Clean grass-free garden)
        // Flower 1: Pink Rose / Peony Bloom (Left)
        drawGardenRose(
            cx = width * 0.06f,
            baseY = soilTop + 1.dp.toPx(),
            stemHeight = 16.dp.toPx(),
            sway = windSway * 2.5.dp.toPx(),
            petalColor = Color(0xFFFB7185),
            petalCenter = Color(0xFFE11D48)
        )

        // Clover Sprout 1
        drawCloverSprout(
            cx = width * 0.12f,
            baseY = soilTop + 1.dp.toPx(),
            sway = windSway * 1.5.dp.toPx()
        )

        // Flower 2: Golden Sunflower / Marigold (Left-Mid)
        drawGardenSunflower(
            cx = width * 0.19f,
            baseY = soilTop + 1.dp.toPx(),
            stemHeight = 18.dp.toPx(),
            sway = windSway * 3.dp.toPx()
        )

        // Flower 3: Soft Peach Blossom (Left-Center)
        drawGardenRose(
            cx = width * 0.27f,
            baseY = soilTop + 1.dp.toPx(),
            stemHeight = 15.dp.toPx(),
            sway = windSway * 2.3.dp.toPx(),
            petalColor = Color(0xFFFDA4AF),
            petalCenter = Color(0xFFF43F5E)
        )

        // Flower 4: Violet Lavender Wildflower (Mid-Left)
        drawGardenLavender(
            cx = width * 0.35f,
            baseY = soilTop + 1.dp.toPx(),
            stemHeight = 19.dp.toPx(),
            sway = windSway * 2.2.dp.toPx()
        )

        // Clover Sprout 2
        drawCloverSprout(
            cx = width * 0.42f,
            baseY = soilTop + 1.dp.toPx(),
            sway = windSway * 1.4.dp.toPx()
        )

        // Flower 5: White Daisy with Sunny Golden Eye (Center)
        drawGardenDaisy(
            cx = width * 0.49f,
            baseY = soilTop + 1.dp.toPx(),
            stemHeight = 17.dp.toPx(),
            sway = windSway * 2.8.dp.toPx()
        )

        // Flower 6: Crimson Poppy / Tulip Blossom (Mid-Right)
        drawGardenRose(
            cx = width * 0.58f,
            baseY = soilTop + 1.dp.toPx(),
            stemHeight = 18.5.dp.toPx(),
            sway = windSway * 3.2.dp.toPx(),
            petalColor = Color(0xFFF43F5E),
            petalCenter = Color(0xFFBE123C)
        )

        // Clover Sprout 3
        drawCloverSprout(
            cx = width * 0.66f,
            baseY = soilTop + 1.dp.toPx(),
            sway = windSway * 1.5.dp.toPx()
        )

        // Flower 7: Radiant Yellow Marigold (Right-Mid)
        drawGardenSunflower(
            cx = width * 0.74f,
            baseY = soilTop + 1.dp.toPx(),
            stemHeight = 16.5.dp.toPx(),
            sway = windSway * 2.6.dp.toPx()
        )

        // Flower 8: Purple Bellflower / Lavender (Right)
        drawGardenLavender(
            cx = width * 0.83f,
            baseY = soilTop + 1.dp.toPx(),
            stemHeight = 17.5.dp.toPx(),
            sway = windSway * 2.0.dp.toPx()
        )

        // Flower 9: Rose Quartz Camellia (Far Right-Mid)
        drawGardenRose(
            cx = width * 0.90f,
            baseY = soilTop + 1.dp.toPx(),
            stemHeight = 15.5.dp.toPx(),
            sway = windSway * 2.2.dp.toPx(),
            petalColor = Color(0xFFF9A8D4),
            petalCenter = Color(0xFFDB2777)
        )

        // Flower 10: Mini White Chamomile (Far Right)
        drawGardenDaisy(
            cx = width * 0.96f,
            baseY = soilTop + 1.dp.toPx(),
            stemHeight = 14.dp.toPx(),
            sway = windSway * 2.4.dp.toPx()
        )

        // 4. ANIMATED LADYBUG ON A GARDEN LEAF
        val ladybugLeafX = width * 0.67f + windSway * 1.5.dp.toPx()
        val ladybugLeafY = soilTop - 5.dp.toPx()
        drawGardenLadybug(cx = ladybugLeafX, cy = ladybugLeafY)

        // 5. ANIMATED FLUTTERING BUTTERFLY GLIDING OVER BLOSSOMS
        val bFlyX = width * butterflyX
        val bFlyY = height * 0.26f + kotlin.math.sin((butterflyX * 6 * Math.PI).toDouble()).toFloat() * 3.5.dp.toPx()
        if (bFlyX in -20f..(width + 20f)) {
            drawGardenButterfly(
                cx = bFlyX,
                cy = bFlyY,
                wingSpread = wingFlap
            )
        }
    }
}

/**
 * Draws a blooming Rose/Peony with stem, leaves, and layered petals.
 */
private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawGardenRose(
    cx: Float,
    baseY: Float,
    stemHeight: Float,
    sway: Float,
    petalColor: Color,
    petalCenter: Color
) {
    val topX = cx + sway
    val topY = baseY - stemHeight

    // Stem
    val stem = Path().apply {
        moveTo(cx, baseY)
        quadraticBezierTo(cx + sway * 0.4f, baseY - stemHeight * 0.5f, topX, topY)
    }
    drawPath(
        path = stem,
        color = Color(0xFF15803D),
        style = Stroke(width = 1.8.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round)
    )

    // Little Green Leaf on stem
    val leafY = baseY - stemHeight * 0.45f
    val leaf = Path().apply {
        moveTo(cx + sway * 0.4f, leafY)
        quadraticBezierTo(
            cx + sway * 0.4f - 4.dp.toPx(), leafY - 3.dp.toPx(),
            cx + sway * 0.4f - 6.dp.toPx(), leafY - 1.dp.toPx()
        )
        quadraticBezierTo(
            cx + sway * 0.4f - 3.dp.toPx(), leafY + 1.dp.toPx(),
            cx + sway * 0.4f, leafY
        )
        close()
    }
    drawPath(leaf, color = Color(0xFF22C55E))

    // Rose Petal Layers
    drawCircle(
        color = petalColor,
        radius = 4.2.dp.toPx(),
        center = Offset(topX, topY)
    )
    drawCircle(
        color = petalCenter,
        radius = 2.4.dp.toPx(),
        center = Offset(topX, topY)
    )
    drawCircle(
        color = Color(0xFFFFF1F2),
        radius = 1.0.dp.toPx(),
        center = Offset(topX - 0.6.dp.toPx(), topY - 0.6.dp.toPx())
    )
}

/**
 * Draws a radiant Sunflower with golden ray petals and chocolate seed center.
 */
private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawGardenSunflower(
    cx: Float,
    baseY: Float,
    stemHeight: Float,
    sway: Float
) {
    val topX = cx + sway
    val topY = baseY - stemHeight

    // Stem
    val stem = Path().apply {
        moveTo(cx, baseY)
        quadraticBezierTo(cx + sway * 0.4f, baseY - stemHeight * 0.5f, topX, topY)
    }
    drawPath(
        path = stem,
        color = Color(0xFF166534),
        style = Stroke(width = 2.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round)
    )

    // Leaf
    val leafY = baseY - stemHeight * 0.5f
    val leaf = Path().apply {
        moveTo(cx + sway * 0.4f, leafY)
        quadraticBezierTo(
            cx + sway * 0.4f + 4.5.dp.toPx(), leafY - 3.dp.toPx(),
            cx + sway * 0.4f + 6.5.dp.toPx(), leafY
        )
        quadraticBezierTo(
            cx + sway * 0.4f + 3.dp.toPx(), leafY + 2.dp.toPx(),
            cx + sway * 0.4f, leafY
        )
        close()
    }
    drawPath(leaf, color = Color(0xFF16A34A))

    // 8 Golden Petals around center
    val petalCount = 8
    val petalDistance = 3.6.dp.toPx()
    for (i in 0 until petalCount) {
        val angle = (i.toFloat() / petalCount) * 2 * Math.PI
        val px = topX + kotlin.math.cos(angle).toFloat() * petalDistance
        val py = topY + kotlin.math.sin(angle).toFloat() * petalDistance
        drawCircle(
            color = Color(0xFFFBBF24),
            radius = 1.8.dp.toPx(),
            center = Offset(px, py)
        )
    }

    // Disk Florets (Brown Center)
    drawCircle(
        color = Color(0xFF78350F),
        radius = 2.6.dp.toPx(),
        center = Offset(topX, topY)
    )
    // Golden Pollen Core
    drawCircle(
        color = Color(0xFFFDE047),
        radius = 1.1.dp.toPx(),
        center = Offset(topX, topY)
    )
}

/**
 * Draws a White Daisy with golden heart.
 */
private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawGardenDaisy(
    cx: Float,
    baseY: Float,
    stemHeight: Float,
    sway: Float
) {
    val topX = cx + sway
    val topY = baseY - stemHeight

    // Stem
    val stem = Path().apply {
        moveTo(cx, baseY)
        quadraticBezierTo(cx + sway * 0.4f, baseY - stemHeight * 0.5f, topX, topY)
    }
    drawPath(
        path = stem,
        color = Color(0xFF15803D),
        style = Stroke(width = 1.6.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round)
    )

    // White Petals
    val petalCount = 6
    val petalDistance = 3.2.dp.toPx()
    for (i in 0 until petalCount) {
        val angle = (i.toFloat() / petalCount) * 2 * Math.PI
        val px = topX + kotlin.math.cos(angle).toFloat() * petalDistance
        val py = topY + kotlin.math.sin(angle).toFloat() * petalDistance
        drawCircle(
            color = Color.White,
            radius = 1.8.dp.toPx(),
            center = Offset(px, py)
        )
    }

    // Sunny Yellow Center
    drawCircle(
        color = Color(0xFFFACC15),
        radius = 2.0.dp.toPx(),
        center = Offset(topX, topY)
    )
}

/**
 * Draws a Lavender / Wildflower spike with clustered purple bell blossoms.
 */
private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawGardenLavender(
    cx: Float,
    baseY: Float,
    stemHeight: Float,
    sway: Float
) {
    val topX = cx + sway
    val topY = baseY - stemHeight

    // Stem
    val stem = Path().apply {
        moveTo(cx, baseY)
        quadraticBezierTo(cx + sway * 0.4f, baseY - stemHeight * 0.5f, topX, topY)
    }
    drawPath(
        path = stem,
        color = Color(0xFF166534),
        style = Stroke(width = 1.6.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round)
    )

    // Purple Florets climbing stem
    val florets = listOf(
        Pair(0.95f, Color(0xFFC084FC)),
        Pair(0.85f, Color(0xFFA855F7)),
        Pair(0.75f, Color(0xFF9333EA)),
        Pair(0.65f, Color(0xFF7E22CE))
    )

    florets.forEach { (progress, color) ->
        val fx = cx + sway * progress
        val fy = baseY - stemHeight * progress
        drawCircle(
            color = color,
            radius = 2.0.dp.toPx(),
            center = Offset(fx - 1.8.dp.toPx(), fy)
        )
        drawCircle(
            color = color,
            radius = 2.0.dp.toPx(),
            center = Offset(fx + 1.8.dp.toPx(), fy)
        )
    }
    // Top spike tip
    drawCircle(
        color = Color(0xFFE9D5FF),
        radius = 1.4.dp.toPx(),
        center = Offset(topX, topY)
    )
}

/**
 * Draws a cute 3-leaf clover sprout emerging from the soil.
 */
private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawCloverSprout(
    cx: Float,
    baseY: Float,
    sway: Float
) {
    val topX = cx + sway * 0.5f
    val topY = baseY - 8.dp.toPx()

    // Tiny stalk
    drawLine(
        color = Color(0xFF15803D),
        start = Offset(cx, baseY),
        end = Offset(topX, topY),
        strokeWidth = 1.2.dp.toPx(),
        cap = androidx.compose.ui.graphics.StrokeCap.Round
    )

    // 3 clover heart leaves
    drawCircle(color = Color(0xFF22C55E), radius = 1.8.dp.toPx(), center = Offset(topX - 1.8.dp.toPx(), topY - 1.dp.toPx()))
    drawCircle(color = Color(0xFF16A34A), radius = 1.8.dp.toPx(), center = Offset(topX + 1.8.dp.toPx(), topY - 1.dp.toPx()))
    drawCircle(color = Color(0xFF4ADE80), radius = 1.8.dp.toPx(), center = Offset(topX, topY - 2.5.dp.toPx()))
}

/**
 * Draws a cute Ladybug perched on a garden leaf.
 */
private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawGardenLadybug(
    cx: Float,
    cy: Float
) {
    // Red Ladybug Body
    drawCircle(
        color = Color(0xFFEF4444),
        radius = 2.4.dp.toPx(),
        center = Offset(cx, cy)
    )
    // Black Ladybug Head
    drawCircle(
        color = Color(0xFF1E293B),
        radius = 1.1.dp.toPx(),
        center = Offset(cx + 1.8.dp.toPx(), cy - 0.8.dp.toPx())
    )
    // Ladybug Spots
    drawCircle(color = Color(0xFF1E293B), radius = 0.5.dp.toPx(), center = Offset(cx - 0.8.dp.toPx(), cy - 0.8.dp.toPx()))
    drawCircle(color = Color(0xFF1E293B), radius = 0.5.dp.toPx(), center = Offset(cx - 0.8.dp.toPx(), cy + 0.8.dp.toPx()))
    drawCircle(color = Color(0xFF1E293B), radius = 0.5.dp.toPx(), center = Offset(cx + 0.6.dp.toPx(), cy))
}

/**
 * Draws an animated fluttering Butterfly over the garden flowers.
 */
private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawGardenButterfly(
    cx: Float,
    cy: Float,
    wingSpread: Float
) {
    val wingW = 4.5.dp.toPx() * wingSpread
    val wingH = 4.5.dp.toPx()

    // Left Wing
    drawOval(
        brush = Brush.radialGradient(
            colors = listOf(Color(0xFFE0F2FE), Color(0xFF38BDF8), Color(0xFF0284C7)),
            center = Offset(cx - wingW * 0.5f, cy),
            radius = wingW
        ),
        topLeft = Offset(cx - wingW, cy - wingH * 0.7f),
        size = androidx.compose.ui.geometry.Size(wingW, wingH)
    )

    // Right Wing
    drawOval(
        brush = Brush.radialGradient(
            colors = listOf(Color(0xFFE0F2FE), Color(0xFF38BDF8), Color(0xFF0284C7)),
            center = Offset(cx + wingW * 0.5f, cy),
            radius = wingW
        ),
        topLeft = Offset(cx, cy - wingH * 0.7f),
        size = androidx.compose.ui.geometry.Size(wingW, wingH)
    )

    // Slender Body & Antennae
    drawOval(
        color = Color(0xFF0F172A),
        topLeft = Offset(cx - 0.8.dp.toPx(), cy - 3.5.dp.toPx()),
        size = androidx.compose.ui.geometry.Size(1.6.dp.toPx(), 7.dp.toPx())
    )
}

/**
 * Expanding Light Blue Watercolor Drop Ripple Effect behind Active / Selected Tab
 */
@Composable
fun WaterRippleTabBackground(
    isSelected: Boolean,
    rippleColor: Color,
    modifier: Modifier = Modifier
) {
    if (!isSelected) return

    val infiniteTransition = rememberInfiniteTransition(label = "water_ripple_tab")

    val wave1Radius by infiniteTransition.animateFloat(
        initialValue = 0.15f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(4500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "w1_rad"
    )
    val wave1Alpha by infiniteTransition.animateFloat(
        initialValue = 0.60f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(4500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "w1_alpha"
    )

    val wave2Radius by infiniteTransition.animateFloat(
        initialValue = 0.15f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(4500, delayMillis = 2250, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "w2_rad"
    )
    val wave2Alpha by infiniteTransition.animateFloat(
        initialValue = 0.60f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(4500, delayMillis = 2250, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "w2_alpha"
    )

    val lightBlueWaterPrimary = Color(0xFF38BDF8)
    val lightBlueWaterSoft = Color(0xFFBAE6FD)

    Canvas(modifier = modifier.fillMaxSize()) {
        val maxRadius = size.maxDimension / 1.5f

        // Expanding Outer Light Blue Watercolor Ripple Ring 1
        drawCircle(
            color = lightBlueWaterPrimary.copy(alpha = wave1Alpha * 0.55f),
            radius = maxRadius * wave1Radius,
            center = center,
            style = Stroke(width = 2.dp.toPx())
        )
        // Inner Light Blue Water Drop Cushion 1
        drawCircle(
            color = lightBlueWaterSoft.copy(alpha = wave1Alpha * 0.25f),
            radius = maxRadius * wave1Radius * 0.75f,
            center = center
        )

        // Expanding Outer Light Blue Watercolor Ripple Ring 2
        drawCircle(
            color = lightBlueWaterPrimary.copy(alpha = wave2Alpha * 0.55f),
            radius = maxRadius * wave2Radius,
            center = center,
            style = Stroke(width = 1.5.dp.toPx())
        )
        // Inner Light Blue Water Drop Cushion 2
        drawCircle(
            color = lightBlueWaterSoft.copy(alpha = wave2Alpha * 0.20f),
            radius = maxRadius * wave2Radius * 0.75f,
            center = center
        )
    }
}

/**
 * Animated Light Blue Watercolor Wave Ripple Line separating navigation tabs
 */
@Composable
fun AnimatedMulticoloredWaveLine(
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "wave_line_transition")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(9500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave_phase"
    )

    val colorShift by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(16000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "color_shift"
    )

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(8.dp)
    ) {
        val width = size.width
        val height = size.height
        val centerY = height / 2f

        val path = Path()
        var x = 0f
        val step = 3f
        var first = true

        val wavelength = width / 4.5f
        val amplitude = 2.5f

        while (x <= width) {
            val y = centerY + kotlin.math.sin(((x / wavelength) * 2 * Math.PI + phase).toDouble()).toFloat() * amplitude
            if (first) {
                path.moveTo(x, y)
                first = false
            } else {
                path.lineTo(x, y)
            }
            x += step
        }

        val lightBlueWaterPalette = listOf(
            Color(0xFFE0F2FE), // Sky Water
            Color(0xFFBAE6FD), // Soft Light Blue
            Color(0xFF7DD3FC), // Aquamarine Water
            Color(0xFF38BDF8), // Vibrant Water Blue
            Color(0xFF0284C7), // Deep Ocean Blue
            Color(0xFF0EA5E9), // Cerulean Water
            Color(0xFF7DD3FC), // Aquamarine Water
            Color(0xFFBAE6FD)  // Soft Light Blue
        )

        val brush = Brush.horizontalGradient(
            colors = lightBlueWaterPalette,
            startX = width * colorShift - width,
            endX = width * colorShift + width
        )

        drawPath(
            path = path,
            brush = brush,
            style = Stroke(
                width = 2.dp.toPx(),
                cap = androidx.compose.ui.graphics.StrokeCap.Round
            )
        )
    }
}

/**
 * Mobile Bottom Navigation Bar with Light Grey Background,
 * Light Blue Watercolor Overlay, and Dynamic Icon Click Animations.
 */
@Composable
fun LedgerBinderBottomBar(
    currentSection: LedgerSection,
    onSectionSelected: (LedgerSection) -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "tab_pulse_transition")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.50f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    val lightGreyTabBg = Color.Transparent

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
        color = lightGreyTabBg,
        border = BorderStroke(1.dp, Color(0xFFE2E8F0).copy(alpha = 0.2f)),
        shadowElevation = 0.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(lightGreyTabBg)
        ) {
            // Light Blue Watercolor Wave Flow above Light Grey Base
            TabBarWaterBackground(modifier = Modifier.matchParentSize())

            val rows = remember { listOf(NAV_ITEMS) }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                // Beautiful Garden with Soil Layer directly above the first row of tabs
                FloatingSailingBoats(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(32.dp)
                        .padding(bottom = 2.dp)
                )

                rows.forEachIndexed { rowIndex, rowItems ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        rowItems.forEach { item ->
                            val isSelected = currentSection == item.section
                            val icon = if (isSelected) item.selectedIcon else item.unselectedIcon

                            // Animated Icon Click Physics
                            val animatedScale by animateFloatAsState(
                                targetValue = if (isSelected) 1.25f else 0.90f,
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessVeryLow
                                ),
                                label = "tab_scale"
                            )

                            val animatedRotation by animateFloatAsState(
                                targetValue = if (isSelected) 360f else 0f,
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioNoBouncy,
                                    stiffness = Spring.StiffnessVeryLow
                                ),
                                label = "tab_rotation"
                            )

                            val animatedOffsetY by animateFloatAsState(
                                targetValue = if (isSelected) -4f else 0f,
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioNoBouncy,
                                    stiffness = Spring.StiffnessVeryLow
                                ),
                                label = "tab_offset_y"
                            )

                            val tint by animateColorAsState(
                                targetValue = if (isSelected) Color(0xFF0284C7) else Color(0xFF64748B),
                                animationSpec = tween(durationMillis = 400),
                                label = "tab_tint"
                            )

                            val pillBg by animateColorAsState(
                                targetValue = if (isSelected) Color(0xFFE0F2FE).copy(alpha = 0.85f) else Color.Transparent,
                                animationSpec = tween(durationMillis = 400),
                                label = "tab_bg"
                            )

                            val borderColor by animateColorAsState(
                                targetValue = if (isSelected) Color(0xFF38BDF8).copy(alpha = pulseAlpha) else Color.Transparent,
                                animationSpec = tween(durationMillis = 400),
                                label = "tab_border"
                            )

                            val barWidthFraction by animateFloatAsState(
                                targetValue = if (isSelected) 0.60f else 0f,
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioNoBouncy,
                                    stiffness = Spring.StiffnessVeryLow
                                ),
                                label = "bar_width"
                            )

                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(42.dp)
                                    .padding(horizontal = 2.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(pillBg)
                                    .border(
                                        width = if (isSelected) 1.5.dp else 0.dp,
                                        color = borderColor,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null
                                    ) { onSectionSelected(item.section) }
                            ) {
                                // Light Blue Watercolor Ripple drop rings on selected tab
                                WaterRippleTabBackground(
                                    isSelected = isSelected,
                                    rippleColor = Color(0xFF38BDF8),
                                    modifier = Modifier.matchParentSize()
                                )

                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center,
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = item.label,
                                        tint = tint,
                                        modifier = Modifier
                                            .size(22.dp)
                                            .offset(y = animatedOffsetY.dp)
                                            .scale(animatedScale)
                                            .rotate(animatedRotation)
                                    )

                                    if (barWidthFraction > 0.05f) {
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth(barWidthFraction)
                                                .height(3.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFF0284C7).copy(alpha = pulseAlpha))
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Tablet & Desktop Adaptive Navigation Rail with Light Grey Background and Light Blue Watercolor styling.
 */
@Composable
fun LedgerBinderNavRail(
    currentSection: LedgerSection,
    onSectionSelected: (LedgerSection) -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "rail_pulse_transition")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.45f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "rail_pulse_alpha"
    )

    val lightGreyTabBg = Color.Transparent

    Surface(
        modifier = modifier
            .width(76.dp)
            .fillMaxHeight(),
        color = lightGreyTabBg,
        border = BorderStroke(1.dp, Color(0xFFE2E8F0).copy(alpha = 0.2f)),
        shadowElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Logo
            AppHeaderLogo(size = 36.dp)

            Spacer(modifier = Modifier.height(16.dp))

            // Navigation Items
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                NAV_ITEMS.forEach { item ->
                    val isSelected = currentSection == item.section
                    val icon = if (isSelected) item.selectedIcon else item.unselectedIcon

                    val animatedScale by animateFloatAsState(
                        targetValue = if (isSelected) 1.25f else 0.90f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioHighBouncy,
                            stiffness = Spring.StiffnessMedium
                        ),
                        label = "rail_scale"
                    )

                    val animatedRotation by animateFloatAsState(
                        targetValue = if (isSelected) 360f else 0f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        ),
                        label = "rail_rotation"
                    )

                    val tint by animateColorAsState(
                        targetValue = if (isSelected) Color(0xFF0284C7) else Color(0xFF64748B),
                        animationSpec = tween(durationMillis = 250),
                        label = "rail_tint"
                    )

                    val pillBg by animateColorAsState(
                        targetValue = if (isSelected) Color(0xFFE0F2FE).copy(alpha = 0.85f) else Color.Transparent,
                        animationSpec = tween(durationMillis = 250),
                        label = "rail_bg"
                    )

                    val borderColor by animateColorAsState(
                        targetValue = if (isSelected) Color(0xFF38BDF8).copy(alpha = pulseAlpha) else Color.Transparent,
                        animationSpec = tween(durationMillis = 250),
                        label = "rail_border"
                    )

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(pillBg)
                            .border(
                                width = if (isSelected) 1.5.dp else 0.dp,
                                color = borderColor,
                                shape = RoundedCornerShape(14.dp)
                            )
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { onSectionSelected(item.section) }
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = item.label,
                                tint = tint,
                                modifier = Modifier
                                    .size(24.dp)
                                    .scale(animatedScale)
                                    .rotate(animatedRotation)
                            )
                        }
                    }
                }
            }
        }
    }
}

