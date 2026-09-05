package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.LedgerSection

/**
 * Renders the picture "app background" with round corners as the background image of all tabs.
 */
@Composable
fun TabBackgroundView(
    currentSection: LedgerSection,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(28.dp))
    ) {
        // Authentic "app background" picture used as the background image of all tabs
        Image(
            painter = painterResource(id = R.drawable.app_background),
            contentDescription = "Page App Background",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(28.dp))
        )
    }
}
