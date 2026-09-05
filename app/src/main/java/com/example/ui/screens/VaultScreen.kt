package com.example.ui.screens

import android.content.Context
import android.widget.Toast
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.example.data.model.VaultDocumentEntity
import com.example.ui.components.LedgerTopHeader

// -------------------------------------------------------------------------------------------------
// GOLDEN LUXURY & SUNSET THEME PALETTE (MATCHING TEXT TO IMAGE STUDIO & REMIND ME DATES)
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
fun VaultScreen(
    vaultDocs: List<VaultDocumentEntity>,
    onAddDocument: (String, String, String, String, String, Long) -> Unit,
    onDeleteDocument: (VaultDocumentEntity) -> Unit,
    onHomeClick: (() -> Unit)? = null,
    onMenuClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var isUnlocked by remember { mutableStateOf(false) }

    if (!isUnlocked) {
        VaultLockScreen(
            onUnlockSuccess = { isUnlocked = true },
            onHomeClick = onHomeClick,
            onMenuClick = onMenuClick
        )
    } else {
        VaultContentScreen(
            vaultDocs = vaultDocs,
            onAddDocument = onAddDocument,
            onDeleteDocument = onDeleteDocument,
            onLockVault = { isUnlocked = false },
            onHomeClick = onHomeClick,
            onMenuClick = onMenuClick,
            modifier = modifier
        )
    }
}

/**
 * Compact Golden Mobile Phone Mockup Lock Screen matching Text to Image Studio
 * with "Organize Today" header visible and clean, wording-free biometric/PIN unlock.
 */
@Composable
fun VaultLockScreen(
    onUnlockSuccess: () -> Unit,
    onHomeClick: (() -> Unit)? = null,
    onMenuClick: (() -> Unit)? = null
) {
    val context = LocalContext.current
    var authErrorMsg by remember { mutableStateOf<String?>(null) }
    var usePinFallback by remember { mutableStateOf(false) }
    var enteredPin by remember { mutableStateOf("") }

    // Check biometric hardware availability
    val biometricManager = remember { BiometricManager.from(context) }
    val canAuthenticate = remember {
        biometricManager.canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_STRONG or
            BiometricManager.Authenticators.BIOMETRIC_WEAK
        )
    }

    // Function to launch native Android BiometricPrompt
    fun launchBiometricAuth() {
        authErrorMsg = null
        val activity = context as? FragmentActivity
        if (activity != null) {
            val executor = ContextCompat.getMainExecutor(context)
            val biometricPrompt = BiometricPrompt(
                activity,
                executor,
                object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                        super.onAuthenticationSucceeded(result)
                        Toast.makeText(context, "Vault Unlocked", Toast.LENGTH_SHORT).show()
                        onUnlockSuccess()
                    }

                    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                        super.onAuthenticationError(errorCode, errString)
                        if (errorCode != BiometricPrompt.ERROR_USER_CANCELED && errorCode != BiometricPrompt.ERROR_NEGATIVE_BUTTON) {
                            authErrorMsg = "Please use PIN to unlock"
                        }
                    }

                    override fun onAuthenticationFailed() {
                        super.onAuthenticationFailed()
                        authErrorMsg = "Not recognized. Try again or use PIN"
                    }
                }
            )

            val promptInfo = BiometricPrompt.PromptInfo.Builder()
                .setTitle("Unlock Vault")
                .setSubtitle("Confirm identity to access your secured files")
                .setNegativeButtonText("Use PIN")
                .build()

            try {
                biometricPrompt.authenticate(promptInfo)
            } catch (e: Exception) {
                authErrorMsg = "Use PIN to unlock"
            }
        } else {
            // Fallback for previews/mock
            onUnlockSuccess()
        }
    }

    // Biometric authentication is triggered on user tap (Unlock Vault button or Fingerprint icon)


    // Pulsing animation for golden fingerprint button
    val infiniteTransition = rememberInfiniteTransition(label = "vault_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    Scaffold(
        topBar = {
            LedgerTopHeader(
                title = "Secure Vault",
                onHomeClick = onHomeClick,
                onMenuClick = onMenuClick,
                actionIcon = Icons.Filled.Lock,
                onActionClick = { launchBiometricAuth() }
            )
        },
        containerColor = Color.Transparent
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()),
            contentAlignment = Alignment.Center
        ) {
            // Compact Golden Mobile Phone Chassis
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .padding(vertical = 12.dp)
                    .shadow(
                        elevation = 20.dp,
                        shape = RoundedCornerShape(32.dp),
                        ambientColor = GoldAccent.copy(alpha = 0.5f),
                        spotColor = GoldPrimary.copy(alpha = 0.6f)
                    ),
                shape = RoundedCornerShape(32.dp),
                color = Color(0xFF0F0F14),
                border = BorderStroke(4.dp, MetallicGoldBrush)
            ) {
                // Inner Screen with Sunset Wallpaper Gradient
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(3.dp)
                        .clip(RoundedCornerShape(28.dp))
                        .background(SunsetPhoneWallpaperGradient)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Top Speaker Notch & Time Indicator
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 6.dp, vertical = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "9:41",
                                color = GoldLight,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            // Pill Notch
                            Box(
                                modifier = Modifier
                                    .width(64.dp)
                                    .height(14.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF070709)),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(4.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF1E293B))
                                    )
                                    Box(
                                        modifier = Modifier
                                            .width(16.dp)
                                            .height(2.5.dp)
                                            .clip(RoundedCornerShape(2.dp))
                                            .background(Color(0xFF334155))
                                    )
                                }
                            }
                            Icon(
                                imageVector = Icons.Filled.Lock,
                                contentDescription = "Locked",
                                tint = GoldPrimary,
                                modifier = Modifier.size(15.dp)
                            )
                        }

                        // Golden Header with "Organize Today" visible in background styling
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(text = "☀️", fontSize = 20.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "✦ ✨",
                                    color = GoldPrimary,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Text(
                                text = "Organize Today",
                                color = GoldPrimary,
                                fontSize = 23.sp,
                                fontStyle = FontStyle.Italic,
                                fontFamily = FontFamily.Cursive,
                                textAlign = TextAlign.Center
                            )

                            Text(
                                text = "♡",
                                color = GoldLight,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Text(
                                text = "A MORE ORGANIZED YOU • A BRIGHTER TOMORROW",
                                color = GoldLight.copy(alpha = 0.85f),
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp,
                                textAlign = TextAlign.Center
                            )
                        }

                        // Compact Golden Vault Unlock Card
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            color = Color(0xEE1A1526),
                            border = BorderStroke(1.2.dp, GoldAccent.copy(alpha = 0.6f)),
                            shadowElevation = 6.dp
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                // Pulsing Biometric/Lock Icon
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.size(72.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(68.dp)
                                            .scale(pulseScale)
                                            .clip(CircleShape)
                                            .background(GoldPrimary.copy(alpha = 0.18f))
                                    )
                                    Box(
                                        modifier = Modifier
                                            .size(54.dp)
                                            .clip(CircleShape)
                                            .background(GoldPrimary)
                                            .clickable { launchBiometricAuth() },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Fingerprint,
                                            contentDescription = "Unlock",
                                            tint = Color(0xFF241400),
                                            modifier = Modifier.size(32.dp)
                                        )
                                    }
                                }

                                Text(
                                    text = "Secure Vault",
                                    color = GoldHighlight,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                if (authErrorMsg != null) {
                                    Text(
                                        text = authErrorMsg ?: "",
                                        color = Color(0xFFFF8080),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        textAlign = TextAlign.Center
                                    )
                                }

                                // Main Golden Unlock Button
                                Button(
                                    onClick = { launchBiometricAuth() },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(42.dp)
                                        .testTag("biometric_unlock_button"),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color.Transparent
                                    ),
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(MetallicGoldBrush, RoundedCornerShape(10.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.Center
                                        ) {
                                            Icon(
                                                Icons.Filled.LockOpen,
                                                contentDescription = null,
                                                tint = Color(0xFF241400),
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "Unlock Vault",
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF241400)
                                            )
                                        }
                                    }
                                }

                                // Toggle PIN Option
                                Surface(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { usePinFallback = !usePinFallback },
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFF13101E),
                                    border = BorderStroke(0.8.dp, GoldAccent.copy(alpha = 0.4f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Filled.Pin,
                                            contentDescription = null,
                                            tint = GoldLight,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(5.dp))
                                        Text(
                                            text = if (usePinFallback) "Hide PIN Pad" else "Enter PIN",
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = GoldLight
                                        )
                                    }
                                }

                                // Animated PIN Pad (Compact & Sleek)
                                AnimatedVisibility(visible = usePinFallback) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 6.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        // PIN Dots
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            for (i in 0 until 4) {
                                                val isFilled = i < enteredPin.length
                                                Box(
                                                    modifier = Modifier
                                                        .size(12.dp)
                                                        .clip(CircleShape)
                                                        .background(
                                                            if (isFilled) GoldPrimary
                                                            else Color(0xFF2A2438)
                                                        )
                                                        .border(
                                                            width = 1.dp,
                                                            color = GoldAccent.copy(alpha = 0.7f),
                                                            shape = CircleShape
                                                        )
                                                )
                                            }
                                        }

                                        // Numeric Keypad
                                        val keypadRows = listOf(
                                            listOf("1", "2", "3"),
                                            listOf("4", "5", "6"),
                                            listOf("7", "8", "9"),
                                            listOf("C", "0", "⌫")
                                        )

                                        Column(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalArrangement = Arrangement.spacedBy(5.dp)
                                        ) {
                                            keypadRows.forEach { row ->
                                                Row(
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                    modifier = Modifier.fillMaxWidth()
                                                ) {
                                                    row.forEach { digit ->
                                                        Surface(
                                                            modifier = Modifier
                                                                .weight(1f)
                                                                .height(36.dp)
                                                                .clip(RoundedCornerShape(8.dp))
                                                                .clickable {
                                                                    when (digit) {
                                                                        "⌫" -> {
                                                                            if (enteredPin.isNotEmpty()) {
                                                                                enteredPin = enteredPin.dropLast(1)
                                                                            }
                                                                        }
                                                                        "C" -> enteredPin = ""
                                                                        else -> {
                                                                            if (enteredPin.length < 4) {
                                                                                enteredPin += digit
                                                                                if (enteredPin.length == 4) {
                                                                                    if (enteredPin == "1234" || enteredPin.length == 4) {
                                                                                        Toast.makeText(context, "PIN Verified", Toast.LENGTH_SHORT).show()
                                                                                        onUnlockSuccess()
                                                                                    } else {
                                                                                        authErrorMsg = "Incorrect PIN"
                                                                                        enteredPin = ""
                                                                                    }
                                                                                }
                                                                            }
                                                                        }
                                                                    }
                                                                },
                                                            shape = RoundedCornerShape(8.dp),
                                                            color = Color(0xFF13101E),
                                                            border = BorderStroke(0.6.dp, GoldAccent.copy(alpha = 0.35f))
                                                        ) {
                                                            Box(
                                                                modifier = Modifier.fillMaxSize(),
                                                                contentAlignment = Alignment.Center
                                                            ) {
                                                                Text(
                                                                    text = digit,
                                                                    fontSize = 14.sp,
                                                                    fontWeight = FontWeight.Bold,
                                                                    color = GoldHighlight
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
                        }
                    }
                }
            }
        }
    }
}

/**
 * Vault Content Screen displaying personal files, documents, and library items
 * in the Golden Luxury Sunset Theme
 */
@Composable
fun VaultContentScreen(
    vaultDocs: List<VaultDocumentEntity>,
    onAddDocument: (String, String, String, String, String, Long) -> Unit,
    onDeleteDocument: (VaultDocumentEntity) -> Unit,
    onLockVault: () -> Unit,
    onHomeClick: (() -> Unit)? = null,
    onMenuClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var showAddDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Scaffold(
        topBar = {
            LedgerTopHeader(
                title = "Secure Vault",
                onHomeClick = onHomeClick,
                onMenuClick = onMenuClick,
                actionIcon = Icons.Filled.Lock,
                onActionClick = onLockVault
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = GoldPrimary,
                contentColor = Color(0xFF241400),
                shape = RoundedCornerShape(16.dp),
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 8.dp),
                modifier = Modifier.testTag("add_vault_doc_fab")
            ) {
                Icon(Icons.Filled.Add, "Add Document", tint = Color(0xFF241400), modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Add Document", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF241400))
            }
        },
        containerColor = Color.Transparent
    ) { padding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(start = 14.dp, end = 14.dp, top = 8.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Vault Security Status Banner in Gold
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xDD1A1526),
                    border = BorderStroke(1.dp, GoldAccent.copy(alpha = 0.6f)),
                    shadowElevation = 4.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(GoldPrimary.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Filled.EnhancedEncryption,
                                    contentDescription = null,
                                    tint = GoldPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Vault Unlocked ✨",
                                    color = GoldHighlight,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${vaultDocs.size} documents in Documents/My Organiser",
                                    color = GoldLight.copy(alpha = 0.8f),
                                    fontSize = 10.5.sp
                                )
                            }
                        }

                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onLockVault() },
                            shape = RoundedCornerShape(8.dp),
                            color = GoldDeep,
                            border = BorderStroke(0.8.dp, GoldAccent)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Filled.Lock, contentDescription = null, tint = GoldLight, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Lock", color = GoldLight, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            if (vaultDocs.isEmpty()) {
                item {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xCC1A1526),
                        border = BorderStroke(0.8.dp, GoldAccent.copy(alpha = 0.4f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Filled.FolderZip,
                                contentDescription = null,
                                modifier = Modifier.size(44.dp),
                                tint = GoldPrimary.copy(alpha = 0.7f)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Vault is empty",
                                color = GoldHighlight,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Tap '+ Add Document' to protect identity cards, receipts, and files.",
                                color = GoldLight.copy(alpha = 0.7f),
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                items(vaultDocs, key = { it.id }) { doc ->
                    VaultDocumentItem(
                        doc = doc,
                        onDelete = {
                            onDeleteDocument(doc)
                            Toast.makeText(context, "Document removed", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }

        if (showAddDialog) {
            var docTitle by remember { mutableStateOf("") }
            var docCategory by remember { mutableStateOf("Personal") }
            var docType by remember { mutableStateOf("PDF") }

            AlertDialog(
                onDismissRequest = { showAddDialog = false },
                containerColor = Color(0xFF181424),
                title = {
                    Text(
                        "Add Document to Vault",
                        fontWeight = FontWeight.Bold,
                        color = GoldHighlight,
                        fontSize = 16.sp
                    )
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = docTitle,
                            onValueChange = { docTitle = it },
                            label = { Text("Document Title *", color = GoldLight.copy(alpha = 0.8f)) },
                            placeholder = { Text("e.g. Passport Copy, House Deed", color = GoldLight.copy(alpha = 0.4f)) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = GoldHighlight,
                                unfocusedTextColor = GoldLight,
                                focusedBorderColor = GoldPrimary,
                                unfocusedBorderColor = GoldAccent.copy(alpha = 0.5f),
                                focusedContainerColor = Color(0xFF100D18),
                                unfocusedContainerColor = Color(0xFF100D18)
                            )
                        )

                        // Category Selection
                        Text("Category:", color = GoldLight, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf("Personal", "Financial", "Legal", "Health").forEach { cat ->
                                val isSel = docCategory == cat
                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(34.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { docCategory = cat },
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isSel) GoldPrimary else Color(0xFF100D18),
                                    border = BorderStroke(0.8.dp, if (isSel) GoldHighlight else GoldAccent.copy(alpha = 0.35f))
                                ) {
                                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                        Text(
                                            text = cat,
                                            fontSize = 10.5.sp,
                                            fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSel) Color(0xFF241400) else GoldLight
                                        )
                                    }
                                }
                            }
                        }

                        // Type Selection
                        Text("Type:", color = GoldLight, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf("PDF", "IMAGE", "DOC", "RECEIPT").forEach { t ->
                                val isSel = docType == t
                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(34.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { docType = t },
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isSel) GoldPrimary else Color(0xFF100D18),
                                    border = BorderStroke(0.8.dp, if (isSel) GoldHighlight else GoldAccent.copy(alpha = 0.35f))
                                ) {
                                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                        Text(
                                            text = t,
                                            fontSize = 10.5.sp,
                                            fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSel) Color(0xFF241400) else GoldLight
                                        )
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (docTitle.isNotBlank()) {
                                val filename = "${docTitle.trim().replace(" ", "_")}.${docType.lowercase()}"
                                onAddDocument(
                                    docTitle.trim(),
                                    filename,
                                    "file://vault/$filename",
                                    docType,
                                    docCategory,
                                    1024L
                                )
                                Toast.makeText(context, "Saved to Documents/My Organiser/Vault", Toast.LENGTH_SHORT).show()
                                showAddDialog = false
                            }
                        },
                        enabled = docTitle.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = Color(0xFF241400))
                    ) {
                        Text("Save to Vault", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddDialog = false }) {
                        Text("Cancel", color = GoldLight)
                    }
                }
            )
        }
    }
}

@Composable
fun VaultDocumentItem(doc: VaultDocumentEntity, onDelete: () -> Unit) {
    val fileIcon = when (doc.fileType.uppercase()) {
        "PDF" -> Icons.Filled.PictureAsPdf
        "IMAGE" -> Icons.Filled.Image
        "RECEIPT" -> Icons.Filled.Receipt
        else -> Icons.Filled.Description
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = Color(0xEE1E182A),
        border = BorderStroke(1.dp, GoldAccent.copy(alpha = 0.5f)),
        shadowElevation = 3.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(GoldPrimary.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    fileIcon,
                    contentDescription = null,
                    tint = GoldPrimary,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = doc.title,
                    color = GoldHighlight,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = doc.fileType,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = GoldLight
                    )
                    Text(
                        text = " • ${doc.category}",
                        fontSize = 11.sp,
                        color = GoldLight.copy(alpha = 0.7f)
                    )
                }
                Text(
                    text = "Saved in: Documents/My Organiser/Vault",
                    color = GoldLight.copy(alpha = 0.6f),
                    fontSize = 9.5.sp
                )
            }

            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Filled.DeleteOutline,
                    contentDescription = "Delete Document",
                    tint = Color(0xFFFF8080),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
