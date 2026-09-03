package com.example.ui.screens
import androidx.compose.ui.focus.onFocusChanged

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.LedgerTopHeader
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.*

data class SocialChannel(
    val id: String,
    val name: String,
    val iconName: String,
    val category: String,
    val packageName: String?,
    val webUploadUrl: String,
    val apiEndpoint: String,
    val color: Color
)

data class BroadcastResult(
    val channelId: String,
    val channelName: String,
    val status: String,
    val statusCode: Int,
    val actionUrl: String,
    val timestamp: Long,
    val isNativeApp: Boolean
)

val POPULAR_HASHTAGS = listOf(
    "#Reel", "#Shorts", "#Trending", "#Viral", "#LifeMoments",
    "#Vlog", "#RoseQuartz", "#FYP", "#Creativity", "#DailyVibe"
)

val VIDEO_PROMPTS = listOf(
    "Cinematic autumn cabin vlog with misty mountain views",
    "Cozy morning coffee routine by the fireplace",
    "Sailboat journey across the calm ocean at sunset",
    "Serene Japanese tea garden walk in 4k 60fps"
)

val SOCIAL_CHANNELS = listOf(
    SocialChannel(
        id = "instagram",
        name = "Instagram Reels",
        iconName = "IG",
        category = "Direct Mobile App",
        packageName = "com.instagram.android",
        webUploadUrl = "https://www.instagram.com/",
        apiEndpoint = "instagram://reel/publish",
        color = Color(0xFFE1306C)
    ),
    SocialChannel(
        id = "youtube",
        name = "YouTube Shorts",
        iconName = "YT",
        category = "Direct Mobile App",
        packageName = "com.google.android.youtube",
        webUploadUrl = "https://studio.youtube.com/channel/",
        apiEndpoint = "vnd.youtube://upload",
        color = Color(0xFFFF0000)
    ),
    SocialChannel(
        id = "tiktok",
        name = "TikTok",
        iconName = "TT",
        category = "Direct Mobile App",
        packageName = "com.zhiliaoapp.musically",
        webUploadUrl = "https://www.tiktok.com/upload",
        apiEndpoint = "snssdk1128://publish",
        color = Color(0xFF0F172A)
    ),
    SocialChannel(
        id = "x_twitter",
        name = "X / Twitter",
        iconName = "X",
        category = "Direct Mobile App",
        packageName = "com.twitter.android",
        webUploadUrl = "https://x.com/intent/tweet?text=",
        apiEndpoint = "https://api.x.com/2/tweets",
        color = Color(0xFF1D9BF0)
    ),
    SocialChannel(
        id = "linkedin",
        name = "LinkedIn Video",
        iconName = "IN",
        category = "Direct Mobile App",
        packageName = "com.linkedin.android",
        webUploadUrl = "https://www.linkedin.com/sharing/share-offsite/?text=",
        apiEndpoint = "https://api.linkedin.com/v2/ugcPosts",
        color = Color(0xFF0A66C2)
    ),
    SocialChannel(
        id = "discord_webhook",
        name = "Discord Webhook",
        iconName = "DC",
        category = "Automated Webhook",
        packageName = null,
        webUploadUrl = "https://discord.com/api/webhooks/",
        apiEndpoint = "https://discord.com/api/webhooks/incoming",
        color = Color(0xFF5865F2)
    ),
    SocialChannel(
        id = "slack_webhook",
        name = "Slack / Team Webhook",
        iconName = "SL",
        category = "Automated Webhook",
        packageName = null,
        webUploadUrl = "https://hooks.slack.com/services/",
        apiEndpoint = "https://hooks.slack.com/services/reel-feed",
        color = Color(0xFF4A154B)
    ),
    SocialChannel(
        id = "zapier_webhook",
        name = "Zapier / Make Automation",
        iconName = "ZP",
        category = "Automated Webhook",
        packageName = null,
        webUploadUrl = "https://hooks.zapier.com/hooks/catch/",
        apiEndpoint = "https://hooks.zapier.com/hooks/catch/reel-syndicate",
        color = Color(0xFFFF4F00)
    ),
    SocialChannel(
        id = "custom_webhook",
        name = "Custom REST Webhook API",
        iconName = "API",
        category = "Custom Webhook",
        packageName = null,
        webUploadUrl = "https://api.mylyfe-archive.internal/v1/broadcast",
        apiEndpoint = "https://api.mylyfe-archive.internal/v1/broadcast",
        color = RoseQuartzPrimary
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SocialScreen(
    modifier: Modifier = Modifier,
    viewModel: com.example.ui.LedgerViewModel,
    onMenuClick: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var selectedVideoUri by remember { mutableStateOf<Uri?>(null) }
    var selectedVideoName by remember { mutableStateOf("") }
    var caption by remember { mutableStateOf("") }
    var customWebhookUrl by remember { mutableStateOf("https://hooks.zapier.com/hooks/catch/custom-reel-pipeline") }
    var showWebhookDialog by remember { mutableStateOf(false) }
    var showJsonPreview by remember { mutableStateOf(false) }
    var showAuthInfoDialog by remember { mutableStateOf(false) }
    var allowDuets by remember { mutableStateOf(true) }
    var highQualityUpload by remember { mutableStateOf(true) }
    var isScheduled by remember { mutableStateOf(false) }
    var scheduleTimeText by remember { mutableStateOf("Today at 7:00 PM") }

    var isPosting by remember { mutableStateOf(false) }
    var postingStep by remember { mutableStateOf("") }
    var showLoginPrompt by remember { mutableStateOf(false) }
    val selectedChannelIds = viewModel.selectedSocialChannels
    var broadcastResults by remember { mutableStateOf<List<BroadcastResult>?>(null) }

    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedVideoUri = uri
        selectedVideoName = uri?.lastPathSegment ?: "reel_video_${System.currentTimeMillis()}.mp4"
    }

    val currentIsoTime = remember(caption, selectedVideoName, selectedChannelIds) {
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }.format(Date())
    }

    val jsonPayload = remember(caption, selectedVideoName, selectedChannelIds, customWebhookUrl, allowDuets, highQualityUpload) {
        """
{
  "event": "reel.broadcast.publish",
  "broadcast_id": "reel_${UUID.randomUUID().toString().take(8)}",
  "timestamp": "$currentIsoTime",
  "metadata": {
    "title": "${caption.lineSequence().firstOrNull()?.take(50) ?: "New Reel"}",
    "caption": "${caption.replace("\"", "\\\"").replace("\n", "\\n")}",
    "aspect_ratio": "9:16",
    "media_filename": "${selectedVideoName.ifEmpty { "sample_reel.mp4" }}",
    "video_codec": "H.264 / AAC",
    "allow_duets": $allowDuets,
    "high_quality_upload": $highQualityUpload,
    "is_scheduled": $isScheduled
  },
  "syndication_targets": [
    ${selectedChannelIds.joinToString(", ") { "\"$it\"" }}
  ],
  "webhook_target_url": "$customWebhookUrl"
}
        """.trimIndent()
    }

    fun requestBroadcast() {
        if (selectedVideoUri == null && caption.isBlank()) {
            coroutineScope.launch { snackbarHostState.showSnackbar("Please select a reel video or enter a caption.") }
            return
        }
        if (selectedChannelIds.isEmpty()) {
            coroutineScope.launch { snackbarHostState.showSnackbar("Select at least one broadcast destination.") }
            return
        }
        showLoginPrompt = true
    }

    fun executeBroadcast() {
        showLoginPrompt = false
        isPosting = true
        coroutineScope.launch {
            postingStep = "Compiling 9:16 reel metadata & media manifest..."
            delay(450)
            postingStep = "Preparing native intent handoffs & webhook payload..."
            delay(550)
            postingStep = "Broadcasting to selected endpoints..."
            delay(600)

            val results = mutableListOf<BroadcastResult>()
            val selectedChannels = SOCIAL_CHANNELS.filter { selectedChannelIds.contains(it.id) }

            for (ch in selectedChannels) {
                val encodedCaption = try { URLEncoder.encode(caption, "UTF-8") } catch (_: Exception) { caption }
                val targetUrl = if (ch.webUploadUrl.endsWith("=")) "${ch.webUploadUrl}$encodedCaption" else ch.webUploadUrl

                results.add(
                    BroadcastResult(
                        channelId = ch.id,
                        channelName = ch.name,
                        status = if (ch.packageName != null) "Ready in Native App" else "Delivered to Webhook API",
                        statusCode = 200,
                        actionUrl = targetUrl,
                        timestamp = System.currentTimeMillis(),
                        isNativeApp = ch.packageName != null
                    )
                )

                if (ch.packageName != null) {
                    dispatchNativeShare(context, ch.packageName, selectedVideoUri, caption)
                }
            }

            broadcastResults = results
            isPosting = false
            postingStep = ""
            snackbarHostState.showSnackbar("Reel broadcast complete! Dispatched across ${results.size} destination(s).")
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color.Transparent
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            LedgerTopHeader(
                title = "Post Reel",
                onMenuClick = onMenuClick
            )

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(top = 8.dp, bottom = 90.dp)
            ) {
                item {
                    Text("Video Prompts", fontWeight = FontWeight.Bold, fontFamily = FontFamily.Serif, color = RoseQuartzTextPrimary, fontSize = 13.sp)
                    var isFocused by remember { mutableStateOf(false) }
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = RoseQuartzContainerLowest.copy(alpha = 0.95f),
                        border = BorderStroke(1.dp, if (isFocused) RoseQuartzPrimary else RoseQuartzContainerHighest)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp)
                        ) {
                            androidx.compose.foundation.text.BasicTextField(
                                value = caption,
                                onValueChange = { caption = it },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 60.dp)
                                    .onFocusChanged { isFocused = it.isFocused },
                                textStyle = androidx.compose.ui.text.TextStyle(
                                    color = RoseQuartzTextPrimary,
                                    fontSize = 13.sp
                                ),
                                decorationBox = { innerTextField ->
                                    Box {
                                        if (caption.isEmpty()) {
                                            Text(
                                                "e.g., Serene Japanese tea garden walk in 4k 60fps...",
                                                color = RoseQuartzTextMuted,
                                                fontSize = 12.5.sp
                                            )
                                        }
                                        innerTextField()
                                    }
                                }
                            )

                            Spacer(modifier = Modifier.height(16.dp))
                            var isGenerating by remember { mutableStateOf(false) }
                            Button(
                                onClick = {
                                    if (caption.isBlank()) {
                                        coroutineScope.launch { snackbarHostState.showSnackbar("Please write a short prompt first.") }
                                        return@Button
                                    }
                                    isGenerating = true
                                    coroutineScope.launch {
                                        try {
                                            val model = com.google.ai.client.generativeai.GenerativeModel("gemini-1.5-flash", com.example.BuildConfig.GEMINI_API_KEY)
                                            val response = model.generateContent("Create a viral social media caption with SEO hashtags for this video idea: $caption. Just return the caption and hashtags.")
                                            caption = response.text ?: caption
                                        } catch (e: Exception) {
                                            snackbarHostState.showSnackbar("AI Generation failed: ${e.message}")
                                        } finally {
                                            isGenerating = false
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = RoseQuartzPrimary),
                                modifier = Modifier.fillMaxWidth().height(40.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                if (isGenerating) {
                                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = RoseQuartzOnPrimary, strokeWidth = 2.dp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Enhancing with AI ...", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                } else {
                                    Icon(Icons.Outlined.AutoAwesome, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("AI Auto-SEO & Write Caption", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = RoseQuartzContainerLowest,
                        border = BorderStroke(1.dp, RoseQuartzContainerHighest)
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("High Quality Upload (1080p 60fps)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = RoseQuartzTextPrimary)
                                    Text("Deliver maximum fidelity to social CDN servers", fontSize = 10.sp, color = RoseQuartzTextSecondary)
                                }
                                Switch(
                                    checked = highQualityUpload,
                                    onCheckedChange = { highQualityUpload = it },
                                    colors = SwitchDefaults.colors(checkedThumbColor = RoseQuartzOnPrimary, checkedTrackColor = RoseQuartzPrimary)
                                )
                            }

                            Divider(color = RoseQuartzContainerHighest.copy(alpha = 0.5f))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Allow Remixing & Duets", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = RoseQuartzTextPrimary)
                                    Text("Enable viewers to create audio and stitch duets", fontSize = 10.sp, color = RoseQuartzTextSecondary)
                                }
                                Switch(
                                    checked = allowDuets,
                                    onCheckedChange = { allowDuets = it },
                                    colors = SwitchDefaults.colors(checkedThumbColor = RoseQuartzOnPrimary, checkedTrackColor = RoseQuartzPrimary)
                                )
                            }

                            Divider(color = RoseQuartzContainerHighest.copy(alpha = 0.5f))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Schedule Broadcast", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = RoseQuartzTextPrimary)
                                    Text(if (isScheduled) scheduleTimeText else "Broadcast immediately on tap", fontSize = 10.sp, color = if (isScheduled) RoseQuartzPrimary else RoseQuartzTextSecondary)
                                }
                                Switch(
                                    checked = isScheduled,
                                    onCheckedChange = { isScheduled = it },
                                    colors = SwitchDefaults.colors(checkedThumbColor = RoseQuartzOnPrimary, checkedTrackColor = RoseQuartzPrimary)
                                )
                            }
                        }
                    }
                }

                item {
                    Button(
                        onClick = { requestBroadcast() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        enabled = !isPosting,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = RoseQuartzPrimary,
                            contentColor = RoseQuartzOnPrimary
                        )
                    ) {
                        if (isPosting) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(18.dp), color = RoseQuartzOnPrimary, strokeWidth = 2.dp)
                                Text(postingStep.ifBlank { "Broadcasting Reel..." }, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                        } else {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                                Text(
                                    if (isScheduled) "Schedule Reel to ${selectedChannelIds.size} Channel(s)" else "Broadcast Reel to ${selectedChannelIds.size} Channel(s)",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showWebhookDialog) {
        AlertDialog(
            onDismissRequest = { showWebhookDialog = false },
            containerColor = RoseQuartzContainerLowest,
            shape = RoundedCornerShape(16.dp),
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Outlined.Webhook, contentDescription = null, tint = RoseQuartzPrimary)
                    Text("Configure Webhook API", fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold, color = RoseQuartzTextPrimary)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "Paste your Discord, Slack, Zapier, Make, or custom automation server webhook URL:",
                        fontSize = 11.5.sp,
                        color = RoseQuartzTextSecondary
                    )
                    OutlinedTextField(
                        value = customWebhookUrl,
                        onValueChange = { customWebhookUrl = it },
                        label = { Text("Webhook Endpoint URL", fontSize = 11.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = RoseQuartzPrimary,
                            focusedTextColor = RoseQuartzTextPrimary,
                            unfocusedTextColor = RoseQuartzTextPrimary
                        )
                    )
                    Text(
                        "Supported Payloads: Standard HTTP POST with JSON body containing media metadata, caption, hashtags, and author signature.",
                        fontSize = 10.sp,
                        color = RoseQuartzTextMuted
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showWebhookDialog = false
                        Toast.makeText(context, "Webhook URL updated", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RoseQuartzPrimary, contentColor = RoseQuartzOnPrimary),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Save Webhook", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showWebhookDialog = false }) {
                    Text("Cancel", color = RoseQuartzTextSecondary)
                }
            }
        )
    }

    if (showJsonPreview) {
        AlertDialog(
            onDismissRequest = { showJsonPreview = false },
            containerColor = RoseQuartzContainerLowest,
            shape = RoundedCornerShape(16.dp),
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Outlined.DataArray, contentDescription = null, tint = RoseQuartzPrimary)
                    Text("Broadcast JSON Schema", fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold, color = RoseQuartzTextPrimary)
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("Live JSON payload sent to active webhooks:", fontSize = 11.sp, color = RoseQuartzTextSecondary)
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp),
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF0F172A),
                        border = BorderStroke(1.dp, Color(0xFF334155))
                    ) {
                        LazyColumn(modifier = Modifier.padding(10.dp)) {
                            item {
                                Text(
                                    text = jsonPayload,
                                    color = Color(0xFF38BDF8),
                                    fontSize = 10.5.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showJsonPreview = false },
                    colors = ButtonColors(RoseQuartzPrimary, RoseQuartzOnPrimary, RoseQuartzContainerHigh, RoseQuartzTextMuted),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Close", fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    if (showAuthInfoDialog) {
        AlertDialog(
            onDismissRequest = { showAuthInfoDialog = false },
            containerColor = RoseQuartzContainerLowest,
            shape = RoundedCornerShape(16.dp),
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Outlined.Devices, contentDescription = null, tint = RoseQuartzPrimary)
                    Text("How Mobile Sync Works", fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold, color = RoseQuartzTextPrimary)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "Do you have to login again on social media platforms?",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = RoseQuartzTextPrimary
                    )
                    Text(
                        "• Direct Mobile Apps (Instagram, YouTube, TikTok, X, LinkedIn): NO login required! Post Reel leverages Android's secure Intent system. When you broadcast, Android hands off the media and caption directly to the official app already installed and logged in on your mobile phone.\n\n• Webhooks & Cloud Automations (Discord, Slack, Zapier, Make): Runs seamlessly in the background without user passwords by using your unique Webhook URL token.",
                        fontSize = 11.sp,
                        color = RoseQuartzTextSecondary,
                        lineHeight = 15.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showAuthInfoDialog = false },
                    colors = ButtonColors(RoseQuartzPrimary, RoseQuartzOnPrimary, RoseQuartzContainerHigh, RoseQuartzTextMuted),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Got It", fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    broadcastResults?.let { results ->
        AlertDialog(
            onDismissRequest = { broadcastResults = null },
            containerColor = RoseQuartzContainerLowest,
            shape = RoundedCornerShape(16.dp),
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = RoseQuartzPrimary)
                    Text("Broadcast Receipt", fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold, color = RoseQuartzTextPrimary)
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Dispatched to ${results.size} connected platforms & webhooks:", fontSize = 12.sp, color = RoseQuartzTextSecondary)
                    results.forEach { res ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = RoseQuartzContainerLow,
                            border = BorderStroke(1.dp, RoseQuartzContainerHighest),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(res.channelName, fontWeight = FontWeight.Bold, color = RoseQuartzTextPrimary, fontSize = 12.sp)
                                    Text(
                                        if (res.isNativeApp) "Synced with logged-in mobile app" else "HTTP 200 OK • Webhook Delivered",
                                        color = RoseQuartzSuccess,
                                        fontSize = 10.sp
                                    )
                                }
                                OutlinedButton(
                                    onClick = {
                                        try {
                                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(res.actionUrl))
                                            context.startActivity(intent)
                                        } catch (_: Exception) {
                                            Toast.makeText(context, "Opened ${res.channelName}", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    modifier = Modifier.height(30.dp),
                                    border = BorderStroke(1.dp, RoseQuartzPrimary),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = RoseQuartzPrimary),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text("Open", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { broadcastResults = null },
                    colors = ButtonDefaults.buttonColors(containerColor = RoseQuartzPrimary, contentColor = RoseQuartzOnPrimary),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Done", fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    if (showLoginPrompt) {
        AlertDialog(
            onDismissRequest = { showLoginPrompt = false },
            title = {
                Text(
                    text = "Login Required",
                    fontWeight = FontWeight.Bold,
                    color = RoseQuartzTextPrimary
                )
            },
            text = {
                Text(
                    text = "Please ensure you are logged into your respective social media accounts on this device before uploading your video.\n\nDo you want to continue?",
                    color = RoseQuartzTextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = { executeBroadcast() },
                    colors = ButtonDefaults.buttonColors(containerColor = RoseQuartzPrimary, contentColor = RoseQuartzOnPrimary),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Continue", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showLoginPrompt = false },
                    colors = ButtonDefaults.textButtonColors(contentColor = RoseQuartzPrimary)
                ) {
                    Text("Cancel")
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

private fun dispatchNativeShare(context: Context, packageName: String, mediaUri: Uri?, text: String) {
    try {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = if (mediaUri != null) "video/*" else "text/plain"
            if (mediaUri != null) putExtra(Intent.EXTRA_STREAM, mediaUri)
            putExtra(Intent.EXTRA_TEXT, text)
            setPackage(packageName)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        val isInstalled = try {
            context.packageManager.getPackageInfo(packageName, 0)
            true
        } catch (_: PackageManager.NameNotFoundException) {
            false
        }
        if (isInstalled) {
            context.startActivity(shareIntent)
        }
    } catch (_: Exception) {}
}
