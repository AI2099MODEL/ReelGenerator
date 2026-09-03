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
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun ShaderAnimation110Background(
    modifier: Modifier = Modifier,
    alphaMultiplier: Float = 0.9f
) {
    val infiniteTransition = rememberInfiniteTransition(label = "shader_110_transition")
    
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 100f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "u_time"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        
        if (width <= 0f || height <= 0f) return@Canvas

        // Base background (light glassmorphic white/neutral)
        drawRect(color = Color(0xFFFAFAFA))

        val t = time
        
        // Moving energy nodes mimicking shader noise1 and noise2
        val cx1 = width * (0.5f + 0.3f * sin(t * 0.4f) * cos(t * 0.3f))
        val cy1 = height * (0.5f + 0.3f * sin(t * 0.3f) * sin(t * 0.2f))

        val cx2 = width * (0.5f + 0.35f * cos(t * 0.2f) * sin(t * 0.5f))
        val cy2 = height * (0.5f + 0.35f * cos(t * 0.4f) * cos(t * 0.3f))

        val cx3 = width * (0.5f + 0.25f * sin(t * 0.5f - 1.0f) * cos(t * 0.3f + 1.0f))
        val cy3 = height * (0.5f + 0.25f * cos(t * 0.3f + 1.0f) * sin(t * 0.4f - 1.0f))

        // Vibrant Spectrum Tones from shader
        val blue = Color(0f, 0.33f, 1.0f)
        val gold = Color(1.0f, 0.84f, 0.0f)
        val emerald = Color(0.31f, 0.78f, 0.47f)
        val crimson = Color(0.85f, 0.11f, 0.38f)

        // Draw Blue & Gold field
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    blue.copy(alpha = 0.25f * alphaMultiplier),
                    gold.copy(alpha = 0.2f * alphaMultiplier),
                    Color.Transparent
                ),
                center = Offset(cx1, cy1),
                radius = width * 0.8f
            ),
            center = Offset(cx1, cy1),
            radius = width * 0.8f
        )

        // Draw Emerald field
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    emerald.copy(alpha = 0.22f * alphaMultiplier),
                    Color.Transparent
                ),
                center = Offset(cx2, cy2),
                radius = width * 0.75f
            ),
            center = Offset(cx2, cy2),
            radius = width * 0.75f
        )

        // Draw Crimson energy accent
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    crimson.copy(alpha = 0.2f * alphaMultiplier),
                    Color.Transparent
                ),
                center = Offset(cx3, cy3),
                radius = width * 0.7f
            ),
            center = Offset(cx3, cy3),
            radius = width * 0.7f
        )

        // Glassmorphic lightening overlay (0.85 mix simulation)
        drawRect(
            color = Color.White.copy(alpha = 0.78f)
        )
    }
}
