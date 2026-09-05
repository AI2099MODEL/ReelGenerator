package com.example.ui.components

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.cos
import kotlin.math.sin

/**
 * Animated Inner Glow & Shimmer Light Sweep Modifier.
 * Sweeps a subtle translucent rose-gold light highlight diagonally across cards/buttons to draw attention.
 */
@Composable
fun Modifier.roseQuartzShimmerLightSweep(
    durationMillis: Int = 3200,
    shimmerColor: Color = Color.White.copy(alpha = 0.45f),
    innerGlowColor: Color = RoseQuartzPrimary.copy(alpha = 0.12f)
): Modifier {
    val infiniteTransition = rememberInfiniteTransition(label = "LightSweep")
    val shimmerTranslate by infiniteTransition.animateFloat(
        initialValue = -300f,
        targetValue = 1200f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ShimmerTranslate"
    )

    return this.drawWithContent {
        drawContent()

        // Subdued inner glow highlight around center/edges
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(innerGlowColor, Color.Transparent),
                center = Offset(size.width / 2, size.height / 2),
                radius = size.maxDimension.coerceAtLeast(1f)
            )
        )

        // Light sweep animated diagonal beam
        val width = size.width
        val shimmerWidth = 200f
        if (shimmerTranslate in -shimmerWidth..(width + shimmerWidth)) {
            drawRect(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.Transparent,
                        shimmerColor.copy(alpha = 0.15f),
                        shimmerColor,
                        shimmerColor.copy(alpha = 0.15f),
                        Color.Transparent
                    ),
                    start = Offset(shimmerTranslate - shimmerWidth, 0f),
                    end = Offset(shimmerTranslate, size.height)
                )
            )
        }
    }
}

/**
 * Custom 3D Card Elevation & Perspective Transform Modifier consistent with Rose Quartz design.
 */
fun Modifier.roseQuartz3dCardEffect(
    elevation: Dp = 5.dp,
    rotationX: Float = -1.2f,
    rotationY: Float = 0.8f,
    shape: Shape = RoundedCornerShape(18.dp)
): Modifier = this
    .shadow(
        elevation = elevation,
        shape = shape,
        clip = false,
        ambientColor = Color(0x266B21A8), // Soft Violet Ambient Shadow
        spotColor = Color(0x3D934B19)    // Deep Rose Quartz Spot Shadow
    )
    .graphicsLayer {
        this.rotationX = rotationX
        this.rotationY = rotationY
        this.cameraDistance = 16f * density
        this.shape = shape
        this.clip = false
        this.shadowElevation = elevation.toPx()
        this.spotShadowColor = Color(0x3D934B19)
        this.ambientShadowColor = Color(0x266B21A8)
    }

/**
 * 3D Layered Extruded Heading Text with Rose Quartz shadow depth.
 */
@Composable
fun RoseQuartz3dHeadingText(
    text: String,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 19.sp,
    fontFamily: FontFamily = FontFamily.Serif,
    fontWeight: FontWeight = FontWeight.Bold,
    color: Color = RoseQuartzTextPrimary,
    letterSpacing: TextUnit = 0.3.sp,
    maxLines: Int = 1
) {
    Box(modifier = modifier) {
        // 3D Extrusion Shadow Layer
        Text(
            text = text,
            fontFamily = fontFamily,
            fontWeight = fontWeight,
            fontSize = fontSize,
            color = RoseQuartzPrimary.copy(alpha = 0.30f),
            letterSpacing = letterSpacing,
            maxLines = maxLines,
            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
            modifier = Modifier.offset(x = 1.2.dp, y = 1.5.dp)
        )
        // Main Heading Text Surface Layer
        Text(
            text = text,
            fontFamily = fontFamily,
            fontWeight = fontWeight,
            fontSize = fontSize,
            color = color,
            letterSpacing = letterSpacing,
            maxLines = maxLines,
            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
        )
    }
}

/**
 * App Logo icon component featuring square shaped prominent app_icon.
 */
@Composable
fun AppHeaderLogo(
    modifier: Modifier = Modifier,
    size: Dp = 40.dp
) {
    Surface(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(8.dp)),
        shape = RoundedCornerShape(8.dp),
        color = Color.White,
        shadowElevation = 3.dp,
        border = BorderStroke(1.2.dp, Color(0xFFBAE6FD))
    ) {
        Image(
            painter = painterResource(id = R.drawable.app_icon),
            contentDescription = "App Logo",
            modifier = Modifier
                .fillMaxSize()
                .padding(1.dp)
                .clip(RoundedCornerShape(7.dp)),
            contentScale = androidx.compose.ui.layout.ContentScale.Crop
        )
    }
}

/**
 * Animated Light Blue Sky Background with Floating Light Blue & White Clouds for Headings.
 */
@Composable
fun HeaderSkyCloudsBackground(
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "HeaderSkyClouds")

    // Cloud 1: Large fluffy blue & white cloud drifting across the sky
    val cloud1Progress by infiniteTransition.animateFloat(
        initialValue = -0.3f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(26000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "cloud1"
    )

    // Cloud 2: Medium airy cloud drifting (higher elevation)
    val cloud2Progress by infiniteTransition.animateFloat(
        initialValue = -0.35f,
        targetValue = 1.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(34000, delayMillis = 4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "cloud2"
    )

    // Cloud 3: Soft small puffy cloud drifting (lower elevation)
    val cloud3Progress by infiniteTransition.animateFloat(
        initialValue = -0.25f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(22000, delayMillis = 11000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "cloud3"
    )

    // Cloud 4: Distant wide drifting cloud
    val cloud4Progress by infiniteTransition.animateFloat(
        initialValue = -0.3f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(42000, delayMillis = 18000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "cloud4"
    )

    // Gentle sunbeam / sky glimmer shimmer cycle
    val skyGlimmer by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "skyGlimmer"
    )

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .clipToBounds()
    ) {
        val w = size.width
        val h = size.height

        // 1. Light Blue Sky Gradient with subtle soft warmth
        val skyGradient = Brush.verticalGradient(
            colors = listOf(
                Color(0xFFBAE6FD).copy(alpha = 0.55f), // Soft sky blue 200
                Color(0xFFE0F2FE).copy(alpha = 0.45f), // Light sky blue 100
                Color(0xFFF0F9FF).copy(alpha = 0.25f), // Soft sky 50
                Color.Transparent
            ),
            startY = 0f,
            endY = h
        )
        drawRect(brush = skyGradient)

        // 2. Soft horizontal sunbeam mist in the sky
        val mistBrush = Brush.linearGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.22f * skyGlimmer),
                Color(0xFFBAE6FD).copy(alpha = 0.12f),
                Color.White.copy(alpha = 0.18f * (1f - skyGlimmer))
            ),
            start = Offset(0f, h * 0.3f),
            end = Offset(w, h * 0.7f)
        )
        drawRect(brush = mistBrush)

        // 3. Draw Floating Animated Blue & White Clouds
        // Cloud 1: Main fluffy cloud
        val c1X = w * cloud1Progress
        val c1Y = h * 0.35f + sin((cloud1Progress * 4 * Math.PI).toDouble()).toFloat() * (h * 0.08f)
        drawPuffyCloud(
            centerX = c1X,
            centerY = c1Y,
            scaleWidth = 64.dp.toPx(),
            scaleHeight = 24.dp.toPx(),
            alpha = 0.92f
        )

        // Cloud 2: Medium high airy cloud
        val c2X = w * cloud2Progress
        val c2Y = h * 0.22f + cos((cloud2Progress * 3 * Math.PI).toDouble()).toFloat() * (h * 0.06f)
        drawPuffyCloud(
            centerX = c2X,
            centerY = c2Y,
            scaleWidth = 50.dp.toPx(),
            scaleHeight = 18.dp.toPx(),
            alpha = 0.85f
        )

        // Cloud 3: Small swift lower cloud
        val c3X = w * cloud3Progress
        val c3Y = h * 0.65f + sin((cloud3Progress * 5 * Math.PI).toDouble()).toFloat() * (h * 0.07f)
        drawPuffyCloud(
            centerX = c3X,
            centerY = c3Y,
            scaleWidth = 40.dp.toPx(),
            scaleHeight = 15.dp.toPx(),
            alpha = 0.88f
        )

        // Cloud 4: Distant wide drifting cloud
        val c4X = w * cloud4Progress
        val c4Y = h * 0.45f + cos((cloud4Progress * 2.5 * Math.PI).toDouble()).toFloat() * (h * 0.05f)
        drawPuffyCloud(
            centerX = c4X,
            centerY = c4Y,
            scaleWidth = 76.dp.toPx(),
            scaleHeight = 28.dp.toPx(),
            alpha = 0.75f
        )
    }
}

/**
 * Draws a multi-lobed blue and white floating cloud with soft layered shading.
 */
private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawPuffyCloud(
    centerX: Float,
    centerY: Float,
    scaleWidth: Float,
    scaleHeight: Float,
    alpha: Float
) {
    val cloudWhite = Color.White.copy(alpha = alpha.coerceIn(0f, 1f))
    val cloudBlueShade = Color(0xFF7DD3FC).copy(alpha = (alpha * 0.75f).coerceIn(0f, 1f))
    val cloudSkyAccent = Color(0xFF38BDF8).copy(alpha = (alpha * 0.45f).coerceIn(0f, 1f))
    val softBaseBlue = Color(0xFFBAE6FD).copy(alpha = (alpha * 0.60f).coerceIn(0f, 1f))

    // 1. Soft blue base shadow puff
    drawOval(
        color = softBaseBlue,
        topLeft = Offset(centerX - scaleWidth * 0.48f, centerY - scaleHeight * 0.10f),
        size = androidx.compose.ui.geometry.Size(scaleWidth * 0.96f, scaleHeight * 0.80f)
    )

    // 2. Light blue lower puff body
    drawOval(
        color = cloudBlueShade,
        topLeft = Offset(centerX - scaleWidth * 0.46f, centerY - scaleHeight * 0.22f),
        size = androidx.compose.ui.geometry.Size(scaleWidth * 0.92f, scaleHeight * 0.65f)
    )

    // 3. Crisp white main cloud body
    drawOval(
        color = cloudWhite,
        topLeft = Offset(centerX - scaleWidth * 0.44f, centerY - scaleHeight * 0.30f),
        size = androidx.compose.ui.geometry.Size(scaleWidth * 0.88f, scaleHeight * 0.60f)
    )

    // 4. Left blue & white puff lobe
    drawCircle(
        color = cloudSkyAccent,
        radius = scaleHeight * 0.46f,
        center = Offset(centerX - scaleWidth * 0.25f, centerY - scaleHeight * 0.08f)
    )
    drawCircle(
        color = cloudWhite,
        radius = scaleHeight * 0.42f,
        center = Offset(centerX - scaleWidth * 0.25f, centerY - scaleHeight * 0.14f)
    )

    // 5. Center-top tallest pure white puff lobe with blue bottom rim
    drawCircle(
        color = cloudBlueShade,
        radius = scaleHeight * 0.58f,
        center = Offset(centerX - scaleWidth * 0.04f, centerY - scaleHeight * 0.18f)
    )
    drawCircle(
        color = cloudWhite,
        radius = scaleHeight * 0.54f,
        center = Offset(centerX - scaleWidth * 0.04f, centerY - scaleHeight * 0.26f)
    )

    // 6. Right-top puff lobe
    drawCircle(
        color = cloudSkyAccent,
        radius = scaleHeight * 0.44f,
        center = Offset(centerX + scaleWidth * 0.24f, centerY - scaleHeight * 0.06f)
    )
    drawCircle(
        color = cloudWhite,
        radius = scaleHeight * 0.40f,
        center = Offset(centerX + scaleWidth * 0.24f, centerY - scaleHeight * 0.12f)
    )

    // 7. Extra mini puff on far right
    drawCircle(
        color = cloudWhite,
        radius = scaleHeight * 0.32f,
        center = Offset(centerX + scaleWidth * 0.36f, centerY + scaleHeight * 0.02f)
    )
}

/**
 * Top header banner matching ChitChat header styling, font, and animated sky & clouds background.
 */
@Composable
fun LedgerTopHeader(
    title: String,
    modifier: Modifier = Modifier,
    actionIcon: ImageVector? = null,
    onActionClick: (() -> Unit)? = null,
    onHomeClick: (() -> Unit)? = null,
    onMenuClick: (() -> Unit)? = null,
    globalSettings: com.example.ui.GlobalSettingsState? = null,
    onOpenGlobalSettings: (() -> Unit)? = null,
    trailingContent: (@Composable () -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp))
        ) {
            // Animated Floating Sky & Clouds Background across all top headings
            HeaderSkyCloudsBackground(
                modifier = Modifier.matchParentSize()
            )

            // Frosted translucent surface overlay to ensure text contrast while showing drifting clouds
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(Color.White.copy(alpha = 0.28f))
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left side: App Logo and Title
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // App Logo placed directly before heading title
                    AppHeaderLogo(size = 28.dp)

                    // Title with ChitChat Typography and Style
                    Text(
                        text = title,
                        fontSize = 18.sp,
                        fontFamily = FontFamily.Cursive,
                        fontWeight = FontWeight.Bold,
                        color = RoseQuartzTextPrimary,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }

                // Right side: Action icon, trailing submenu filters, and Home Landing button
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (onHomeClick != null) {
                        IconButton(
                            onClick = onHomeClick,
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(RoseQuartzContainerLowest),
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Home,
                                contentDescription = "Back to Landing Page",
                                tint = RoseQuartzPrimary,
                                modifier = Modifier.size(17.dp)
                            )
                        }
                    }
                    if (actionIcon != null && onActionClick != null) {
                        IconButton(
                            onClick = onActionClick,
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(RoseQuartzContainerLowest),
                        ) {
                            Icon(
                                imageVector = actionIcon,
                                contentDescription = "Action",
                                tint = RoseQuartzPrimary,
                                modifier = Modifier.size(17.dp)
                            )
                        }
                    }
                    if (trailingContent != null) {
                        trailingContent()
                    }
                }
            }
        }
    }
}

/**
 * Standard card surface matching Rose Quartz glassmorphism with 3D elevation transform & localized water ripple.
 */
@Composable
fun LedgerPaperCard(
    modifier: Modifier = Modifier,
    borderColor: Color = RoseQuartzContainerHighest,
    rippleColor: Color = RoseQuartzPrimary,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .roseQuartz3dCardEffect(
                elevation = 5.dp,
                rotationX = -1.2f,
                rotationY = 0.8f,
                shape = RoundedCornerShape(18.dp)
            )
            .waterRippleTouch(rippleColor = rippleColor, onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        color = RoseQuartzContainerLowest.copy(alpha = 0.92f),
        border = BorderStroke(1.dp, borderColor),
        shadowElevation = 4.dp
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            content = content
        )
    }
}

/**
 * Priority badge indicator in Rose Quartz Archive styling.
 */
@Composable
fun LedgerPriorityBadge(
    priority: String,
    modifier: Modifier = Modifier
) {
    val (bg, textColor, label) = when (priority.uppercase()) {
        "HIGH" -> Triple(RoseQuartzErrorContainer, RoseQuartzError, "HIGH")
        "MED", "MEDIUM" -> Triple(RoseQuartzPrimaryContainer, RoseQuartzPrimary, "MED")
        else -> Triple(RoseQuartzSteelLight, RoseQuartzSteel, "LOW")
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(4.dp),
        color = bg,
        border = BorderStroke(1.dp, textColor.copy(alpha = 0.4f))
    ) {
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = textColor,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            letterSpacing = 0.5.sp
        )
    }
}

/**
 * Empty state indicator.
 */
@Composable
fun LedgerEmptyState(
    icon: ImageVector,
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onActionClick: (() -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            modifier = Modifier.size(60.dp),
            shape = CircleShape,
            color = RoseQuartzPrimaryContainer,
            border = BorderStroke(1.dp, RoseQuartzPrimary.copy(alpha = 0.3f))
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = RoseQuartzPrimary,
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = title,
            fontFamily = FontFamily.Serif,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = RoseQuartzTextPrimary
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = description,
            fontSize = 12.sp,
            color = RoseQuartzTextSecondary,
            lineHeight = 16.sp
        )

        if (actionLabel != null && onActionClick != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onActionClick,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = RoseQuartzPrimary,
                    contentColor = RoseQuartzOnPrimary
                ),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = actionLabel, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

fun showDatePicker(context: Context, initialTimestamp: Long, onDateSelected: (Long) -> Unit) {
    val cal = Calendar.getInstance().apply {
        if (initialTimestamp > 0) timeInMillis = initialTimestamp
    }
    DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            cal.set(Calendar.YEAR, year)
            cal.set(Calendar.MONTH, month)
            cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            onDateSelected(cal.timeInMillis)
        },
        cal.get(Calendar.YEAR),
        cal.get(Calendar.MONTH),
        cal.get(Calendar.DAY_OF_MONTH)
    ).show()
}

fun showTimePicker(context: Context, initialTimestamp: Long, onTimeSelected: (Long) -> Unit) {
    val cal = Calendar.getInstance().apply {
        if (initialTimestamp > 0) timeInMillis = initialTimestamp
    }
    TimePickerDialog(
        context,
        { _, hourOfDay, minute ->
            cal.set(Calendar.HOUR_OF_DAY, hourOfDay)
            cal.set(Calendar.MINUTE, minute)
            cal.set(Calendar.SECOND, 0)
            onTimeSelected(cal.timeInMillis)
        },
        cal.get(Calendar.HOUR_OF_DAY),
        cal.get(Calendar.MINUTE),
        false
    ).show()
}

/**
 * Custom Compose implementation of the "Organize Today" badge with rising sun & foliage
 */
@Composable
fun OrganizeTodayBrandBadge() {
    Surface(
        modifier = Modifier.size(62.dp),
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFFFFFBE8),
        border = BorderStroke(1.dp, Color(0xFFFFE599)),
        shadowElevation = 4.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFFFFFFFF),
                            Color(0xFFFFF9E4),
                            Color(0xFFFFEFBE)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 4.dp, vertical = 3.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Rising Sun with rays icon
                Text(
                    text = "☀️",
                    fontSize = 16.sp,
                    lineHeight = 16.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )

                // "Organize" in dark pine green
                Text(
                    text = "Organize",
                    fontSize = 8.5.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF0F3832),
                    lineHeight = 9.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )

                // "Today" in golden cursive
                Text(
                    text = "Today",
                    fontSize = 11.5.sp,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                    fontFamily = FontFamily.Cursive,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF8A531C),
                    lineHeight = 12.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }

            // Decorative green leaf accent at bottom-left corner
            Text(
                text = "🌿",
                fontSize = 10.sp,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 2.dp, bottom = 1.dp)
            )
        }
    }
}

