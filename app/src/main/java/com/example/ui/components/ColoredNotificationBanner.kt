package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

enum class NotificationType {
    SUCCESS,
    VOICE,
    ALERT,
    INFO,
    ERROR
}

data class AppNotification(
    val id: String = java.util.UUID.randomUUID().toString(),
    val title: String,
    val message: String,
    val type: NotificationType = NotificationType.SUCCESS,
    val durationMs: Long = 3800L
)

@Composable
fun ColoredNotificationBannerHost(
    notification: AppNotification?,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(notification?.id) {
        if (notification != null) {
            delay(notification.durationMs)
            onDismiss()
        }
    }

    AnimatedVisibility(
        visible = notification != null,
        enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(animationSpec = tween(300)),
        exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(animationSpec = tween(250)),
        modifier = modifier
    ) {
        notification?.let { item ->
            ColoredNotificationCard(
                notification = item,
                onDismiss = onDismiss
            )
        }
    }
}

@Composable
fun ColoredNotificationCard(
    notification: AppNotification,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (bgGradient, borderColor, icon, iconTint, badgeBg) = when (notification.type) {
        NotificationType.SUCCESS -> Tuple5(
            Brush.horizontalGradient(listOf(Color(0xFF064E3B), Color(0xFF047857), Color(0xFF059669))),
            Color(0xFF34D399),
            Icons.Filled.CheckCircle,
            Color(0xFFA7F3D0),
            Color(0xFF065F46)
        )
        NotificationType.VOICE -> Tuple5(
            Brush.horizontalGradient(listOf(Color(0xFF4C1D95), Color(0xFF6B21A8), Color(0xFF9333EA))),
            Color(0xFFE9D5FF),
            Icons.Filled.Mic,
            Color(0xFFF5D0FE),
            Color(0xFF581C87)
        )
        NotificationType.ALERT -> Tuple5(
            Brush.horizontalGradient(listOf(Color(0xFF78350F), Color(0xFFB45309), Color(0xFFD97706))),
            Color(0xFFFDE68A),
            Icons.Filled.NotificationsActive,
            Color(0xFFFEF08A),
            Color(0xFF92400E)
        )
        NotificationType.INFO -> Tuple5(
            Brush.horizontalGradient(listOf(Color(0xFF075985), Color(0xFF0284C7), Color(0xFF0369A1))),
            Color(0xFFBAE6FD),
            Icons.Filled.Info,
            Color(0xFFE0F2FE),
            Color(0xFF0C4A6E)
        )
        NotificationType.ERROR -> Tuple5(
            Brush.horizontalGradient(listOf(Color(0xFF881337), Color(0xFFBE123C), Color(0xFFE11D48))),
            Color(0xFFFECDD3),
            Icons.Filled.Error,
            Color(0xFFFFE4E6),
            Color(0xFF9F1239)
        )
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 8.dp)
            .shadow(12.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        color = Color.Transparent,
        border = BorderStroke(1.4.dp, borderColor)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(bgGradient)
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Animated Badge Icon
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(badgeBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = notification.title,
                        color = Color.White,
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = notification.message,
                        color = Color.White.copy(alpha = 0.92f),
                        fontSize = 12.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(26.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.2f))
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Dismiss",
                        tint = Color.White,
                        modifier = Modifier.size(15.dp)
                    )
                }
            }
        }
    }
}

private data class Tuple5<A, B, C, D, E>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D,
    val fifth: E
)
