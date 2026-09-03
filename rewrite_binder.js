const fs = require('fs');

const content = `package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.alpha
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import kotlin.math.sin

enum class LedgerSection(val tabLabel: String) {
    CHAT("Chat"),
    DIARY("Diary"),
    EVENTS("Events"),
    VAULT("Vault"),
    TASKS("Tasks"),
    SOCIAL("Social")
}

@Composable
fun LedgerBinderBottomBar(
    currentSection: LedgerSection,
    onSectionSelected: (LedgerSection) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
        color = Color.Transparent,
        border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            NavWaterFlowBackground()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                LedgerSection.values().forEach { section ->
                    val isSelected = currentSection == section
                    BinderTabItem(
                        section = section,
                        isSelected = isSelected,
                        onClick = { onSectionSelected(section) }
                    )
                }
            }
        }
    }
}

@Composable
private fun BinderTabItem(
    section: LedgerSection,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sectionColor = getSectionColor(section)
    val bgColor by animateColorAsState(
        targetValue = if (isSelected) sectionColor.copy(alpha = 0.15f) else Color.Transparent,
        animationSpec = tween(durationMillis = 200),
        label = "bgColor"
    )
    val textColor by animateColorAsState(
        targetValue = if (isSelected) sectionColor else Gray500,
        animationSpec = tween(durationMillis = 200),
        label = "textColor"
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .testTag("nav_tab_\${section.name.lowercase()}"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = getSectionIcon(section, isSelected),
                contentDescription = section.tabLabel,
                tint = textColor,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = section.tabLabel,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                color = textColor
            )
        }
    }
}

@Composable
fun LedgerBinderNavRail(
    currentSection: LedgerSection,
    onSectionSelected: (LedgerSection) -> Unit,
    modifier: Modifier = Modifier
) {
    val visible = remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        visible.value = true
    }

    Surface(
        modifier = modifier
            .fillMaxHeight()
            .width(88.dp),
        color = Color.Transparent,
        border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder)
    ) {
        Box(modifier = Modifier.fillMaxHeight()) {
            NavWaterFlowBackground()
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 24.dp, horizontal = 8.dp)
                    .statusBarsPadding()
                    .navigationBarsPadding(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF2E434C).copy(alpha = 0.1f))
                        .border(1.dp, Color(0xFF2E434C).copy(alpha = 0.2f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "L",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Serif,
                        color = Color(0xFF2E434C)
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
                LedgerSection.values().forEachIndexed { index, section ->
                    val isSelected = currentSection == section
                    
                    val animatedOffsetX by animateDpAsState(
                        targetValue = if (visible.value) 0.dp else (-20).dp,
                        animationSpec = tween(durationMillis = 400, delayMillis = index * 100),
                        label = "offsetX"
                    )
                    val animatedAlpha by animateFloatAsState(
                        targetValue = if (visible.value) 1f else 0f,
                        animationSpec = tween(durationMillis = 400, delayMillis = index * 100),
                        label = "alpha"
                    )

                    Surface(
                        modifier = Modifier
                            .offset(x = animatedOffsetX)
                            .alpha(animatedAlpha)
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { onSectionSelected(section) },
                        color = if (isSelected) getSectionColor(section).copy(alpha = 0.15f) else Color.Transparent,
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 12.dp, horizontal = 4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = getSectionIcon(section, isSelected),
                                contentDescription = section.tabLabel,
                                tint = if (isSelected) getSectionColor(section) else Gray500,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = section.tabLabel,
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                                color = if (isSelected) getSectionColor(section) else Gray500
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun getSectionColor(section: LedgerSection): Color {
    return when (section) {
        LedgerSection.CHAT -> NavChatBlue
        LedgerSection.DIARY -> NavDiaryGold
        LedgerSection.EVENTS -> NavEventsEmerald
        LedgerSection.VAULT -> NavVaultCrimson
        LedgerSection.TASKS -> NavTasksViolet
        LedgerSection.SOCIAL -> NavSocialOrange
    }
}

private fun getSectionIcon(section: LedgerSection, isSelected: Boolean): ImageVector {
    return when (section) {
        LedgerSection.CHAT -> if (isSelected) Icons.Filled.ChatBubble else Icons.Outlined.ChatBubbleOutline
        LedgerSection.DIARY -> if (isSelected) Icons.AutoMirrored.Filled.MenuBook else Icons.AutoMirrored.Outlined.MenuBook
        LedgerSection.EVENTS -> if (isSelected) Icons.Filled.CalendarMonth else Icons.Outlined.CalendarMonth
        LedgerSection.VAULT -> if (isSelected) Icons.Filled.FolderOpen else Icons.Outlined.FolderOpen
        LedgerSection.TASKS -> if (isSelected) Icons.Filled.CheckCircle else Icons.Outlined.CheckCircle
        LedgerSection.SOCIAL -> if (isSelected) Icons.Filled.Share else Icons.Outlined.Share
    }
}

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
                colors = listOf(Color(0xFF4FC3F7).copy(alpha = 0.2f), Color.Transparent),
                center = c1,
                radius = w * 0.8f
            ),
            center = c1,
            radius = w * 0.8f
        )
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFF81D4FA).copy(alpha = 0.2f), Color.Transparent),
                center = c2,
                radius = w * 0.9f
            ),
            center = c2,
            radius = w * 0.9f
        )
    }
}
`;
fs.writeFileSync('app/src/main/java/com/example/ui/components/BinderNavigation.kt', content);
