@file:OptIn(ExperimentalLayoutApi::class)
package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.graphicsLayer
import com.example.ui.components.LocalNotificationService
import com.example.ui.components.NotificationType

import androidx.compose.animation.core.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.example.ai.ImageGenAi
import com.example.data.model.GalleryMediaItem
import com.example.data.model.GallerySourceType
import com.example.ui.GlobalSettingsState
import com.example.ui.components.LedgerTopHeader
import com.example.ui.components.SpeechToTextButton
import com.example.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

val STYLE_PRESETS = listOf(
    "Digital Art" to "🎨",
    "Photorealistic" to "📸",
    "Anime / Manga" to "🎌",
    "Cinematic 8K" to "🎬",
    "Watercolor" to "🖌️",
    "Cyberpunk" to "⚡",
    "3D Render" to "🧸",
    "Oil Painting" to "🖼️",
    "Fantasy Concept" to "✨"
)

val ASPECT_RATIOS = listOf(
    "1:1" to "Square (1:1)",
    "16:9" to "Landscape (16:9)",
    "9:16" to "Portrait (9:16)",
    "4:3" to "Classic (4:3)",
    "3:4" to "Poster (3:4)"
)

private val GoldHighlight = Color(0xFFFFF8D6)
private val GoldLight = Color(0xFFFFE082)
private val GoldPrimary = Color(0xFFFFD700)
private val GoldAccent = Color(0xFFD4AF37)
private val GoldDark = Color(0xFFAA771C)
private val GoldDeep = Color(0xFF4A3206)

private val MetallicGoldBrush = Brush.linearGradient(
    listOf(
        Color(0xFFFFDF73),
        Color(0xFFD4AF37),
        Color(0xFFFFF5B8),
        Color(0xFFAA771C),
        Color(0xFFFFE57F),
        Color(0xFFC59B27),
        Color(0xFF8C6212)
    )
)

private val SunsetPhoneWallpaperGradient = Brush.verticalGradient(
    listOf(
        Color(0xFF131628),
        Color(0xFF1C1F38),
        Color(0xFF2E2644),
        Color(0xFF4E2D4E),
        Color(0xFF783955),
        Color(0xFFA64A4E),
        Color(0xFFCD6742),
        Color(0xFFE8914F),
        Color(0xFF382230)
    )
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ImageStudioScreen(
    onHomeClick: (() -> Unit)? = null,
    onMenuClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    globalSettings: GlobalSettingsState? = null,
    onOpenGlobalSettings: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val notificationService = LocalNotificationService.current
    val scope = rememberCoroutineScope()

    // Prompt & Generation States
    var promptInput by remember { mutableStateOf("") }
    var selectedStyle by remember { mutableStateOf("Digital Art") }
    var selectedAspectRatio by remember { mutableStateOf("1:1") }
    var isGenerating by remember { mutableStateOf(false) }
    var isEnhancingPrompt by remember { mutableStateOf(false) }
    var latestResult by remember { mutableStateOf<ImageGenAi.GenerationResult?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Reference Image from Camera or Gallery Upload
    var referenceImageUri by remember { mutableStateOf<Uri?>(null) }
    var referenceImageBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }

    // Gallery Media List (No default fake images)
    var galleryItems by remember { mutableStateOf<List<GalleryMediaItem>>(emptyList()) }
    var selectedMediaForViewer by remember { mutableStateOf<GalleryMediaItem?>(null) }

    // Read real user-generated / imported files
    fun refreshGallery() {
        scope.launch(Dispatchers.IO) {
            val list = mutableListOf<GalleryMediaItem>()
            val galleryDir = File(context.filesDir, "gallery_images")
            if (galleryDir.exists()) {
                val files = galleryDir.listFiles()?.sortedByDescending { it.lastModified() } ?: emptyList()
                for (f in files) {
                    if (f.isFile && (f.name.endsWith(".jpg", true) || f.name.endsWith(".png", true))) {
                        val isAi = f.name.startsWith("AI_", true) || f.name.startsWith("REMIX_", true)
                        val raw = f.nameWithoutExtension
                            .replace("AI_", "")
                            .replace("REMIX_", "")
                            .replace("LOCAL_", "")
                        // Remove all random numbers, timestamps, and digits completely
                        val cleanWords = raw
                            .replace(Regex("[0-9]"), "")
                            .replace("_", " ")
                            .replace(Regex("\\s+"), " ")
                            .trim()
                        val title = cleanWords.ifBlank { "Artwork" }.take(30)
                        list.add(
                            GalleryMediaItem(
                                id = f.absolutePath,
                                title = title,
                                source = if (isAi) GallerySourceType.AI_GENERATED else GallerySourceType.LOCAL_STORAGE,
                                localFilePath = f.absolutePath,
                                dateAddedMs = f.lastModified(),
                                sizeBytes = f.length(),
                                isSyncedToGoogleDrive = f.name.hashCode() % 3 == 0
                            )
                        )
                    }
                }
            }
            withContext(Dispatchers.Main) {
                galleryItems = list
            }
        }
    }

    LaunchedEffect(Unit) {
        refreshGallery()
    }

    // Photo Picker Launcher for uploading reference image
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            referenceImageUri = uri
            scope.launch(Dispatchers.IO) {
                try {
                    val bitmap = context.contentResolver.openInputStream(uri)?.use {
                        BitmapFactory.decodeStream(it)
                    }
                    withContext(Dispatchers.Main) {
                        referenceImageBitmap = bitmap
                        notificationService.show("Success", "Image uploaded for editing & changes! ✨", NotificationType.SUCCESS)
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        notificationService.show("Action Failed", "Failed to load image: ${e.message}", NotificationType.ERROR)
                    }
                }
            }
        }
    }

    var currentPhotoFile by remember { mutableStateOf<File?>(null) }

    // Direct Bitmap Camera Launcher (fallback & preview)
    val cameraPreviewLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            referenceImageBitmap = bitmap
            referenceImageUri = null
            notificationService.show("Success", "Photo captured! Ready for AI styling & changes. ✨", NotificationType.SUCCESS)
        }
    }

    // High-Res File Camera Launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success: Boolean ->
        if (success && currentPhotoFile != null && currentPhotoFile!!.exists()) {
            scope.launch(Dispatchers.IO) {
                try {
                    val bitmap = BitmapFactory.decodeFile(currentPhotoFile!!.absolutePath)
                    withContext(Dispatchers.Main) {
                        if (bitmap != null) {
                            referenceImageBitmap = bitmap
                            referenceImageUri = tempCameraUri
                            notificationService.show("Success", "Photo captured! Ready for AI styling & changes. ✨", NotificationType.SUCCESS)
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun startCameraIntent() {
        try {
            val cameraDir = File(context.cacheDir, "camera_photos").apply { mkdirs() }
            val photoFile = File(cameraDir, "CAM_${System.currentTimeMillis()}.jpg")
            currentPhotoFile = photoFile
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                photoFile
            )
            tempCameraUri = uri
            cameraLauncher.launch(uri)
        } catch (e: Exception) {
            e.printStackTrace()
            try {
                cameraPreviewLauncher.launch(null)
            } catch (e2: Exception) {
                notificationService.show("Action Failed", "Unable to launch camera: ${e2.message}", NotificationType.ERROR)
            }
        }
    }

    // Permission Launcher for CAMERA
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            startCameraIntent()
        } else {
            notificationService.show("Action Failed", "Camera permission needed to take pictures", NotificationType.ERROR)
        }
    }

    fun launchCameraCapture() {
        val permissionState = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
        if (permissionState == PackageManager.PERMISSION_GRANTED) {
            startCameraIntent()
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    fun shareImage(item: GalleryMediaItem) {
        try {
            val file = item.localFilePath?.let { File(it) }
            if (file != null && file.exists()) {
                val contentUri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "image/jpeg"
                    putExtra(Intent.EXTRA_STREAM, contentUri)
                    putExtra(Intent.EXTRA_TEXT, "Shared from Studio: ${item.title}")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(Intent.createChooser(shareIntent, "Share Image"))
            }
        } catch (e: Exception) {
            notificationService.show("Action Failed", "Cannot share image: ${e.message}", NotificationType.ERROR)
        }
    }

    // Delete image item
    fun deleteImageItem(item: GalleryMediaItem) {
        scope.launch(Dispatchers.IO) {
            item.localFilePath?.let { path ->
                val f = File(path)
                if (f.exists()) f.delete()
            }
            refreshGallery()
            withContext(Dispatchers.Main) {
                if (selectedMediaForViewer?.id == item.id) {
                    selectedMediaForViewer = null
                }
                notificationService.show("Item Removed", "Image deleted", NotificationType.ALERT)
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color.Transparent,
        topBar = {
            LedgerTopHeader(
                title = "Text to Image Studio",
                onHomeClick = onHomeClick,
                onMenuClick = null
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            // Butterfly Overlay
            val transition = rememberInfiniteTransition(label = "ButterflyFlyTransition")
            val flightProgress by transition.animateFloat(
                initialValue = 0f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 8000, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                ),
                label = "flightProgress"
            )
        
            val flutterWing by transition.animateFloat(
                initialValue = -15f,
                targetValue = 15f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 300, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "flutterWing"
            )
        
            val butterflies = remember {
                listOf(
                    Triple("🦋", 0.1f, 0.2f),
                    Triple("🦋", 0.7f, 0.4f),
                    Triple("🦋", 0.3f, 0.7f),
                    Triple("🦋", 0.85f, 0.15f)
                )
            }
        
            butterflies.forEachIndexed { index, (emoji, startX, startY) ->
                val phaseOffset = index * 0.25f
                val currentProgress = (flightProgress + phaseOffset) % 1f
        
                val offsetX = (startX * 320 + kotlin.math.sin((currentProgress * 2 * Math.PI) + index) * 50).dp
                val offsetY = (startY * 500 + kotlin.math.cos((currentProgress * 2 * Math.PI) + index) * 40 - (currentProgress * 60)).dp
        
                Text(
                    text = emoji,
                    fontSize = (20 + (index % 3) * 4).sp,
                    modifier = Modifier
                        .offset(x = offsetX, y = offsetY)
                        .graphicsLayer(
                            rotationZ = flutterWing + (if (index % 2 == 0) 10f else -10f),
                            scaleX = if (index % 2 == 0) 1f else -1f,
                            alpha = 0.85f
                        )
                )
            }

            TextToImageSection(
                promptInput = promptInput,
                onPromptChange = { promptInput = it },
                selectedStyle = selectedStyle,
                onStyleSelect = { selectedStyle = it },
                selectedAspectRatio = selectedAspectRatio,
                onAspectRatioSelect = { selectedAspectRatio = it },
                referenceImageBitmap = referenceImageBitmap,
                onRemoveReferenceImage = {
                    referenceImageBitmap = null
                    referenceImageUri = null
                },
                onLaunchCamera = { launchCameraCapture() },
                onLaunchUpload = {
                    photoPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                isEnhancingPrompt = isEnhancingPrompt,
                onAiEnhancePrompt = {
                    isEnhancingPrompt = true
                    scope.launch {
                        try {
                            val enhanced = ImageGenAi.enhancePromptWithAi(
                                userSeed = promptInput,
                                style = selectedStyle,
                                hasInputImage = referenceImageBitmap != null
                            )
                            promptInput = enhanced
                            notificationService.show("Success", "Prompt enriched with AI ✨", NotificationType.SUCCESS)
                        } catch (e: Exception) {
                            notificationService.show("Notification", "AI Helper: ${e.message}", NotificationType.INFO)
                        } finally {
                            isEnhancingPrompt = false
                        }
                    }
                },
                isGenerating = isGenerating,
                latestResult = latestResult,
                onGenerate = {
                    if (promptInput.isBlank() && referenceImageBitmap == null) {
                        notificationService.show("Attention", "Please enter a prompt or attach an image", NotificationType.ALERT)
                        return@TextToImageSection
                    }
                    isGenerating = true
                    errorMessage = null
                    scope.launch {
                        try {
                            val effectivePrompt = promptInput.ifBlank { "Reimagined artwork" }
                            val result = ImageGenAi.generateImage(
                                context = context,
                                prompt = effectivePrompt.trim(),
                                style = selectedStyle,
                                aspectRatio = selectedAspectRatio,
                                inputImageBitmap = referenceImageBitmap
                            )
                            latestResult = result
                            refreshGallery()
                            notificationService.show("Success", "Artwork generated and saved to gallery!", NotificationType.SUCCESS)
                        } catch (e: Exception) {
                            errorMessage = e.message ?: "Failed to generate image"
                        } finally {
                            isGenerating = false
                        }
                    }
                },
                onInspect = { res ->
                    selectedMediaForViewer = GalleryMediaItem(
                        id = res.localFilePath,
                        title = res.prompt,
                        source = GallerySourceType.AI_GENERATED,
                        localFilePath = res.localFilePath,
                        prompt = res.prompt,
                        style = res.style,
                        aspectRatio = res.aspectRatio
                    )
                },
                aiCreations = galleryItems.filter { it.source == GallerySourceType.AI_GENERATED },
                onSelectCreation = { item -> selectedMediaForViewer = item },
                onShareCreation = { shareImage(it) },
                onDeleteCreation = { deleteImageItem(it) }
            )
        }
    }

    // Fullscreen Image Lightbox Viewer
    selectedMediaForViewer?.let { mediaItem ->
        ImageLightboxDialog(
            item = mediaItem,
            onDismiss = { selectedMediaForViewer = null },
            onShare = { shareImage(mediaItem) },
            onDelete = { deleteImageItem(mediaItem) }
        )
    }
}

// -------------------------------------------------------------------------------------------------
// 1. TEXT TO IMAGE CREATOR SECTION - ENTER PROMPT & THREE SCORE CARDS PER ROW
// -------------------------------------------------------------------------------------------------

@Composable
private fun TextToImageSection(
    promptInput: String,
    onPromptChange: (String) -> Unit,
    selectedStyle: String,
    onStyleSelect: (String) -> Unit,
    selectedAspectRatio: String,
    onAspectRatioSelect: (String) -> Unit,
    referenceImageBitmap: Bitmap?,
    onRemoveReferenceImage: () -> Unit,
    onLaunchCamera: () -> Unit,
    onLaunchUpload: () -> Unit,
    isEnhancingPrompt: Boolean,
    onAiEnhancePrompt: () -> Unit,
    isGenerating: Boolean,
    latestResult: ImageGenAi.GenerationResult?,
    onGenerate: () -> Unit,
    onInspect: (ImageGenAi.GenerationResult) -> Unit,
    aiCreations: List<GalleryMediaItem>,
    onSelectCreation: (GalleryMediaItem) -> Unit,
    onShareCreation: (GalleryMediaItem) -> Unit,
    onDeleteCreation: (GalleryMediaItem) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 96.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Blue Colored UI Prompt & Settings Box (Matching the dialog UI box from other pages)
        item {
            val dialogBackgroundBrush = remember {
                Brush.verticalGradient(
                    listOf(
                        Color(0xFFFFFFFF),
                        Color(0xFFF8FAFC),
                        Color(0xFFEFF6FF)
                    )
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 440.dp)
                    .padding(vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(
                            elevation = 20.dp,
                            shape = RoundedCornerShape(26.dp),
                            ambientColor = Color(0xFF0284C7).copy(alpha = 0.25f),
                            spotColor = Color(0xFF0284C7).copy(alpha = 0.35f)
                        ),
                    shape = RoundedCornerShape(26.dp),
                    color = Color.White,
                    border = BorderStroke(1.5.dp, Color(0xFFBAE6FD))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(dialogBackgroundBrush)
                            .padding(horizontal = 18.dp, vertical = 18.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            // Header: Title with Cursive font & AI Prompt Writer Helper Button
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(34.dp)
                                            .clip(CircleShape)
                                            .background(
                                                Brush.linearGradient(
                                                    listOf(Color(0xFFE0F2FE), Color(0xFFBAE6FD))
                                                )
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("✨", fontSize = 16.sp)
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = "Generate Image",
                                        color = Color(0xFF0369A1),
                                        fontSize = 22.sp,
                                        fontFamily = androidx.compose.ui.text.font.FontFamily.Cursive,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                // AI Prompt Writer Helper Button
                                Surface(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .clickable(enabled = !isEnhancingPrompt) { onAiEnhancePrompt() },
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color(0xFFF0F9FF),
                                    border = BorderStroke(1.2.dp, Color(0xFFBAE6FD))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        if (isEnhancingPrompt) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(12.dp),
                                                color = Color(0xFF0284C7),
                                                strokeWidth = 1.5.dp
                                            )
                                            Text(
                                                text = "AI Thinking...",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF0284C7)
                                            )
                                        } else {
                                            Text("✍️", fontSize = 12.sp)
                                            Text(
                                                text = "AI Prompt Help",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF0369A1)
                                            )
                                        }
                                    }
                                }
                            }

                            // Prompt Input Field
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = "Prompt Description",
                                    color = Color(0xFF0369A1),
                                    fontSize = 15.sp,
                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Cursive,
                                    fontWeight = FontWeight.Bold
                                )

                                OutlinedTextField(
                                    value = promptInput,
                                    onValueChange = onPromptChange,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(min = 86.dp)
                                        .testTag("ai_image_prompt_input"),
                                    textStyle = TextStyle(
                                        color = Color(0xFF0F172A),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium
                                    ),
                                    placeholder = {
                                        Text(
                                            "Describe anything you want to create or change in your photo...",
                                            color = Color(0xFF94A3B8),
                                            fontSize = 13.sp,
                                            lineHeight = 18.sp
                                        )
                                    },
                                    trailingIcon = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            SpeechToTextButton(
                                                onResult = { recognizedText ->
                                                    onPromptChange(if (promptInput.isEmpty()) recognizedText else "$promptInput $recognizedText")
                                                },
                                                tint = Color(0xFF0284C7)
                                            )
                                            if (promptInput.isNotEmpty()) {
                                                IconButton(onClick = { onPromptChange("") }) {
                                                    Icon(Icons.Default.Clear, contentDescription = "Clear", tint = Color(0xFF64748B))
                                                }
                                            }
                                        }
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color(0xFF0F172A),
                                        unfocusedTextColor = Color(0xFF1E293B),
                                        focusedBorderColor = Color(0xFF0284C7),
                                        unfocusedBorderColor = Color(0xFFBAE6FD),
                                        focusedContainerColor = Color(0xFFF0F9FF),
                                        unfocusedContainerColor = Color(0xFFF8FAFC),
                                        cursorColor = Color(0xFF0284C7)
                                    )
                                )
                            }

                            // Upload Image or Camera Integration Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedButton(
                                    onClick = onLaunchCamera,
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(42.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.2.dp, Color(0xFFBAE6FD)),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        containerColor = Color(0xFFF0F9FF),
                                        contentColor = Color(0xFF0369A1)
                                    ),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                                ) {
                                    Icon(
                                        Icons.Default.PhotoCamera,
                                        contentDescription = "Camera",
                                        modifier = Modifier.size(16.dp),
                                        tint = Color(0xFF0284C7)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        "Camera",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF0369A1)
                                    )
                                }

                                OutlinedButton(
                                    onClick = onLaunchUpload,
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(42.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.2.dp, Color(0xFFBAE6FD)),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        containerColor = Color(0xFFF0F9FF),
                                        contentColor = Color(0xFF0369A1)
                                    ),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                                ) {
                                    Icon(
                                        Icons.Default.UploadFile,
                                        contentDescription = "Upload",
                                        modifier = Modifier.size(16.dp),
                                        tint = Color(0xFF0284C7)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        "Upload Image",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF0369A1)
                                    )
                                }
                            }

                            // Reference Image Preview (if attached)
                            referenceImageBitmap?.let { bitmap ->
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color(0xFFF0F9FF),
                                    border = BorderStroke(1.2.dp, Color(0xFFBAE6FD))
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Image(
                                            bitmap = bitmap.asImageBitmap(),
                                            contentDescription = "Reference Photo",
                                            modifier = Modifier
                                                .size(48.dp)
                                                .clip(RoundedCornerShape(8.dp)),
                                            contentScale = ContentScale.Crop
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "📷 Image Attached for Changes",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF0369A1)
                                            )
                                            Text(
                                                text = "Style & prompt will be applied to this image",
                                                fontSize = 11.sp,
                                                color = Color(0xFF64748B)
                                            )
                                        }
                                        IconButton(
                                            onClick = onRemoveReferenceImage,
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.Close,
                                                contentDescription = "Remove Reference",
                                                tint = Color(0xFFEF4444),
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            // Visual Style Selector
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = "Visual Style",
                                    color = Color(0xFF0369A1),
                                    fontSize = 15.sp,
                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Cursive,
                                    fontWeight = FontWeight.Bold
                                )

                                Row(
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    STYLE_PRESETS.forEach { (styleName, emoji) ->
                                        val isSelected = selectedStyle == styleName
                                        Surface(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(10.dp))
                                                .clickable { onStyleSelect(styleName) },
                                            shape = RoundedCornerShape(10.dp),
                                            color = if (isSelected) Color(0xFF0284C7) else Color(0xFFF0F9FF),
                                            border = BorderStroke(
                                                1.2.dp,
                                                if (isSelected) Color(0xFF0284C7) else Color(0xFFBAE6FD)
                                            )
                                        ) {
                                            Box(
                                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 6.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(text = emoji, fontSize = 15.sp)
                                            }
                                        }
                                    }
                                }
                            }

                            // Aspect Ratio Selector
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = "Aspect Ratio",
                                    color = Color(0xFF0369A1),
                                    fontSize = 15.sp,
                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Cursive,
                                    fontWeight = FontWeight.Bold
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    ASPECT_RATIOS.forEach { (ratio, _) ->
                                        val isSelected = selectedAspectRatio == ratio
                                        Surface(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(10.dp))
                                                .clickable { onAspectRatioSelect(ratio) },
                                            shape = RoundedCornerShape(10.dp),
                                            color = if (isSelected) Color(0xFF0284C7) else Color(0xFFF0F9FF),
                                            border = BorderStroke(
                                                1.2.dp,
                                                if (isSelected) Color(0xFF0284C7) else Color(0xFFBAE6FD)
                                            )
                                        ) {
                                            Column(
                                                modifier = Modifier.padding(vertical = 8.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                Text(
                                                    text = ratio,
                                                    fontSize = 12.sp,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                    color = if (isSelected) Color.White else Color(0xFF0369A1)
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(2.dp))

                            // Generate / Transform Button with Blue / Sky Theme
                            Button(
                                onClick = onGenerate,
                                enabled = !isGenerating,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .shadow(6.dp, RoundedCornerShape(14.dp), ambientColor = Color(0xFF0284C7).copy(alpha = 0.35f), spotColor = Color(0xFF0284C7).copy(alpha = 0.45f))
                                    .testTag("ai_generate_image_button"),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF0284C7),
                                    contentColor = Color.White,
                                    disabledContainerColor = Color(0xFFBAE6FD)
                                )
                            ) {
                                if (isGenerating) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        color = Color.White,
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        if (referenceImageBitmap != null) "Transforming Image..." else "Generating Artwork...",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                } else {
                                    Icon(
                                        if (referenceImageBitmap != null) Icons.Default.Transform else Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp),
                                        tint = Color.White
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        if (referenceImageBitmap != null) "Apply AI Changes" else "Generate Image",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Live Generated Result Preview Card
        latestResult?.let { res ->
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 440.dp)
                        .shadow(12.dp, RoundedCornerShape(22.dp), ambientColor = GoldAccent, spotColor = GoldPrimary),
                    shape = RoundedCornerShape(22.dp),
                    color = Color(0xFF131118),
                    border = BorderStroke(2.dp, MetallicGoldBrush)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (res.isImageToImage) "✨ Image Transformed" else "✨ Latest Creation",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = GoldPrimary
                            )

                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFF2E7D32).copy(alpha = 0.25f),
                                border = BorderStroke(0.5.dp, Color(0xFF4CAF50).copy(alpha = 0.5f))
                            ) {
                                Text(
                                    text = "Saved ✓",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF81C784)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(260.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color.Black)
                                .border(1.dp, GoldAccent.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                                .clickable { onInspect(res) }
                        ) {
                            Image(
                                bitmap = res.bitmap.asImageBitmap(),
                                contentDescription = res.prompt,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Fit
                            )

                            
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { onInspect(res) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, GoldAccent)
                            ) {
                                Icon(Icons.Default.Fullscreen, contentDescription = null, modifier = Modifier.size(16.dp), tint = GoldLight)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Zoom", fontSize = 12.sp, color = GoldLight, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Created Images shown in Three Score Cards in One Row (No "Recent Creations" title)
        if (aiCreations.isNotEmpty()) {
            val chunked = aiCreations.chunked(3)
            items(chunked) { rowItems ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 440.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    for (i in 0 until 3) {
                        if (i < rowItems.size) {
                            val item = rowItems[i]
                            ScoreCardImage(
                                item = item,
                                modifier = Modifier.weight(1f),
                                onClick = { onSelectCreation(item) },
                                onShare = { onShareCreation(item) },
                                onDelete = { onDeleteCreation(item) }
                            )
                        } else {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

/**
 * Score card style display for created images with direct Delete and Share buttons.
 */
@Composable
private fun ScoreCardImage(
    item: GalleryMediaItem,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .shadow(4.dp, RoundedCornerShape(14.dp)),
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFF16141D),
        border = BorderStroke(1.5.dp, MetallicGoldBrush)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            AsyncImage(
                model = item.localFilePath?.let { File(it) },
                contentDescription = item.title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // Bottom action bar with Delete and Share buttons directly on small thumbnail
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f))
                        )
                    )
                    .padding(horizontal = 4.dp, vertical = 3.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Share action
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF0284C7).copy(alpha = 0.9f))
                        .clickable { onShare() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.Share,
                        contentDescription = "Share",
                        tint = Color.White,
                        modifier = Modifier.size(13.dp)
                    )
                }

                // Delete action
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFEF4444).copy(alpha = 0.9f))
                        .clickable { onDelete() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.Delete,
                        contentDescription = "Delete",
                        tint = Color.White,
                        modifier = Modifier.size(13.dp)
                    )
                }
            }
        }
    }
}

// -------------------------------------------------------------------------------------------------
// FULLSCREEN IMAGE LIGHTBOX VIEWER DIALOG
// -------------------------------------------------------------------------------------------------

@Composable
private fun ImageLightboxDialog(
    item: GalleryMediaItem,
    onDismiss: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color.Black.copy(alpha = 0.95f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
            ) {
                // Top Action Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f))
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IconButton(
                            onClick = onShare,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f))
                        ) {
                            Icon(Icons.Default.Share, contentDescription = "Share", tint = Color.White)
                        }

                        IconButton(
                            onClick = {
                                onDelete()
                                onDismiss()
                            },
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFD32F2F).copy(alpha = 0.8f))
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.White)
                        }
                    }
                }

                // Fullscreen Image View
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = item.localFilePath?.let { File(it) },
                        contentDescription = item.title,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Fit
                    )
                }
            }
        }
    }
}
