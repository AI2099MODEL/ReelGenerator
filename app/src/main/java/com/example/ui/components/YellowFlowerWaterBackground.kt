package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun YellowFlowerWaterBackground(
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "yellow_flower_water_transition")

    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 100f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 25000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ripple_time"
    )

    val rippleRadius1 by infiniteTransition.animateFloat(
        initialValue = 50f,
        targetValue = 600f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ripple1"
    )

    val rippleRadius2 by infiniteTransition.animateFloat(
        initialValue = 100f,
        targetValue = 750f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ripple2"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        // 1. Warm Light Yellow & Cream Watercolor Gradient Base
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFFFFFDE7), // Light yellow 50
                    Color(0xFFFFF9C4), // Light yellow 100
                    Color(0xFFFFF8E1)  // Cream soft amber
                )
            )
        )

        // 2. Soft Light Yellow Flower Illustration in Background (Watercolor aesthetic)
        val flowerCenterX = width * 0.75f
        val flowerCenterY = height * 0.25f

        // Flower glow / petals background
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFFFFEE58).copy(alpha = 0.35f), // Soft bright yellow
                    Color(0xFFFFE082).copy(alpha = 0.15f),
                    Color.Transparent
                ),
                center = Offset(flowerCenterX, flowerCenterY),
                radius = width * 0.5f
            ),
            center = Offset(flowerCenterX, flowerCenterY),
            radius = width * 0.5f
        )

        // Draw delicate flower petals
        val petalCount = 8
        for (i in 0 until petalCount) {
            val angle = (i * 2.0 * PI / petalCount) + (time * 0.02f)
            val px = flowerCenterX + (width * 0.18f * cos(angle)).toFloat()
            val py = flowerCenterY + (width * 0.18f * sin(angle)).toFloat()

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFFFFF59D).copy(alpha = 0.45f),
                        Color(0xFFFFEE58).copy(alpha = 0.2f),
                        Color.Transparent
                    ),
                    center = Offset(px, py),
                    radius = width * 0.22f
                ),
                center = Offset(px, py),
                radius = width * 0.22f
            )
        }

        // Flower center stamen glow
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFFFFD54F),
                    Color(0xFFFFB300).copy(alpha = 0.5f),
                    Color.Transparent
                ),
                center = Offset(flowerCenterX, flowerCenterY),
                radius = width * 0.1f
            ),
            center = Offset(flowerCenterX, flowerCenterY),
            radius = width * 0.1f
        )

        // 3. Water Ripple Effects (Concentric expanding water rings)
        val rippleCenter1 = Offset(width * 0.3f, height * 0.7f)
        val alpha1 = (1f - (rippleRadius1 / 600f)).coerceIn(0f, 1f) * 0.25f

        drawCircle(
            color = Color(0xFFFFE082).copy(alpha = alpha1),
            center = rippleCenter1,
            radius = rippleRadius1,
            style = Stroke(width = 3.dp.toPx())
        )

        val rippleCenter2 = Offset(width * 0.7f, height * 0.8f)
        val alpha2 = (1f - (rippleRadius2 / 750f)).coerceIn(0f, 1f) * 0.2f

        drawCircle(
            color = Color(0xFFFFF59D).copy(alpha = alpha2),
            center = rippleCenter2,
            radius = rippleRadius2,
            style = Stroke(width = 2.5.dp.toPx())
        )

        // Subtle ambient water shimmer wave
        val t = time * 0.1f
        val waveY = height * 0.85f
        val pathColor = Color(0xFFFFE082).copy(alpha = 0.15f)
        
        // Soft overlay for readability
        drawRect(
            color = Color(0xFFFFFDE7).copy(alpha = 0.4f)
        )
    }
}
