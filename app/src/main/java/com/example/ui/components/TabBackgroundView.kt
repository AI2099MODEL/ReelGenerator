package com.example.ui.components

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.R
import com.example.ui.LedgerSection

/**
 * Renders a distinct, theme-tailored background image for each tab
 * with smooth crossfade transitions upon tab navigation.
 */
@Composable
fun TabBackgroundView(
    currentSection: LedgerSection,
    modifier: Modifier = Modifier
) {
    // Ambient breathing shimmer for subtle lighting movement
    val infiniteTransition = rememberInfiniteTransition(label = "ambient_glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.08f,
        targetValue = 0.22f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_alpha"
    )

    Crossfade(
        targetState = currentSection,
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        modifier = modifier.fillMaxSize(),
        label = "tab_background_crossfade"
    ) { section ->
        val bgDrawableRes = when (section) {
            LedgerSection.HOME -> R.drawable.bg_tab_home
            LedgerSection.TASKS -> R.drawable.bg_tab_tasks
            LedgerSection.EVENTS -> R.drawable.bg_tab_events
            LedgerSection.VAULT -> R.drawable.bg_tab_vault
            LedgerSection.IMAGES -> R.drawable.bg_tab_collage
        }

        val accentColor = when (section) {
            LedgerSection.HOME -> Color(0xFFF59E0B)    // Amber Gold
            LedgerSection.TASKS -> Color(0xFF38BDF8)   // Sky Cyan
            LedgerSection.EVENTS -> Color(0xFFF43F5E)  // Rose Coral
            LedgerSection.VAULT -> Color(0xFF06B6D4)   // Deep Cyan Cyber
            LedgerSection.IMAGES -> Color(0xFFEC4899)  // Creative Orchid
        }

        Box(modifier = Modifier.fillMaxSize()) {
            // Distinct Background Image for this Tab
            Image(
                painter = painterResource(id = bgDrawableRes),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Dynamic Ambient Radial Color Accent
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(glowAlpha)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(accentColor, Color.Transparent),
                            radius = 1200f
                        )
                    )
            )

            // Readability Scrim (ensures text and cards contrast cleanly)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.15f),
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.35f)
                            )
                        )
                    )
            )
        }
    }
}
