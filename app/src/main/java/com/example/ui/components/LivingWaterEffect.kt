package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.example.ui.theme.RoseQuartzBg
import com.example.ui.theme.RoseQuartzPrimary
import com.example.ui.theme.RoseQuartzSecondary
import com.example.ui.theme.RoseQuartzSteel
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.hypot
import kotlin.math.sin

data class LocalizedWaterRipplePoint(
    val id: Long,
    val center: Offset,
    val anim: Animatable<Float, AnimationVector1D>
)

/**
 * Modifier that triggers a localized ambient water ripple animation when clicked or tapped,
 * spreading from the exact point of contact across card elements to match the ambient water theme.
 */
@Composable
fun Modifier.waterRippleTouch(
    rippleColor: Color = RoseQuartzPrimary,
    enabled: Boolean = true,
    onClick: (() -> Unit)? = null
): Modifier {
    val coroutineScope = rememberCoroutineScope()
    val ripples = remember { mutableStateListOf<LocalizedWaterRipplePoint>() }

    return this
        .clipToBounds()
        .pointerInput(enabled, onClick) {
            if (enabled) {
                detectTapGestures { tapOffset ->
                    val anim = Animatable(0f)
                    val id = System.currentTimeMillis() + (0..10000).random()
                    val ripplePoint = LocalizedWaterRipplePoint(id, tapOffset, anim)
                    ripples.add(ripplePoint)

                    onClick?.invoke()

                    coroutineScope.launch {
                        anim.animateTo(
                            targetValue = 1f,
                            animationSpec = tween(durationMillis = 1400, easing = FastOutSlowInEasing)
                        )
                        ripples.remove(ripplePoint)
                    }
                }
            }
        }
        .drawWithContent {
            drawContent()
            val maxRadius = hypot(size.width, size.height) * 1.15f

            ripples.forEach { ripple ->
                val progress = ripple.anim.value
                val currentRadius = maxRadius * progress
                val alpha = (1f - progress) * 0.55f

                if (alpha > 0.005f) {
                    // Primary spreading fluid wave ring from contact point
                    drawCircle(
                        color = rippleColor.copy(alpha = alpha * 0.80f),
                        radius = currentRadius,
                        center = ripple.center,
                        style = Stroke(width = (3.dp.toPx() * (1f - progress * 0.5f)).coerceAtLeast(1f))
                    )

                    // Secondary trailing water drop wave ring
                    if (progress > 0.12f) {
                        val subProgress = (progress - 0.12f) / 0.88f
                        val subRadius = currentRadius * 0.78f
                        drawCircle(
                            color = rippleColor.copy(alpha = (1f - subProgress) * 0.45f),
                            radius = subRadius,
                            center = ripple.center,
                            style = Stroke(width = 2.dp.toPx())
                        )
                    }

                    // Ambient water drop splash cushion glow at point of contact
                    val cushionRadius = (currentRadius * 0.5f).coerceAtLeast(2f)
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                rippleColor.copy(alpha = alpha * 0.40f),
                                rippleColor.copy(alpha = alpha * 0.12f),
                                Color.Transparent
                            ),
                            center = ripple.center,
                            radius = cushionRadius
                        ),
                        radius = cushionRadius,
                        center = ripple.center
                    )
                }
            }
        }
}

/**
 * Living Water Flow Effect tailored to Stitch's Rose Quartz Archive theme.
 * Renders harmonic fluid wave ribbons and shimmering rose quartz caustics.
 */
@Composable
fun AmbientWaterFlowBackground(
    modifier: Modifier = Modifier,
    alphaMultiplier: Float = 1.0f
) {
    val infiniteTransition = rememberInfiniteTransition(label = "rose_quartz_water_transition")
    
    val phase1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 9000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase1"
    )

    val phase2 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 13000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase2"
    )

    val shimmerOffset by infiniteTransition.animateFloat(
        initialValue = -0.4f,
        targetValue = 1.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 7000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shimmer"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        // Base Luminous Warm Rose Canvas
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color.Transparent,
                    Color.Transparent,
                    Color.Transparent
                )
            )
        )

        // Wave Layer 1 — Fluid Rose Quartz Swell (#D81B60)
        drawFluidWave(
            width = width,
            height = height,
            baseY = height * 0.45f,
            amplitude = 30f,
            wavelength = width * 0.85f,
            phase = phase1,
            color = RoseQuartzPrimary.copy(alpha = 0.05f * alphaMultiplier)
        )

        // Wave Layer 2 — Warm Terracotta / Amber Ribbon (#934B19)
        drawFluidWave(
            width = width,
            height = height,
            baseY = height * 0.68f,
            amplitude = 38f,
            wavelength = width * 1.15f,
            phase = -phase2,
            color = RoseQuartzSecondary.copy(alpha = 0.04f * alphaMultiplier)
        )

        // Wave Layer 3 — Slate Steel Water Swell
        drawFluidWave(
            width = width,
            height = height,
            baseY = height * 0.84f,
            amplitude = 24f,
            wavelength = width * 0.75f,
            phase = phase1 * 0.8f + 1.4f,
            color = RoseQuartzSteel.copy(alpha = 0.035f * alphaMultiplier)
        )

        // Subtle diagonal iridescent rose caustic glow
        val shimmerX = width * shimmerOffset
        val causticRadius = (width * 0.7f).coerceAtLeast(1f)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFFFFB6C1).copy(alpha = 0.12f * alphaMultiplier),
                    Color.Transparent
                ),
                center = Offset(shimmerX, height * 0.35f),
                radius = causticRadius
            ),
            center = Offset(shimmerX, height * 0.35f),
            radius = causticRadius
        )
    }
}

private fun DrawScope.drawFluidWave(
    width: Float,
    height: Float,
    baseY: Float,
    amplitude: Float,
    wavelength: Float,
    phase: Float,
    color: Color
) {
    val path = Path()
    path.moveTo(0f, height)
    path.lineTo(0f, baseY)

    val step = 12f
    var x = 0f
    while (x <= width) {
        val relativeX = x / wavelength
        val y = baseY + amplitude * sin(relativeX * 2f * PI.toFloat() + phase)
        path.lineTo(x, y)
        x += step
    }

    path.lineTo(width, height)
    path.close()
    drawPath(path = path, color = color)
}
