@file:OptIn(ExperimentalLayoutApi::class)
package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.text.font.FontFamily
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
import com.example.ui.components.roseQuartz3dCardEffect
import com.example.ui.components.waterRippleTouch
import com.example.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.*

enum class StudioTab(val label: String, val iconEmoji: String) {
    TEXT_TO_IMAGE("AI Creator", "✨"),
    LOCAL_GALLERY("Local Storage", "📱"),
    GOOGLE_DRIVE("Google Drive", "☁️")
}

val PROMPT_INSPIRATIONS = listOf(
    "💎 Ethereal crystal palace floating in sunset rose clouds",
    "🌆 Cyberpunk rainy neon street in 8k cinematic lighting",
    "🌸 Traditional Japanese tea garden with cherry blossoms in watercolor",
    "🦁 Majestic celestial lion crowned with glowing golden stars",
    "☕ Cozy vintage library cafe with potted plants and warm fireplace",
    "🚀 Futuristic astronaut discovering a bioluminescent alien forest",
    "🌊 Sacred living water vortex shining with iridescent quartz crystals"
)

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

    var selectedTab by remember { mutableStateOf(StudioTab.TEXT_TO_IMAGE) }

    // Text to Image States
    var promptInput by remember { mutableStateOf("") }
    var selectedStyle by remember { mutableStateOf("Digital Art") }
    var selectedAspectRatio by remember { mutableStateOf("1:1") }
    var isGenerating by remember { mutableStateOf(false) }
    var latestResult by remember { mutableStateOf<ImageGenAi.GenerationResult?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Gallery Media List
    var galleryItems by remember { mutableStateOf<List<GalleryMediaItem>>(emptyList()) }
    var selectedMediaForViewer by remember { mutableStateOf<GalleryMediaItem?>(null) }
    var isGridViewCompact by remember { mutableStateOf(false) }

    // Load initial storage images & seed defaults
    fun refreshGallery() {
        scope.launch(Dispatchers.IO) {
            val list = mutableListOf<GalleryMediaItem>()

            // Read internal gallery directory
            val galleryDir = File(context.filesDir, "gallery_images")
            if (galleryDir.exists()) {
                val files = galleryDir.listFiles()?.sortedByDescending { it.lastModified() } ?: emptyList()
                for (f in files) {
                    if (f.isFile && (f.name.endsWith(".jpg", true) || f.name.endsWith(".png", true))) {
                        val isAi = f.name.startsWith("AI_", true)
                        val title = f.nameWithoutExtension.replace("AI_", "").replace("LOCAL_", "").substringBefore("_")
                        list.add(
                            GalleryMediaItem(
                                id = f.absolutePath,
                                title = title.take(30),
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

            // Also read default app storage images or create seed sample if empty
            if (list.isEmpty()) {
                // Generate a lovely initial sample to welcome the user
                val sampleBitmap = ImageGenAi.generateProceduralArtwork(
                    "Sacred Living Water and Rose Quartz Crystals",
                    "Digital Art",
                    800,
                    800
                )
                val sampleDir = File(context.filesDir, "gallery_images").apply { mkdirs() }
                val sampleFile = File(sampleDir, "AI_${(100..999).random()}_sample.jpg")
                FileOutputStream(sampleFile).use { out ->
                    sampleBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
                }
                list.add(
                    GalleryMediaItem(
                        id = sampleFile.absolutePath,
                        title = "Rose Quartz Sanctuary",
                        source = GallerySourceType.AI_GENERATED,
                        localFilePath = sampleFile.absolutePath,
                        prompt = "Sacred Living Water and Rose Quartz Crystals",
                        style = "Digital Art",
                        dateAddedMs = System.currentTimeMillis(),
                        sizeBytes = sampleFile.length(),
                        isSyncedToGoogleDrive = true
                    )
                )
            }

            withContext(Dispatchers.Main) {
                galleryItems = list
            }
        }
    }

    LaunchedEffect(Unit) {
        refreshGallery()
    }

    // Photo Picker launcher for Local Storage import
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 10)
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            scope.launch(Dispatchers.IO) {
                val galleryDir = File(context.filesDir, "gallery_images").apply { mkdirs() }
                for (uri in uris) {
                    try {
                        val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
                        val targetFile = File(galleryDir, "LOCAL_${(100..999).random()}_${UUID.randomUUID().toString().take(6)}.jpg")
                        FileOutputStream(targetFile).use { outputStream ->
                            inputStream?.copyTo(outputStream)
                        }
                        inputStream?.close()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Imported ${uris.size} photos to Local Gallery!", Toast.LENGTH_SHORT).show()
                    refreshGallery()
                }
            }
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
                    setPackage("com.google.android.apps.docs") // Google Drive target
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }

                // If Google Drive package is installed, launch it directly, else fallback to universal chooser
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
                    putExtra(Intent.EXTRA_TEXT, "Shared from MyLyfe Studio: ${item.title}")
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
                onMenuClick = null,
                actionIcon = Icons.Filled.AutoAwesome,
                onActionClick = {
                    photoPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Segmented Tab Selector (AI Creator, Local Storage, Google Drive) with Golden Luxury theme
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                shape = RoundedCornerShape(16.dp),
                color = Color(0xDD121018),
                border = BorderStroke(1.dp, GoldAccent.copy(alpha = 0.6f)),
                shadowElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    StudioTab.values().forEach { tab ->
                        val isSelected = selectedTab == tab
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { selectedTab = tab },
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) GoldPrimary else Color.Transparent,
                            border = if (isSelected) BorderStroke(1.dp, GoldHighlight) else null,
                            shadowElevation = if (isSelected) 3.dp else 0.dp
                        ) {
                            Row(
                                modifier = Modifier.fillMaxSize(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = tab.iconEmoji,
                                    fontSize = 14.sp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = tab.label,
                                    color = if (isSelected) Color(0xFF241400) else GoldLight.copy(alpha = 0.8f),
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }

            // Tab Content
            AnimatedContent(
                targetState = selectedTab,
                label = "studio_tab_animation",
                modifier = Modifier.fillMaxSize()
            ) { tab ->
                when (tab) {
                    StudioTab.TEXT_TO_IMAGE -> {
                        TextToImageSection(
                            promptInput = promptInput,
                            onPromptChange = { promptInput = it },
                            selectedStyle = selectedStyle,
                            onStyleSelect = { selectedStyle = it },
                            selectedAspectRatio = selectedAspectRatio,
                            onAspectRatioSelect = { selectedAspectRatio = it },
                            isGenerating = isGenerating,
                            latestResult = latestResult,
                            onGenerate = {
                                if (promptInput.isBlank()) {
                                    Toast.makeText(context, "Please enter a prompt to generate art", Toast.LENGTH_SHORT).show()
                                    return@TextToImageSection
                                }
                                isGenerating = true
                                errorMessage = null
                                scope.launch {
                                    try {
                                        val result = ImageGenAi.generateImage(
                                            context = context,
                                            prompt = promptInput.trim(),
                                            style = selectedStyle,
                                            aspectRatio = selectedAspectRatio
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
                            recentAiCreations = galleryItems.filter { it.source == GallerySourceType.AI_GENERATED },
                            onSelectRecent = { item -> selectedMediaForViewer = item }
                        )
                    }

                    StudioTab.LOCAL_GALLERY -> {
                        LocalStorageGallerySection(
                            items = galleryItems,
                            isCompact = isGridViewCompact,
                            onToggleCompact = { isGridViewCompact = !isGridViewCompact },
                            onImportClick = {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            onItemClick = { item -> selectedMediaForViewer = item },
                            onUploadToDrive = { item -> uploadItemToGoogleDrive(item) },
                            onShare = { item -> shareImage(item) },
                            onDelete = { item -> deleteImageItem(item) }
                        )
                    }

                    StudioTab.GOOGLE_DRIVE -> {
                        GoogleDriveGallerySection(
                            items = galleryItems,
                            onOpenDriveApp = {
                                try {
                                    val driveIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://drive.google.com/drive/u/0/my-drive"))
                                    context.startActivity(driveIntent)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Opening Google Drive: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                            },
                            onUploadItem = { item -> uploadItemToGoogleDrive(item) },
                            onItemClick = { item -> selectedMediaForViewer = item },
                            onImportFromDevice = {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            }
                        )
                    }
                }
            }
        }
    }

    // Fullscreen Image Lightbox Viewer
    selectedMediaForViewer?.let { mediaItem ->
        ImageLightboxDialog(
            item = mediaItem,
            onDismiss = { selectedMediaForViewer = null },
            onShare = { shareImage(mediaItem) },
            onUploadToDrive = { uploadItemToGoogleDrive(mediaItem) },
            onDelete = {
                deleteImageItem(mediaItem)
            }
        )
    }
}

// -------------------------------------------------------------------------------------------------
// 1. TEXT TO IMAGE CREATOR SECTION - MOBILE SCREEN MOCKUP WITH GOLDEN ROUNDED FRAME
// -------------------------------------------------------------------------------------------------

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

@Composable
private fun TextToImageSection(
    promptInput: String,
    onPromptChange: (String) -> Unit,
    selectedStyle: String,
    onStyleSelect: (String) -> Unit,
    selectedAspectRatio: String,
    onAspectRatioSelect: (String) -> Unit,
    isGenerating: Boolean,
    latestResult: ImageGenAi.GenerationResult?,
    onGenerate: () -> Unit,
    onSaveToDrive: (ImageGenAi.GenerationResult) -> Unit,
    onInspect: (ImageGenAi.GenerationResult) -> Unit,
    recentAiCreations: List<GalleryMediaItem>,
    onSelectRecent: (GalleryMediaItem) -> Unit
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
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "9:41",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GoldLight
                                )

                                // Dynamic Island Pill
                                Box(
                                    modifier = Modifier
                                        .width(90.dp)
                                        .height(22.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(Color.Black)
                                        .border(1.dp, GoldAccent.copy(alpha = 0.35f), RoundedCornerShape(14.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Camera lens
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFF1A1A2E))
                                                .border(0.5.dp, GoldLight.copy(alpha = 0.4f), CircleShape)
                                        )
                                        // Speaker sensor
                                        Box(
                                            modifier = Modifier
                                                .width(18.dp)
                                                .height(3.dp)
                                                .clip(RoundedCornerShape(2.dp))
                                                .background(Color(0xFF2B2B38))
                                        )
                                    }
                                }

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Wifi,
                                        contentDescription = null,
                                        tint = GoldLight,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Icon(
                                        imageVector = Icons.Default.BatteryFull,
                                        contentDescription = null,
                                        tint = GoldLight,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            // Decorative Golden Sun Doodle & Script Header as in the snapshot
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                // Sun rays doodle
                                Row(
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(bottom = 2.dp)
                                ) {
                                    Text(
                                        text = "☀",
                                        fontSize = 24.sp,
                                        color = GoldPrimary
                                    )
                                    Text(
                                        text = "✦",
                                        fontSize = 14.sp,
                                        color = GoldLight,
                                        modifier = Modifier.padding(horizontal = 4.dp)
                                    )
                                    Text(
                                        text = "✨",
                                        fontSize = 18.sp
                                    )
                                }

                                // Golden Cursive Script Words
                                Text(
                                    text = "Organize Today",
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Cursive,
                                    color = GoldLight,
                                    textAlign = TextAlign.Center,
                                    lineHeight = 36.sp
                                )

                                Spacer(modifier = Modifier.height(2.dp))

                                Text(
                                    text = "♡",
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GoldLight,
                                    textAlign = TextAlign.Center
                                )

                                Spacer(modifier = Modifier.height(2.dp))

                                Text(
                                    text = "A MORE ORGANIZED YOU\nA BRIGHTER TOMORROW",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.8.sp,
                                    color = GoldHighlight,
                                    textAlign = TextAlign.Center,
                                    lineHeight = 14.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))

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
                                    // Studio Header in Golden font
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
                                                Text("✨", fontSize = 14.sp)
                                            }
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "Text to Image Studio",
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = GoldPrimary
                                            )
                                        }

                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = GoldDeep.copy(alpha = 0.7f),
                                            border = BorderStroke(1.dp, GoldAccent.copy(alpha = 0.5f))
                                        ) {
                                            Text(
                                                text = "AI Gen",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = GoldHighlight,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }

                                    // Prompt Input with Golden Border and warm text
                                    OutlinedTextField(
                                        value = promptInput,
                                        onValueChange = onPromptChange,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .heightIn(min = 90.dp)
                                            .testTag("ai_image_prompt_input"),
                                        placeholder = {
                                            Text(
                                                "A cozy rustic log cabin in an autumn forest with bright yellow and orange trees, misty morning, mountain background, green grass field with a wooden fence. Light and bright.",
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
                                            focusedBorderColor = GoldPrimary,
                                            unfocusedBorderColor = GoldAccent.copy(alpha = 0.6f),
                                            focusedContainerColor = Color(0x55000000),
                                            unfocusedContainerColor = Color(0x33000000),
                                            focusedTextColor = GoldHighlight,
                                            unfocusedTextColor = GoldHighlight
                                        )
                                    )

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

                                    // Golden Shimmering Generate Button
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
                                                "Generating Artwork...",
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF241400)
                                            )
                                        } else {
                                            Icon(
                                                Icons.Default.AutoAwesome,
                                                contentDescription = null,
                                                modifier = Modifier.size(18.dp),
                                                tint = Color(0xFF241400)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                "Generate Image",
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

        // Live Generated Result Preview Card (Inside Golden Border Frame)
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
                                text = "✨ Latest AI Creation",
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
                                    text = "Saved to Gallery ✓",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF81C784)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Rendered Image Display
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

                            // Quick overlay prompt badge
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

                        // Quick Actions (Inspect, Drive Backup, Share)
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

        // Recent AI Creations Section
        if (recentAiCreations.isNotEmpty()) {
            item {
                Column(
                    modifier = Modifier.fillMaxWidth().widthIn(max = 440.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "✨ Recent Creations in Studio (${recentAiCreations.size})",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = GoldLight
                    )

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(recentAiCreations) { item ->
                            Surface(
                                modifier = Modifier
                                    .size(120.dp, 140.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable { onSelectRecent(item) },
                                shape = RoundedCornerShape(14.dp),
                                color = Color(0xFF16141D),
                                border = BorderStroke(1.dp, GoldAccent.copy(alpha = 0.5f)),
                                shadowElevation = 2.dp
                            ) {
                                Column {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(95.dp)
                                            .background(Color.DarkGray)
                                    ) {
                                        AsyncImage(
                                            model = item.localFilePath?.let { File(it) },
                                            contentDescription = item.title,
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                    }
                                    Text(
                                        text = item.title,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = GoldLight,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------------------------------------------
// 2. LOCAL STORAGE GALLERY SECTION
// -------------------------------------------------------------------------------------------------

@Composable
private fun LocalStorageGallerySection(
    items: List<GalleryMediaItem>,
    isCompact: Boolean,
    onToggleCompact: () -> Unit,
    onImportClick: () -> Unit,
    onItemClick: (GalleryMediaItem) -> Unit,
    onUploadToDrive: (GalleryMediaItem) -> Unit,
    onShare: (GalleryMediaItem) -> Unit,
    onDelete: (GalleryMediaItem) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        // Gallery Header Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Device & App Gallery",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = RoseQuartzTextPrimary
                )
                Text(
                    text = "${items.size} photos in local storage",
                    fontSize = 12.sp,
                    color = RoseQuartzTextMuted
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Toggle 2-column / 3-column
                IconButton(
                    onClick = onToggleCompact,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(RoseQuartzContainerLowest)
                ) {
                    Icon(
                        imageVector = if (isCompact) Icons.Default.ViewAgenda else Icons.Default.GridView,
                        contentDescription = "Toggle Grid",
                        tint = RoseQuartzPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Import from Device Button
                Button(
                    onClick = onImportClick,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = RoseQuartzPrimary),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Import Photos", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        if (items.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("🖼️", fontSize = 48.sp)
                    Text(
                        "No photos in gallery yet",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = RoseQuartzTextPrimary
                    )
                    Text(
                        "Generate AI artworks or import photos from your device storage to view them here.",
                        fontSize = 13.sp,
                        color = RoseQuartzTextMuted,
                        textAlign = TextAlign.Center
                    )
                    Button(
                        onClick = onImportClick,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = RoseQuartzPrimary)
                    ) {
                        Text("Pick Photos from Device")
                    }
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(if (isCompact) 3 else 2),
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 96.dp)
            ) {
                items(items, key = { it.id }) { item ->
                    GalleryPhotoCard(
                        item = item,
                        isCompact = isCompact,
                        onClick = { onItemClick(item) },
                        onUploadToDrive = { onUploadToDrive(item) },
                        onShare = { onShare(item) },
                        onDelete = { onDelete(item) }
                    )
                }
            }
        }
    }
}

@Composable
private fun GalleryPhotoCard(
    item: GalleryMediaItem,
    isCompact: Boolean,
    onClick: () -> Unit,
    onUploadToDrive: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(if (isCompact) 140.dp else 220.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .roseQuartz3dCardEffect(shape = RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        color = RoseQuartzContainerLowest,
        border = BorderStroke(1.dp, RoseQuartzContainerHighest)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Image Content
            AsyncImage(
                model = item.localFilePath?.let { File(it) },
                contentDescription = item.title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // Gradient shade on bottom for legibility
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .height(if (isCompact) 45.dp else 70.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f))
                        )
                    )
            )

            // Origin Badge (AI vs Local Device)
            Surface(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(6.dp),
                shape = RoundedCornerShape(6.dp),
                color = if (item.source == GallerySourceType.AI_GENERATED) RoseQuartzPrimary.copy(alpha = 0.9f) else Color.Black.copy(alpha = 0.65f)
            ) {
                Text(
                    text = if (item.source == GallerySourceType.AI_GENERATED) "AI Art" else "Device",
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            // Sync Status / Drive Badge
            if (item.isSyncedToGoogleDrive) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp),
                    shape = CircleShape,
                    color = Color(0xFF1E88E5)
                ) {
                    Icon(
                        imageVector = Icons.Default.CloudDone,
                        contentDescription = "Synced to Drive",
                        tint = Color.White,
                        modifier = Modifier
                            .size(20.dp)
                            .padding(3.dp)
                    )
                }
            }

            // Bottom Title & Actions
            Row(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = item.title,
                    fontSize = if (isCompact) 11.sp else 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(
                        onClick = { onShare() },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share",
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    IconButton(
                        onClick = { onDelete() },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------------------------------------------
// 3. GOOGLE DRIVE GALLERY & CLOUD BACKUP SECTION
// -------------------------------------------------------------------------------------------------

@Composable
private fun GoogleDriveGallerySection(
    items: List<GalleryMediaItem>,
    onOpenDriveApp: () -> Unit,
    onUploadItem: (GalleryMediaItem) -> Unit,
    onItemClick: (GalleryMediaItem) -> Unit,
    onImportFromDevice: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 96.dp)
    ) {
        // Google Drive Status Banner
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .roseQuartz3dCardEffect(shape = RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                color = RoseQuartzContainerLowest,
                border = BorderStroke(1.dp, Color(0xFF1E88E5).copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF1E88E5).copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("☁️", fontSize = 18.sp)
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Google Drive Cloud Photos",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = RoseQuartzTextPrimary
                                )
                                Text(
                                    text = "Sync & backup artworks & local pictures",
                                    fontSize = 11.sp,
                                    color = RoseQuartzTextMuted
                                )
                            }
                        }

                        IconButton(
                            onClick = onOpenDriveApp,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF1E88E5).copy(alpha = 0.1f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.OpenInNew,
                                contentDescription = "Open Drive",
                                tint = Color(0xFF1E88E5),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = onOpenDriveApp,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E88E5))
                        ) {
                            Icon(Icons.Default.Cloud, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Open Google Drive", fontSize = 12.sp, color = Color.White)
                        }

                        OutlinedButton(
                            onClick = onImportFromDevice,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, RoseQuartzPrimary)
                        ) {
                            Icon(Icons.Default.FileUpload, contentDescription = null, modifier = Modifier.size(16.dp), tint = RoseQuartzPrimary)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Upload Photos", fontSize = 12.sp, color = RoseQuartzPrimary)
                        }
                    }
                }
            }
        }

        // List of Photos with Drive Sync Actions
        item {
            Text(
                text = "📸 Photo Sync & Cloud Backup Manager",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = RoseQuartzTextPrimary
            )
        }

        items(items, key = { "drive_${it.id}" }) { item ->
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .clickable { onItemClick(item) },
                shape = RoundedCornerShape(14.dp),
                color = RoseQuartzContainerLowest,
                border = BorderStroke(1.dp, RoseQuartzContainerHighest)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Thumbnail
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color.DarkGray)
                    ) {
                        AsyncImage(
                            model = item.localFilePath?.let { File(it) },
                            contentDescription = item.title,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = item.title,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = RoseQuartzTextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = if (item.isSyncedToGoogleDrive) Color(0xFF2E7D32).copy(alpha = 0.12f) else RoseQuartzContainerHighest
                            ) {
                                Text(
                                    text = if (item.isSyncedToGoogleDrive) "In Google Drive ✓" else "Local Storage Only",
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = if (item.isSyncedToGoogleDrive) Color(0xFF2E7D32) else RoseQuartzTextMuted
                                )
                            }
                            Text(
                                text = item.displaySize,
                                fontSize = 10.sp,
                                color = RoseQuartzTextMuted
                            )
                        }
                    }

                    // Cloud Upload Button
                    IconButton(
                        onClick = { onUploadItem(item) },
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(if (item.isSyncedToGoogleDrive) Color(0xFF1E88E5).copy(alpha = 0.15f) else RoseQuartzPrimary.copy(alpha = 0.1f))
                    ) {
                        Icon(
                            imageVector = if (item.isSyncedToGoogleDrive) Icons.Default.CloudSync else Icons.Default.CloudUpload,
                            contentDescription = "Upload to Drive",
                            tint = if (item.isSyncedToGoogleDrive) Color(0xFF1E88E5) else RoseQuartzPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------------------------------------------
// 4. FULLSCREEN IMAGE LIGHTBOX VIEWER DIALOG
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
                                text = "Source: ${if (item.source == GallerySourceType.AI_GENERATED) "Image Studio" else "Device Storage"}",
                                fontSize = 11.sp,
                                color = RoseQuartzPrimary
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
