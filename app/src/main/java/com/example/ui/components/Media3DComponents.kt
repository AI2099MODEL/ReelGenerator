package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MusicTrackEntity

/**
 * Ocean 3D Color Palette
 */
object Ocean3DTheme {
    val DeepOcean = Color(0xFF032541)
    val PrimaryAzure = Color(0xFF0284C7)
    val LightAzure = Color(0xFF38BDF8)
    val SkyWater = Color(0xFFE0F2FE)
    val SurfaceCard = Color(0xFFFFFFFF).copy(alpha = 0.92f)
    val GlassSurface = Color(0xFFF8FAFC).copy(alpha = 0.85f)
    val GlassBorder = Color(0xFFBAE6FD).copy(alpha = 0.45f)
    val ShadowTint = Color(0x1A0284C7)
    val GradientOcean = listOf(
        Color(0xFF0284C7),
        Color(0xFF0EA5E9),
        Color(0xFF38BDF8)
    )
    val GradientSunsetWater = listOf(
        Color(0xFF0284C7),
        Color(0xFF6366F1),
        Color(0xFF8B5CF6)
    )
}

/**
 * 3D-inspired Card with layered depth, soft shadows, rounded corners, and glowing rim highlight.
 */
@Composable
fun Media3DCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 18.dp,
    backgroundColor: Color = Ocean3DTheme.SurfaceCard,
    borderColor: Color = Ocean3DTheme.GlassBorder,
    elevation: Dp = 6.dp,
    onClick: (() -> Unit)? = null,
    testTag: String = "media_3d_card",
    content: @Composable BoxScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scaleAnim by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "card_scale"
    )

    Box(
        modifier = modifier
            .testTag(testTag)
            .scale(scaleAnim)
            .shadow(
                elevation = if (isPressed) (elevation / 2) else elevation,
                shape = RoundedCornerShape(cornerRadius),
                ambientColor = Ocean3DTheme.ShadowTint,
                spotColor = Ocean3DTheme.ShadowTint
            )
            .clip(RoundedCornerShape(cornerRadius))
            .background(backgroundColor)
            .border(
                width = 1.2.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.8f),
                        borderColor,
                        borderColor.copy(alpha = 0.2f)
                    )
                ),
                shape = RoundedCornerShape(cornerRadius)
            )
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = onClick
                    )
                } else Modifier
            ),
        content = content
    )
}

/**
 * Translucent Glass Panel with rim lighting and subtle backdrop glow.
 */
@Composable
fun GlassPanel(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 16.dp,
    tint: Color = Ocean3DTheme.GlassSurface,
    borderColor: Color = Ocean3DTheme.GlassBorder,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .shadow(4.dp, RoundedCornerShape(cornerRadius), ambientColor = Color(0x0D000000))
            .clip(RoundedCornerShape(cornerRadius))
            .background(tint)
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    listOf(
                        Color.White.copy(alpha = 0.9f),
                        borderColor,
                        Color.White.copy(alpha = 0.4f)
                    )
                ),
                shape = RoundedCornerShape(cornerRadius)
            ),
        content = content
    )
}

/**
 * 3D Depth Button with elevated pill layout and tactile feedback.
 */
@Composable
fun DepthButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    text: String,
    gradient: List<Color> = Ocean3DTheme.GradientOcean,
    enabled: Boolean = true,
    testTag: String = "depth_button"
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scaleAnim by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "btn_scale"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .testTag(testTag)
            .scale(scaleAnim)
            .shadow(
                elevation = if (isPressed) 2.dp else 6.dp,
                shape = RoundedCornerShape(14.dp),
                spotColor = gradient.first().copy(alpha = 0.4f)
            )
            .clip(RoundedCornerShape(14.dp))
            .background(
                brush = Brush.horizontalGradient(
                    if (enabled) gradient else listOf(Color(0xFFCBD5E1), Color(0xFF94A3B8))
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    listOf(Color.White.copy(alpha = 0.6f), Color.Transparent)
                ),
                shape = RoundedCornerShape(14.dp)
            )
            .clickable(
                enabled = enabled,
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }
    }
}

/**
 * Section Header with ocean title, emoji/icon, count badge, and optional action.
 */
@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    icon: ImageVector? = null,
    badgeText: String? = null,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Ocean3DTheme.SkyWater)
                        .border(1.dp, Ocean3DTheme.GlassBorder, RoundedCornerShape(10.dp))
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = Ocean3DTheme.PrimaryAzure,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
            }
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                    if (badgeText != null) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(Ocean3DTheme.PrimaryAzure.copy(alpha = 0.12f))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = badgeText,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Ocean3DTheme.PrimaryAzure
                            )
                        }
                    }
                }
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF64748B)
                    )
                }
            }
        }

        if (actionText != null && onActionClick != null) {
            TextButton(
                onClick = onActionClick,
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = actionText,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Ocean3DTheme.PrimaryAzure
                )
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = Ocean3DTheme.PrimaryAzure,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

/**
 * Filter Chip with 3D depth and selected gradient highlighting.
 */
@Composable
fun MediaTypeChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    testTag: String = "media_type_chip"
) {
    val bgBrush = if (selected) {
        Brush.horizontalGradient(Ocean3DTheme.GradientOcean)
    } else {
        Brush.horizontalGradient(listOf(Color.White, Color(0xFFF1F5F9)))
    }

    val textColor by animateColorAsState(
        targetValue = if (selected) Color.White else Color(0xFF475569),
        label = "chip_text"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .testTag(testTag)
            .shadow(
                elevation = if (selected) 4.dp else 1.dp,
                shape = RoundedCornerShape(20.dp),
                spotColor = if (selected) Ocean3DTheme.PrimaryAzure.copy(alpha = 0.35f) else Color.Transparent
            )
            .clip(RoundedCornerShape(20.dp))
            .background(bgBrush)
            .border(
                width = 1.dp,
                color = if (selected) Color.White.copy(alpha = 0.5f) else Color(0xFFE2E8F0),
                shape = RoundedCornerShape(20.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = textColor,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
                text = label,
                color = textColor,
                fontSize = 13.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
            )
        }
    }
}

/**
 * 3D Empty State with floating animated icon, informative description and action trigger.
 */
@Composable
fun EmptyMediaState(
    icon: ImageVector,
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    actionButtonText: String? = null,
    onActionClick: (() -> Unit)? = null,
    testTag: String = "empty_media_state"
) {
    val infiniteTransition = rememberInfiniteTransition(label = "empty_float")
    val offsetY by infiniteTransition.animateFloat(
        initialValue = -4f,
        targetValue = 4f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "empty_icon_offset"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .testTag(testTag)
            .fillMaxWidth()
            .padding(24.dp)
    ) {
        Media3DCard(
            modifier = Modifier.fillMaxWidth(),
            cornerRadius = 20.dp,
            backgroundColor = Color.White.copy(alpha = 0.95f),
            elevation = 4.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(72.dp)
                        .offset(y = offsetY.dp)
                        .shadow(8.dp, CircleShape, spotColor = Ocean3DTheme.PrimaryAzure.copy(alpha = 0.3f))
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0xFFE0F2FE), Color(0xFFBAE6FD))
                            )
                        )
                        .border(2.dp, Color.White, CircleShape)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = Ocean3DTheme.PrimaryAzure,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF64748B),
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )

                if (actionButtonText != null && onActionClick != null) {
                    Spacer(modifier = Modifier.height(20.dp))
                    DepthButton(
                        text = actionButtonText,
                        icon = Icons.Default.Add,
                        onClick = onActionClick,
                        testTag = "${testTag}_action"
                    )
                }
            }
        }
    }
}

/**
 * Animated 3D Equalizer Bars for music playback visualization.
 */
@Composable
fun MiniEqualizerBars(
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    barColor: Color = Ocean3DTheme.PrimaryAzure
) {
    val infiniteTransition = rememberInfiniteTransition(label = "equalizer_bars")

    val bar1Height by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(450, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "b1"
    )

    val bar2Height by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(380, delayMillis = 100, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "b2"
    )

    val bar3Height by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(520, delayMillis = 50, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "b3"
    )

    Row(
        modifier = modifier.height(18.dp),
        horizontalArrangement = Arrangement.spacedBy(2.5.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        val h1 = if (isPlaying) bar1Height else 0.25f
        val h2 = if (isPlaying) bar2Height else 0.45f
        val h3 = if (isPlaying) bar3Height else 0.30f

        Box(
            modifier = Modifier
                .width(3.dp)
                .fillMaxHeight(h1)
                .clip(CircleShape)
                .background(barColor)
        )
        Box(
            modifier = Modifier
                .width(3.dp)
                .fillMaxHeight(h3)
                .clip(CircleShape)
                .background(barColor)
        )
        Box(
            modifier = Modifier
                .width(3.dp)
                .fillMaxHeight(h2)
                .clip(CircleShape)
                .background(barColor)
        )
    }
}

/**
 * Persistent Music Mini-Player with 3D styling, wave visualizer, and quick playback controls.
 */
@Composable
fun PersistentMusicMiniPlayer(
    currentTrack: MusicTrackEntity?,
    isPlaying: Boolean,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = "persistent_music_mini_player"
) {
    AnimatedVisibility(
        visible = currentTrack != null,
        enter = slideInVertically { it } + fadeIn(),
        exit = slideOutVertically { it } + fadeOut(),
        modifier = modifier
    ) {
        if (currentTrack != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp)
                    .testTag(testTag)
            ) {
                Media3DCard(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 16.dp,
                    backgroundColor = Color.White.copy(alpha = 0.96f),
                    elevation = 8.dp,
                    onClick = onClick
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Album Art / Disc Icon with 3D depth
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(42.dp)
                                .shadow(4.dp, CircleShape, spotColor = Ocean3DTheme.PrimaryAzure.copy(alpha = 0.4f))
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(Ocean3DTheme.GradientOcean)
                                )
                                .border(1.5.dp, Color.White, CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.MusicNote,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        // Title & Artist & Equalizer
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = currentTrack.songName.ifEmpty { currentTrack.fileName },
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                MiniEqualizerBars(isPlaying = isPlaying)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = (currentTrack.artist.ifEmpty { currentTrack.albumName }).ifEmpty { "Playing Music" },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF64748B),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        // Playback Controls
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = onPrevious,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.SkipPrevious,
                                    contentDescription = "Previous Track",
                                    tint = Color(0xFF334155),
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Ocean3DTheme.PrimaryAzure)
                                    .clickable(onClick = onPlayPause)
                            ) {
                                Icon(
                                    imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                    contentDescription = if (isPlaying) "Pause" else "Play",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            IconButton(
                                onClick = onNext,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.SkipNext,
                                    contentDescription = "Next Track",
                                    tint = Color(0xFF334155),
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
