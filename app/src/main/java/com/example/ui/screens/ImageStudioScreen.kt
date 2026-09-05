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
                        Toast.makeText(context, "Image uploaded for editing & changes! ✨", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Failed to load image: ${e.message}", Toast.LENGTH_SHORT).show()
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
            Toast.makeText(context, "Photo captured! Ready for AI styling & changes. ✨", Toast.LENGTH_SHORT).show()
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
                            Toast.makeText(context, "Photo captured! Ready for AI styling & changes. ✨", Toast.LENGTH_SHORT).show()
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
                Toast.makeText(context, "Unable to launch camera: ${e2.message}", Toast.LENGTH_SHORT).show()
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
            Toast.makeText(context, "Camera permission needed to take pictures", Toast.LENGTH_SHORT).show()
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

    // Google Drive share/backup intent
    fun uploadItemToGoogleDrive(item: GalleryMediaItem) {
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
                    putExtra(Intent.EXTRA_TITLE, item.title)
                    putExtra(Intent.EXTRA_SUBJECT, "Google Drive Backup: ${item.title}")
                    setPackage("com.google.android.apps.docs")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }

                if (shareIntent.resolveActivity(context.packageManager) != null) {
                    context.startActivity(shareIntent)
                } else {
                    val universalIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "image/jpeg"
                        putExtra(Intent.EXTRA_STREAM, contentUri)
                        putExtra(Intent.EXTRA_SUBJECT, "Backup to Google Drive: ${item.title}")
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    context.startActivity(Intent.createChooser(universalIntent, "Save / Backup to Google Drive"))
                }
            } else {
                Toast.makeText(context, "Local file not found for upload", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Opening Google Drive share: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    // Universal share image
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
            Toast.makeText(context, "Cannot share image: ${e.message}", Toast.LENGTH_SHORT).show()
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
                Toast.makeText(context, "Image deleted", Toast.LENGTH_SHORT).show()
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
                .padding(paddingValues)
        ) {
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
                            Toast.makeText(context, "Prompt enriched with AI ✨", Toast.LENGTH_SHORT).show()
                        } catch (e: Exception) {
                            Toast.makeText(context, "AI Helper: ${e.message}", Toast.LENGTH_SHORT).show()
                        } finally {
                            isEnhancingPrompt = false
                        }
                    }
                },
                isGenerating = isGenerating,
                latestResult = latestResult,
                onGenerate = {
                    if (promptInput.isBlank() && referenceImageBitmap == null) {
                        Toast.makeText(context, "Please enter a prompt or attach an image", Toast.LENGTH_SHORT).show()
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
                            Toast.makeText(context, "Artwork generated and saved to gallery!", Toast.LENGTH_SHORT).show()
                        } catch (e: Exception) {
                            errorMessage = e.message ?: "Failed to generate image"
                        } finally {
                            isGenerating = false
                        }
                    }
                },
                onSaveToDrive = { res ->
                    uploadItemToGoogleDrive(
                        GalleryMediaItem(
                            id = res.localFilePath,
                            title = res.prompt.take(25),
                            source = GallerySourceType.AI_GENERATED,
                            localFilePath = res.localFilePath
                        )
                    )
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
            onUploadToDrive = { uploadItemToGoogleDrive(mediaItem) },
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
    onSaveToDrive: (ImageGenAi.GenerationResult) -> Unit,
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
        // Golden Mobile Phone Mockup Device
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 440.dp)
                    .padding(vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                // Outer Phone Chassis with Metallic Golden Bezel
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(
                            elevation = 20.dp,
                            shape = RoundedCornerShape(40.dp),
                            ambientColor = GoldAccent.copy(alpha = 0.5f),
                            spotColor = GoldPrimary.copy(alpha = 0.6f)
                        ),
                    shape = RoundedCornerShape(40.dp),
                    color = Color(0xFF0F0F14),
                    border = BorderStroke(6.dp, MetallicGoldBrush)
                ) {
                    // Inner Screen Bezel & Wallpaper Screen
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp)
                            .clip(RoundedCornerShape(34.dp))
                            .background(SunsetPhoneWallpaperGradient)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Top Speaker / Dynamic Island Notch
                            // Top subtle decorative spacer
                            Spacer(modifier = Modifier.height(4.dp))

                            Spacer(modifier = Modifier.height(10.dp))

                            // Studio Content Card inside the Mobile Screen with Golden Border
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(20.dp),
                                color = Color(0x77110F18),
                                border = BorderStroke(1.5.dp, GoldAccent.copy(alpha = 0.7f)),
                                shadowElevation = 8.dp
                            ) {
                                Column(
                                    modifier = Modifier.padding(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    // Header: Renamed as "Enter Prompt" as requested
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(28.dp)
                                                    .clip(CircleShape)
                                                    .background(GoldPrimary.copy(alpha = 0.2f))
                                                    .border(1.dp, GoldPrimary.copy(alpha = 0.6f), CircleShape),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text("✍️", fontSize = 14.sp)
                                            }
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "Enter Prompt",
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = GoldPrimary
                                            )
                                        }

                                        // AI Prompt Writer Helper Button
                                        Surface(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .clickable(enabled = !isEnhancingPrompt) { onAiEnhancePrompt() },
                                            shape = RoundedCornerShape(8.dp),
                                            color = GoldDeep.copy(alpha = 0.85f),
                                            border = BorderStroke(1.dp, GoldAccent.copy(alpha = 0.7f))
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                if (isEnhancingPrompt) {
                                                    CircularProgressIndicator(
                                                        modifier = Modifier.size(11.dp),
                                                        color = GoldHighlight,
                                                        strokeWidth = 1.5.dp
                                                    )
                                                    Text(
                                                        text = "AI Thinking...",
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = GoldHighlight
                                                    )
                                                } else {
                                                    Text("✨", fontSize = 11.sp)
                                                    Text(
                                                        text = "AI Prompt Help",
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = GoldHighlight
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    // Prompt Input with Golden Border and bright visible white/gold text
                                    OutlinedTextField(
                                        value = promptInput,
                                        onValueChange = onPromptChange,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .heightIn(min = 80.dp)
                                            .testTag("ai_image_prompt_input"),
                                        textStyle = TextStyle(
                                            color = Color.White,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Medium
                                        ),
                                        placeholder = {
                                            Text(
                                                "Describe anything you want to create or change in your photo...",
                                                color = GoldLight.copy(alpha = 0.65f),
                                                fontSize = 12.sp,
                                                lineHeight = 16.sp
                                            )
                                        },
                                        trailingIcon = {
                                            if (promptInput.isNotEmpty()) {
                                                IconButton(onClick = { onPromptChange("") }) {
                                                    Icon(Icons.Default.Clear, contentDescription = "Clear", tint = GoldLight)
                                                }
                                            }
                                        },
                                        shape = RoundedCornerShape(14.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White,
                                            focusedBorderColor = GoldHighlight,
                                            unfocusedBorderColor = GoldAccent.copy(alpha = 0.7f),
                                            focusedContainerColor = Color(0x66000000),
                                            unfocusedContainerColor = Color(0x44000000),
                                            cursorColor = GoldHighlight,
                                            focusedPlaceholderColor = GoldLight.copy(alpha = 0.6f),
                                            unfocusedPlaceholderColor = GoldLight.copy(alpha = 0.5f)
                                        )
                                    )

                                    // Upload Image or Camera Integration Row
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        OutlinedButton(
                                            onClick = onLaunchCamera,
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(10.dp),
                                            border = BorderStroke(1.dp, GoldAccent.copy(alpha = 0.7f)),
                                            colors = ButtonDefaults.outlinedButtonColors(
                                                containerColor = Color(0x22D4AF37),
                                                contentColor = GoldLight
                                            ),
                                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp)
                                        ) {
                                            Icon(Icons.Default.PhotoCamera, contentDescription = "Camera", modifier = Modifier.size(15.dp), tint = GoldLight)
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Camera", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }

                                        OutlinedButton(
                                            onClick = onLaunchUpload,
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(10.dp),
                                            border = BorderStroke(1.dp, GoldAccent.copy(alpha = 0.7f)),
                                            colors = ButtonDefaults.outlinedButtonColors(
                                                containerColor = Color(0x22D4AF37),
                                                contentColor = GoldLight
                                            ),
                                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp)
                                        ) {
                                            Icon(Icons.Default.UploadFile, contentDescription = "Upload", modifier = Modifier.size(15.dp), tint = GoldLight)
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Upload Image", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }

                                    // Reference Image Preview (if attached)
                                    referenceImageBitmap?.let { bitmap ->
                                        Surface(
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(12.dp),
                                            color = Color(0x55000000),
                                            border = BorderStroke(1.dp, GoldHighlight.copy(alpha = 0.6f))
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(6.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Image(
                                                    bitmap = bitmap.asImageBitmap(),
                                                    contentDescription = "Reference Photo",
                                                    modifier = Modifier
                                                        .size(44.dp)
                                                        .clip(RoundedCornerShape(8.dp)),
                                                    contentScale = ContentScale.Crop
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = "📷 Image Attached for Changes",
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = GoldHighlight
                                                    )
                                                    Text(
                                                        text = "Style & prompt will be applied to this image",
                                                        fontSize = 10.sp,
                                                        color = GoldLight.copy(alpha = 0.8f)
                                                    )
                                                }
                                                IconButton(
                                                    onClick = onRemoveReferenceImage,
                                                    modifier = Modifier.size(28.dp)
                                                ) {
                                                    Icon(
                                                        Icons.Default.Close,
                                                        contentDescription = "Remove Reference",
                                                        tint = Color(0xFFFF8A80),
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    // Visual Style Selector in Golden Words
                                    Text(
                                        text = "🎨 Visual Style",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = GoldLight
                                    )

                                    Row(
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        STYLE_PRESETS.forEach { (styleName, emoji) ->
                                            val isSelected = selectedStyle == styleName
                                            Surface(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .clickable { onStyleSelect(styleName) },
                                                shape = RoundedCornerShape(8.dp),
                                                color = if (isSelected) GoldAccent else Color(0x44000000),
                                                border = BorderStroke(
                                                    1.dp,
                                                    if (isSelected) GoldHighlight else GoldAccent.copy(alpha = 0.4f)
                                                )
                                            ) {
                                                Box(
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(text = emoji, fontSize = 14.sp)
                                                }
                                            }
                                        }
                                    }

                                    // Aspect Ratio Selector in Golden Words
                                    Text(
                                        text = "📐 Aspect Ratio",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = GoldLight
                                    )

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        ASPECT_RATIOS.forEach { (ratio, _) ->
                                            val isSelected = selectedAspectRatio == ratio
                                            Surface(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .clip(RoundedCornerShape(10.dp))
                                                    .clickable { onAspectRatioSelect(ratio) },
                                                shape = RoundedCornerShape(10.dp),
                                                color = if (isSelected) GoldAccent.copy(alpha = 0.35f) else Color(0x33000000),
                                                border = BorderStroke(
                                                    1.dp,
                                                    if (isSelected) GoldPrimary else GoldAccent.copy(alpha = 0.4f)
                                                )
                                            ) {
                                                Column(
                                                    modifier = Modifier.padding(vertical = 6.dp),
                                                    horizontalAlignment = Alignment.CenterHorizontally
                                                ) {
                                                    Text(
                                                        text = ratio,
                                                        fontSize = 11.sp,
                                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                        color = if (isSelected) GoldPrimary else GoldLight.copy(alpha = 0.8f)
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))

                                    // Golden Shimmering Generate / Transform Button
                                    Button(
                                        onClick = onGenerate,
                                        enabled = !isGenerating,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(48.dp)
                                            .shadow(8.dp, RoundedCornerShape(14.dp), ambientColor = GoldPrimary, spotColor = GoldAccent)
                                            .testTag("ai_generate_image_button"),
                                        shape = RoundedCornerShape(14.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = GoldPrimary,
                                            contentColor = Color(0xFF241400),
                                            disabledContainerColor = GoldDark.copy(alpha = 0.5f)
                                        )
                                    ) {
                                        if (isGenerating) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(20.dp),
                                                color = Color(0xFF241400),
                                                strokeWidth = 2.dp
                                            )
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Text(
                                                if (referenceImageBitmap != null) "Transforming Image..." else "Generating Artwork...",
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF241400)
                                            )
                                        } else {
                                            Icon(
                                                if (referenceImageBitmap != null) Icons.Default.Transform else Icons.Default.AutoAwesome,
                                                contentDescription = null,
                                                modifier = Modifier.size(18.dp),
                                                tint = Color(0xFF241400)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                if (referenceImageBitmap != null) "Apply AI Changes" else "Generate Image",
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF241400)
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))
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

                            Surface(
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .padding(8.dp),
                                shape = RoundedCornerShape(8.dp),
                                color = Color.Black.copy(alpha = 0.75f),
                                border = BorderStroke(0.5.dp, GoldAccent.copy(alpha = 0.4f))
                            ) {
                                Text(
                                    text = res.prompt,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    fontSize = 11.sp,
                                    color = GoldHighlight,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
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

                            Button(
                                onClick = { onSaveToDrive(res) },
                                modifier = Modifier.weight(1.3f),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E88E5))
                            ) {
                                Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Google Drive", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
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
    onUploadToDrive: () -> Unit,
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
                            onClick = onUploadToDrive,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF1E88E5))
                        ) {
                            Icon(Icons.Default.CloudUpload, contentDescription = "Google Drive", tint = Color.White)
                        }

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

                // Bottom Details Panel
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF1A1A1A).copy(alpha = 0.9f),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = item.title,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        if (!item.prompt.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Prompt: \"${item.prompt}\"",
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.8f),
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Studio Creation",
                                fontSize = 11.sp,
                                color = GoldPrimary
                            )
                            Text(
                                text = "${item.displaySize} • ${SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(item.dateAddedMs))}",
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
            }
        }
    }
}
