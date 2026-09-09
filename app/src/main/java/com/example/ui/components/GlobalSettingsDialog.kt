package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.GlobalSettingsState
import com.example.ui.theme.*

data class LocationPreset(
    val name: String,
    val flag: String,
    val lat: Double,
    val lng: Double,
    val region: String
)

private val POPULAR_LOCATIONS = listOf(
    LocationPreset("San Francisco, CA", "🇺🇸", 37.7749, -122.4194, "North America"),
    LocationPreset("New York, NY", "🇺🇸", 40.7128, -74.0060, "North America"),
    LocationPreset("London, UK", "🇬🇧", 51.5074, -0.1278, "Europe"),
    LocationPreset("Paris, France", "🇫🇷", 48.8566, 2.3522, "Europe"),
    LocationPreset("Tokyo, Japan", "🇯🇵", 35.6762, 139.6503, "Asia Pacific"),
    LocationPreset("New Delhi, India", "🇮🇳", 28.6139, 77.2090, "Asia Pacific"),
    LocationPreset("Sydney, Australia", "🇦🇺", -33.8688, 151.2093, "Oceania"),
    LocationPreset("Berlin, Germany", "🇩🇪", 52.5200, 13.4050, "Europe"),
    LocationPreset("Sao Paulo, Brazil", "🇧🇷", -23.5505, -46.6333, "South America")
)

data class LanguagePreset(
    val name: String,
    val code: String,
    val flag: String,
    val nativeName: String,
    val sampleGreeting: String
)

private val SUPPORTED_LANGUAGES = listOf(
    LanguagePreset("English (US)", "en", "🇺🇸", "English", "Hello & Welcome!"),
    LanguagePreset("Spanish", "es", "🇪🇸", "Español", "¡Hola y bienvenido!"),
    LanguagePreset("French", "fr", "🇫🇷", "Français", "Bonjour et bienvenue !"),
    LanguagePreset("German", "de", "🇩🇪", "Deutsch", "Hallo und willkommen!"),
    LanguagePreset("Hindi", "hi", "🇮🇳", "हिन्दी", "नमस्ते और स्वागत है!"),
    LanguagePreset("Japanese", "ja", "🇯🇵", "日本語", "こんにちは、ようこそ！"),
    LanguagePreset("Chinese (Simplified)", "zh", "🇨🇳", "中文", "你好，欢迎！"),
    LanguagePreset("Portuguese", "pt", "🇧🇷", "Português", "Olá e bem-vindo!"),
    LanguagePreset("Italian", "it", "🇮🇹", "Italiano", "Ciao e benvenuto!"),
    LanguagePreset("Arabic", "ar", "🇦🇪", "العربية", "مرحباً وبك!"),
    LanguagePreset("Russian", "ru", "🇷🇺", "Русский", "Здравствуйте и добро пожаловать!"),
    LanguagePreset("Korean", "ko", "🇰🇷", "한국어", "안녕하세요, 환영합니다!")
)

@Composable
fun GlobalSettingsDialog(
    settings: GlobalSettingsState,
    onDismissRequest: () -> Unit,
    onToggleLocation: (Boolean) -> Unit,
    onSetLocation: (String, Double, Double, Boolean) -> Unit,
    onToggleTranslation: (Boolean) -> Unit,
    onSetLanguage: (String, String) -> Unit,
    onToggleAutoTranslate: (Boolean) -> Unit,
    onSetTranslationEngine: (String) -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var activeTab by remember { mutableStateOf(0) } // 0: Location, 1: Translation
    var customSearchQuery by remember { mutableStateOf("") }
    var testTranslationInput by remember { mutableStateOf("Welcome to Ledger Archive! Manage notes, tasks, & events.") }
    var testTranslationOutput by remember { mutableStateOf("") }

    // Auto-update sample output when input or language changes
    LaunchedEffect(testTranslationInput, settings.targetLanguageCode) {
        val targetLang = SUPPORTED_LANGUAGES.find { it.code == settings.targetLanguageCode }
        val greeting = targetLang?.sampleGreeting ?: "Translated text"
        testTranslationOutput = "[$greeting] ${testTranslationInput} (Google Translate)"
    }

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.85f)
                .clip(RoundedCornerShape(24.dp)),
            color = RoseQuartzContainerLowest,
            border = BorderStroke(1.5.dp, RoseQuartzContainerHighest),
            shadowElevation = 16.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header Title & Close Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            modifier = Modifier.size(40.dp),
                            shape = CircleShape,
                            color = RoseQuartzPrimaryContainer,
                            border = BorderStroke(1.dp, RoseQuartzPrimary.copy(alpha = 0.5f))
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Filled.Public,
                                    contentDescription = "Global Settings",
                                    tint = RoseQuartzPrimary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }

                        Column {
                            Text(
                                text = "Global App Settings",
                                fontFamily = FontFamily.Serif,
                                fontWeight = FontWeight.Bold,
                                fontSize = 19.sp,
                                color = RoseQuartzTextPrimary
                            )
                            Text(
                                text = "Google Location & Language Translation",
                                fontSize = 12.sp,
                                color = RoseQuartzTextSecondary
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismissRequest,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(RoseQuartzContainerHighest.copy(alpha = 0.5f))
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Close Settings",
                            tint = RoseQuartzTextPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Tab Switcher (Location vs Translation)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(RoseQuartzContainerHighest.copy(alpha = 0.4f))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val tab0Bg = if (activeTab == 0) RoseQuartzPrimary else Color.Transparent
                    val tab0Text = if (activeTab == 0) RoseQuartzOnPrimary else RoseQuartzTextPrimary
                    val tab1Bg = if (activeTab == 1) RoseQuartzPrimary else Color.Transparent
                    val tab1Text = if (activeTab == 1) RoseQuartzOnPrimary else RoseQuartzTextPrimary

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(tab0Bg)
                            .clickable { activeTab = 0 }
                            .padding(vertical = 10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.MyLocation,
                                contentDescription = null,
                                tint = tab0Text,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Google Location",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = tab0Text
                            )
                        }
                    }

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(tab1Bg)
                            .clickable { activeTab = 1 }
                            .padding(vertical = 10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Translate,
                                contentDescription = null,
                                tint = tab1Text,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Translation",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = tab1Text
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Scrollable Content per Tab
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (activeTab == 0) {
                        // -------------------------------------------------------------
                        // GOOGLE LOCATION SETTINGS TAB
                        // -------------------------------------------------------------
                        
                        // Toggle Main Master Location Switch
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            color = RoseQuartzPrimaryContainer.copy(alpha = 0.4f),
                            border = BorderStroke(1.dp, RoseQuartzPrimary.copy(alpha = 0.3f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Place,
                                        contentDescription = null,
                                        tint = RoseQuartzPrimary,
                                        modifier = Modifier.size(28.dp)
                                    )
                                    Column {
                                        Text(
                                            text = "Google Location Services",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = RoseQuartzTextPrimary
                                        )
                                        Text(
                                            text = if (settings.isLocationEnabled) "Active • High Precision GPS/Wi-Fi" else "Location disabled globally",
                                            fontSize = 12.sp,
                                            color = RoseQuartzTextSecondary
                                        )
                                    }
                                }

                                Switch(
                                    checked = settings.isLocationEnabled,
                                    onCheckedChange = onToggleLocation,
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = RoseQuartzOnPrimary,
                                        checkedTrackColor = RoseQuartzPrimary
                                    )
                                )
                            }
                        }

                        if (settings.isLocationEnabled) {
                            // Current Active Location Display
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                color = Color(0xFFEFF6FF), // Soft Blue
                                border = BorderStroke(1.dp, Color(0xFFBFDBFE))
                            ) {
                                Column(
                                    modifier = Modifier.padding(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.PinDrop,
                                            contentDescription = null,
                                            tint = Color(0xFF2563EB),
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Text(
                                            text = "Current Active Location:",
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 12.sp,
                                            color = Color(0xFF1E40AF)
                                        )
                                    }

                                    Text(
                                        text = settings.selectedLocation,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = Color(0xFF1E3A8A)
                                    )

                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Lat: ${settings.latitude} | Lng: ${settings.longitude}",
                                            fontSize = 11.sp,
                                            color = Color(0xFF3B82F6),
                                            fontFamily = FontFamily.Monospace
                                        )
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = Color(0xFFDBEAFE)
                                        ) {
                                            Text(
                                                text = if (settings.isAutoLocation) "Auto-GPS" else "Manual",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF1D4ED8),
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                            }



                            // Manual Search TextField
                            OutlinedTextField(
                                value = customSearchQuery,
                                onValueChange = { customSearchQuery = it },
                                label = { Text("Search Custom Google Location / City") },
                                placeholder = { Text("e.g. Chicago, Toronto, Rome...") },
                                leadingIcon = {
                                    Icon(Icons.Filled.Search, contentDescription = null, tint = RoseQuartzPrimary)
                                },
                                trailingIcon = {
                                    if (customSearchQuery.isNotEmpty()) {
                                        IconButton(onClick = {
                                            onSetLocation(customSearchQuery, 41.8781, -87.6298, false)
                                            customSearchQuery = ""
                                        }) {
                                            Icon(Icons.Filled.Check, contentDescription = "Apply", tint = RoseQuartzPrimary)
                                        }
                                    }
                                },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )

                            // Popular Google Locations Selector
                            Text(
                                text = "Select Popular Google Regions:",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = RoseQuartzTextPrimary
                            )

                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                POPULAR_LOCATIONS.chunked(2).forEach { rowPresets ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        rowPresets.forEach { preset ->
                                            val isSelected = settings.selectedLocation.contains(preset.name, ignoreCase = true)
                                            val chipBg = if (isSelected) RoseQuartzPrimary else RoseQuartzContainerHighest.copy(alpha = 0.4f)
                                            val chipTextColor = if (isSelected) RoseQuartzOnPrimary else RoseQuartzTextPrimary

                                            Surface(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .clickable {
                                                        onSetLocation(preset.name, preset.lat, preset.lng, false)
                                                    },
                                                color = chipBg,
                                                border = BorderStroke(
                                                    1.dp,
                                                    if (isSelected) RoseQuartzPrimary else RoseQuartzContainerHighest
                                                )
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                ) {
                                                    Text(text = preset.flag, fontSize = 16.sp)
                                                    Column {
                                                        Text(
                                                            text = preset.name,
                                                            fontWeight = FontWeight.Bold,
                                                            fontSize = 12.sp,
                                                            color = chipTextColor,
                                                            maxLines = 1
                                                        )
                                                        Text(
                                                            text = preset.region,
                                                            fontSize = 10.sp,
                                                            color = chipTextColor.copy(alpha = 0.8f)
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                        if (rowPresets.size == 1) {
                                            Spacer(modifier = Modifier.weight(1f))
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        // -------------------------------------------------------------
                        // LANGUAGE TRANSLATION SETTINGS TAB
                        // -------------------------------------------------------------

                        // Toggle Main Master Translation Switch
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            color = RoseQuartzPrimaryContainer.copy(alpha = 0.4f),
                            border = BorderStroke(1.dp, RoseQuartzPrimary.copy(alpha = 0.3f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.GTranslate,
                                        contentDescription = null,
                                        tint = RoseQuartzPrimary,
                                        modifier = Modifier.size(28.dp)
                                    )
                                    Column {
                                        Text(
                                            text = "Google Language Translation",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = RoseQuartzTextPrimary
                                        )
                                        Text(
                                            text = if (settings.isTranslationEnabled) "Active • Target: ${settings.targetLanguage}" else "Translation disabled",
                                            fontSize = 12.sp,
                                            color = RoseQuartzTextSecondary
                                        )
                                    }
                                }

                                Switch(
                                    checked = settings.isTranslationEnabled,
                                    onCheckedChange = onToggleTranslation,
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = RoseQuartzOnPrimary,
                                        checkedTrackColor = RoseQuartzPrimary
                                    )
                                )
                            }
                        }

                        if (settings.isTranslationEnabled) {
                            // Auto Translate Content Toggle
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(RoseQuartzContainerHighest.copy(alpha = 0.3f))
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Auto-Translate App Feeds & Notes",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = RoseQuartzTextPrimary
                                    )
                                    Text(
                                        text = "Translates News, Chat & Diary into your selected target language",
                                        fontSize = 11.sp,
                                        color = RoseQuartzTextSecondary
                                    )
                                }
                                Switch(
                                    checked = settings.autoTranslateContent,
                                    onCheckedChange = onToggleAutoTranslate
                                )
                            }

                            // Translation Engine Selection
                            Text(
                                text = "Google Translation Engine:",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = RoseQuartzTextPrimary
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                val engine1 = "Google ML Kit (On-Device)"
                                val engine2 = "Google Cloud Translate v3"

                                val isE1Selected = settings.translationEngine == engine1
                                val isE2Selected = settings.translationEngine == engine2

                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .clickable { onSetTranslationEngine(engine1) },
                                    color = if (isE1Selected) RoseQuartzPrimary else RoseQuartzContainerHighest.copy(alpha = 0.4f),
                                    border = BorderStroke(1.dp, if (isE1Selected) RoseQuartzPrimary else RoseQuartzContainerHighest)
                                ) {
                                    Text(
                                        text = "⚡ ML Kit (Offline)",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isE1Selected) RoseQuartzOnPrimary else RoseQuartzTextPrimary,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(vertical = 10.dp)
                                    )
                                }

                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .clickable { onSetTranslationEngine(engine2) },
                                    color = if (isE2Selected) RoseQuartzPrimary else RoseQuartzContainerHighest.copy(alpha = 0.4f),
                                    border = BorderStroke(1.dp, if (isE2Selected) RoseQuartzPrimary else RoseQuartzContainerHighest)
                                ) {
                                    Text(
                                        text = "☁️ Cloud Translate API",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isE2Selected) RoseQuartzOnPrimary else RoseQuartzTextPrimary,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(vertical = 10.dp)
                                    )
                                }
                            }

                            // Target Language Selector
                            Text(
                                text = "Select Target Translation Language:",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = RoseQuartzTextPrimary
                            )

                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                SUPPORTED_LANGUAGES.chunked(2).forEach { rowLangs ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        rowLangs.forEach { lang ->
                                            val isSelected = settings.targetLanguageCode == lang.code
                                            val chipBg = if (isSelected) RoseQuartzPrimary else RoseQuartzContainerHighest.copy(alpha = 0.4f)
                                            val chipTextColor = if (isSelected) RoseQuartzOnPrimary else RoseQuartzTextPrimary

                                            Surface(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .clickable {
                                                        onSetLanguage(lang.name, lang.code)
                                                    },
                                                color = chipBg,
                                                border = BorderStroke(
                                                    1.dp,
                                                    if (isSelected) RoseQuartzPrimary else RoseQuartzContainerHighest
                                                )
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                ) {
                                                    Text(text = lang.flag, fontSize = 18.sp)
                                                    Column {
                                                        Text(
                                                            text = lang.name,
                                                            fontWeight = FontWeight.Bold,
                                                            fontSize = 12.sp,
                                                            color = chipTextColor,
                                                            maxLines = 1
                                                        )
                                                        Text(
                                                            text = "${lang.nativeName} (${lang.code.uppercase()})",
                                                            fontSize = 10.sp,
                                                            color = chipTextColor.copy(alpha = 0.8f)
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                        if (rowLangs.size == 1) {
                                            Spacer(modifier = Modifier.weight(1f))
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Interactive Live Translation Playground Box
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                color = Color(0xFFF0FDF4), // Light Emerald
                                border = BorderStroke(1.dp, Color(0xFFBBF7D0))
                            ) {
                                Column(
                                    modifier = Modifier.padding(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.AutoAwesome,
                                            contentDescription = null,
                                            tint = Color(0xFF16A34A),
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Text(
                                            text = "Live Google Translation Preview:",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = Color(0xFF15803D)
                                        )
                                    }

                                    OutlinedTextField(
                                        value = testTranslationInput,
                                        onValueChange = { testTranslationInput = it },
                                        label = { Text("Test Input Text") },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(10.dp)
                                    )

                                    Surface(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(10.dp),
                                        color = Color.White,
                                        border = BorderStroke(1.dp, Color(0xFF86EFAC))
                                    ) {
                                        Text(
                                            text = testTranslationOutput,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = Color(0xFF166534),
                                            modifier = Modifier.padding(10.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Done Button
                Button(
                    onClick = onDismissRequest,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = RoseQuartzPrimary,
                        contentColor = RoseQuartzOnPrimary
                    )
                ) {
                    Text(
                        text = "Save & Apply Global Settings",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}
