package com.example.ui.components

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.util.NetworkSimulationMode
import com.example.util.NetworkState

@Composable
fun NetworkStatusChip(
    networkState: NetworkState,
    pendingSyncCount: Int,
    onOpenNetworkDialog: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (statusColor, icon, label) = when {
        !networkState.isOnline -> Triple(
            RoseQuartzAccentAmber,
            Icons.Filled.SignalCellularOff,
            "Offline"
        )
        networkState.hasMobileSignal -> Triple(
            Color(0xFF2E7D32), // Deep emerald green
            Icons.Filled.SignalCellularAlt,
            "Mobile Signal"
        )
        networkState.connectionType == "WIFI" -> Triple(
            Color(0xFF1565C0), // Blue
            Icons.Filled.Wifi,
            "Wi-Fi"
        )
        else -> Triple(
            RoseQuartzPrimary,
            Icons.Filled.CloudDone,
            "Online"
        )
    }

    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable { onOpenNetworkDialog() },
        shape = RoundedCornerShape(20.dp),
        color = statusColor.copy(alpha = 0.12f),
        border = BorderStroke(1.dp, statusColor.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(statusColor)
            )

            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = statusColor,
                modifier = Modifier.size(12.dp)
            )

            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = statusColor
            )

            if (pendingSyncCount > 0) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = RoseQuartzAccentAmber,
                    modifier = Modifier.padding(start = 2.dp)
                ) {
                    Text(
                        text = "$pendingSyncCount ⏳",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun OfflineStatusBanner(
    networkState: NetworkState,
    pendingSyncCount: Int,
    onSyncNow: () -> Unit,
    onOpenNetworkDialog: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (!networkState.isOnline || pendingSyncCount > 0) {
        Surface(
            modifier = modifier.fillMaxWidth(),
            color = if (!networkState.isOnline) RoseQuartzAccentAmber.copy(alpha = 0.15f) else RoseQuartzPrimaryContainer.copy(alpha = 0.5f),
            border = BorderStroke(1.dp, if (!networkState.isOnline) RoseQuartzAccentAmber.copy(alpha = 0.5f) else RoseQuartzPrimary.copy(alpha = 0.3f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = if (!networkState.isOnline) Icons.Filled.CloudOff else Icons.Filled.Sync,
                        contentDescription = null,
                        tint = if (!networkState.isOnline) RoseQuartzAccentAmber else RoseQuartzPrimary,
                        modifier = Modifier.size(16.dp)
                    )

                    Column {
                        Text(
                            text = if (!networkState.isOnline) "Offline Mode Active" else "Mobile Signal Sync Ready",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = RoseQuartzTextPrimary
                        )
                        Text(
                            text = if (pendingSyncCount > 0) "$pendingSyncCount chat messages queued locally in Room DB" else "Local on-device database active",
                            fontSize = 10.sp,
                            color = RoseQuartzTextSecondary
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (pendingSyncCount > 0 && networkState.isOnline) {
                        Button(
                            onClick = onSyncNow,
                            colors = ButtonDefaults.buttonColors(containerColor = RoseQuartzPrimary),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            modifier = Modifier.height(26.dp),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text("Sync Now", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    IconButton(
                        onClick = onOpenNetworkDialog,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Tune,
                            contentDescription = "Network Settings",
                            tint = RoseQuartzPrimary,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun NetworkQueueDialog(
    networkState: NetworkState,
    pendingSyncCount: Int,
    onSyncNow: () -> Unit,
    onSetSimulationMode: (NetworkSimulationMode) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = RoseQuartzContainerLowest,
        shape = RoundedCornerShape(18.dp),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Sensors,
                    contentDescription = null,
                    tint = RoseQuartzPrimary,
                    modifier = Modifier.size(22.dp)
                )
                Text(
                    text = "Signal & Local Queue Status",
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    color = RoseQuartzTextPrimary,
                    fontSize = 17.sp
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Current Signal Status Card
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = RoseQuartzBg,
                    border = BorderStroke(1.dp, RoseQuartzContainerHighest),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Current Network:", fontSize = 11.sp, color = RoseQuartzTextMuted)
                            val statusTag = if (networkState.hasMobileSignal) "📶 Mobile Signal (5G/LTE)"
                            else if (networkState.connectionType == "WIFI") "📶 Wi-Fi"
                            else if (networkState.isOnline) "🟢 Online"
                            else "⛔ Offline"
                            Text(statusTag, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = RoseQuartzTextPrimary)
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Queued Chat Messages:", fontSize = 11.sp, color = RoseQuartzTextMuted)
                            Text(
                                text = if (pendingSyncCount > 0) "$pendingSyncCount awaiting mobile signal" else "0 (All synced)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (pendingSyncCount > 0) RoseQuartzAccentAmber else Color(0xFF2E7D32)
                            )
                        }
                    }
                }

                Text(
                    text = "Messages sent while offline are stored securely in local Room DB storage and queue up until a mobile signal is detected.",
                    fontSize = 11.sp,
                    color = RoseQuartzTextSecondary
                )

                // Simulation Mode Selector
                Text(
                    text = "Signal Simulation & Testing:",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = RoseQuartzTextPrimary
                )

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    NetworkSimulationMode.values().forEach { mode ->
                        val isSelected = networkState.simulationMode == mode
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    onSetSimulationMode(mode)
                                    Toast.makeText(context, "Switched to: ${mode.label}", Toast.LENGTH_SHORT).show()
                                },
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) RoseQuartzPrimaryContainer else RoseQuartzBg,
                            border = BorderStroke(1.dp, if (isSelected) RoseQuartzPrimary else RoseQuartzContainerHighest)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = {
                                        onSetSimulationMode(mode)
                                    },
                                    colors = RadioButtonDefaults.colors(selectedColor = RoseQuartzPrimary),
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = mode.label,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = RoseQuartzTextPrimary
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (pendingSyncCount > 0) {
                Button(
                    onClick = {
                        onSyncNow()
                        Toast.makeText(context, "Syncing queued chat messages...", Toast.LENGTH_SHORT).show()
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RoseQuartzPrimary),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Filled.Sync, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Sync $pendingSyncCount Queued Messages")
                }
            } else {
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = RoseQuartzPrimary),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Done")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = RoseQuartzTextMuted)
            }
        }
    )
}
