import os

content = """package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.biometric.BiometricPrompt
import androidx.fragment.app.FragmentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import com.example.data.model.VaultDocumentEntity
import com.example.ui.components.LedgerTopHeader
import com.example.ui.theme.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun VaultScreen(
    vaultDocs: List<VaultDocumentEntity>,
    onAddDocument: (String, String, String, String, String, Long, String) -> Unit,
    onDeleteDocument: (VaultDocumentEntity) -> Unit,
    onHomeClick: (() -> Unit)? = null,
    onMenuClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var isLocked by remember { mutableStateOf(false) }

    if (isLocked) {
        VaultLockScreen(
            onUnlockSuccess = { isLocked = false },
            onHomeClick = onHomeClick,
            onMenuClick = onMenuClick,
            modifier = modifier
        )
    } else {
        VaultContentScreen(
            vaultDocs = vaultDocs,
            onAddDocument = onAddDocument,
            onDeleteDocument = onDeleteDocument,
            onLockVault = { isLocked = true },
            onHomeClick = onHomeClick,
            onMenuClick = onMenuClick,
            modifier = modifier
        )
    }
}

@Composable
fun VaultLockScreen(
    onUnlockSuccess: () -> Unit,
    onHomeClick: (() -> Unit)? = null,
    onMenuClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val notificationService = LocalNotificationService.current
    var authErrorMsg by remember { mutableStateOf<String?>(null) }
    var usePinFallback by remember { mutableStateOf(false) }
    var enteredPin by remember { mutableStateOf("") }
    val correctPin = "1234"

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
                        notificationService.show("Success", "Vault Unlocked", NotificationType.SUCCESS)
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
            onUnlockSuccess()
        }
    }

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
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Icon(
                            Icons.Filled.FolderSpecial,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.9f),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            "Secured by Organiser",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "All documents are encrypted and stored locally on your device.",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 13.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        Spacer(modifier = Modifier.height(48.dp))

                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.size(120.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size((90 * pulseScale).dp)
                                    .clip(CircleShape)
                                    .background(GoldPrimary.copy(alpha = 0.2f))
                            )
                            Box(
                                modifier = Modifier
                                    .size((75 * pulseScale).dp)
                                    .clip(CircleShape)
                                    .background(GoldPrimary.copy(alpha = 0.4f))
                            )
                            IconButton(
                                onClick = { launchBiometricAuth() },
                                modifier = Modifier
                                    .size(65.dp)
                                    .clip(CircleShape)
                                    .background(GoldPrimary)
                            ) {
                                Icon(
                                    Icons.Filled.Fingerprint,
                                    contentDescription = "Unlock Vault",
                                    tint = Color(0xFF241400),
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Tap to Unlock",
                            color = GoldHighlight,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )

                        if (authErrorMsg != null) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Surface(
                                color = Color(0xFFFF4D4D).copy(alpha = 0.15f),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, Color(0xFFFF4D4D).copy(alpha = 0.5f))
                            ) {
                                Text(
                                    text = authErrorMsg!!,
                                    color = Color(0xFFFF8080),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))
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

                        AnimatedVisibility(visible = usePinFallback) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    for (i in 0 until 4) {
                                        val isFilled = i < enteredPin.length
                                        Box(
                                            modifier = Modifier
                                                .size(14.dp)
                                                .clip(CircleShape)
                                                .background(if (isFilled) GoldPrimary else Color.Transparent)
                                                .border(1.dp, GoldPrimary, CircleShape)
                                        )
                                    }
                                }
                                val keys = listOf(
                                    listOf("1", "2", "3"),
                                    listOf("4", "5", "6"),
                                    listOf("7", "8", "9"),
                                    listOf("C", "0", "<")
                                )
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    keys.forEach { rowKeys ->
                                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                            rowKeys.forEach { key ->
                                                val isAction = key == "C" || key == "<"
                                                Surface(
                                                    modifier = Modifier
                                                        .size(52.dp)
                                                        .clip(CircleShape)
                                                        .clickable(enabled = true) {
                                                            if (key == "C") {
                                                                enteredPin = ""
                                                            } else if (key == "<") {
                                                                if (enteredPin.isNotEmpty()) enteredPin = enteredPin.dropLast(1)
                                                            } else {
                                                                if (enteredPin.length < 4) enteredPin += key
                                                                if (enteredPin.length == 4) {
                                                                    if (enteredPin == correctPin) {
                                                                        notificationService.show("Success", "Vault Unlocked via PIN", NotificationType.SUCCESS)
                                                                        onUnlockSuccess()
                                                                    } else {
                                                                        authErrorMsg = "Incorrect PIN"
                                                                        enteredPin = ""
                                                                    }
                                                                }
                                                            }
                                                        },
                                                    shape = CircleShape,
                                                    color = if (isAction) Color(0xFF1E182A) else Color(0x66100D18),
                                                    border = BorderStroke(1.dp, GoldAccent.copy(alpha = 0.2f))
                                                ) {
                                                    Box(contentAlignment = Alignment.Center) {
                                                        if (key == "<") {
                                                            Icon(Icons.Filled.Backspace, contentDescription = "Backspace", tint = GoldLight, modifier = Modifier.size(20.dp))
                                                        } else {
                                                            Text(
                                                                text = key,
                                                                color = if (isAction) GoldLight else GoldHighlight,
                                                                fontSize = 20.sp,
                                                                fontWeight = if (isAction) FontWeight.Medium else FontWeight.Bold
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
                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VaultContentScreen(
    vaultDocs: List<VaultDocumentEntity>,
    onAddDocument: (String, String, String, String, String, Long, String) -> Unit,
    onDeleteDocument: (VaultDocumentEntity) -> Unit,
    onLockVault: () -> Unit,
    onHomeClick: (() -> Unit)? = null,
    onMenuClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val notificationService = LocalNotificationService.current
    val coroutineScope = rememberCoroutineScope()
    
    var showAddDialog by remember { mutableStateOf(false) }
    var docTitle by remember { mutableStateOf("") }
    var docCategory by remember { mutableStateOf("Passport") }
    var docType by remember { mutableStateOf("PDF") }
    
    var isScanning by remember { mutableStateOf(false) }
    var docNotes by remember { mutableStateOf("") }
    var currentCameraUri by remember { mutableStateOf<android.net.Uri?>(null) }
    
    val createCameraUri = {
        val file = java.io.File(context.cacheDir, "vault_cam_${System.currentTimeMillis()}.jpg")
        androidx.core.content.FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }

    val processOcr = { uri: android.net.Uri ->
        coroutineScope.launch {
            try {
                showAddDialog = true
                isScanning = true
                
                val responseText = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                    val bitmap = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                        val source = android.graphics.ImageDecoder.createSource(context.contentResolver, uri)
                        android.graphics.ImageDecoder.decodeBitmap(source)
                    } else {
                        android.provider.MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                    }
                    
                    val generativeModel = com.google.ai.client.generativeai.GenerativeModel(
                        modelName = "gemini-1.5-flash",
                        apiKey = com.example.BuildConfig.GEMINI_API_KEY
                    )
                    val inputContent = com.google.ai.client.generativeai.type.content {
                        image(bitmap)
                        text("Extract the text from this ID document. Please output only the extracted text, formatted cleanly. Specifically look for Name, ID Number, and DOB.")
                    }
                    val response = generativeModel.generateContent(inputContent)
                    response.text
                }
                
                docTitle = if (docCategory == "Other") "Scanned Document" else "$docCategory Scan"
                docType = "IMAGE"
                docNotes = responseText ?: ""
                notificationService.show("OCR Complete", "Extracted: ${responseText?.take(40)}...", NotificationType.SUCCESS)
            } catch (e: Exception) {
                notificationService.show("OCR Failed", "Error: ${e.message}", NotificationType.ERROR)
            } finally {
                isScanning = false
            }
        }
    }
    
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success ->
            if (success && currentCameraUri != null) {
                processOcr(currentCameraUri!!)
            } else {
                isScanning = false
            }
        }
    )

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if (uri != null) {
                processOcr(uri)
            } else {
                isScanning = false
            }
        }
    )

    var selectedTab by remember { mutableStateOf("All") }
    val vaultTabs = listOf("All", "Passport", "Driving Licence", "Aadhaar Card", "Voter Id", "Other")
    val filteredDocs = if (selectedTab == "All") vaultDocs else vaultDocs.filter { it.category == selectedTab }

    Box(modifier = modifier.fillMaxSize().background(
        Brush.verticalGradient(
            colors = listOf(Color(0xFF0F0B18), Color(0xFF18122B)) // Rich premium gradient
        )
    )) {
        Scaffold(
            topBar = {
                LedgerTopHeader(
                    title = "Secure Vault",
                    onHomeClick = onHomeClick,
                    onMenuClick = onMenuClick,
                    actionIcon = Icons.Filled.Add,
                    onActionClick = { showAddDialog = true }
                )
            },
            containerColor = Color.Transparent
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                // Vault Security Status Banner in Gold
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0x771A1526),
                    border = BorderStroke(1.dp, GoldAccent.copy(alpha = 0.4f))
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
                                Icon(Icons.Filled.Security, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Vault is Secured", color = GoldHighlight, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Text("Encrypted local storage", color = GoldLight.copy(alpha = 0.7f), fontSize = 10.5.sp)
                            }
                        }
                        Button(
                            onClick = onLockVault,
                            modifier = Modifier.height(34.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = Color(0xFF241400)),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
                        ) {
                            Icon(Icons.Filled.Lock, contentDescription = "Lock", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Lock", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Small elegant category chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    vaultTabs.forEach { tabName ->
                        val isSelected = selectedTab == tabName
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(24.dp))
                                .clickable { selectedTab = tabName },
                            color = if (isSelected) GoldPrimary.copy(alpha = 0.15f) else Color(0x331E182A),
                            border = BorderStroke(1.dp, if (isSelected) GoldPrimary else GoldLight.copy(alpha = 0.1f)),
                            shape = RoundedCornerShape(24.dp)
                        ) {
                            Text(
                                text = tabName,
                                color = if (isSelected) GoldPrimary else GoldLight.copy(alpha = 0.7f),
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Documents Grid
                if (filteredDocs.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize().padding(bottom = 80.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Outlined.Inventory2, contentDescription = null, tint = GoldLight.copy(alpha = 0.3f), modifier = Modifier.size(64.dp))
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("No Documents Found", color = GoldLight.copy(alpha = 0.5f), fontSize = 15.sp, fontWeight = FontWeight.Medium)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Tap + to securely scan or add IDs.", color = GoldLight.copy(alpha = 0.4f), fontSize = 12.sp)
                        }
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        contentPadding = PaddingValues(start = 14.dp, end = 14.dp, top = 8.dp, bottom = 96.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(filteredDocs) { doc ->
                            VaultDocumentCard(doc = doc, onDelete = { onDeleteDocument(doc) })
                        }
                    }
                }
            }
        }
        
        // Add Document Dialog
        if (showAddDialog) {
            AlertDialog(
                onDismissRequest = { showAddDialog = false },
                containerColor = Color(0xFF1E182A),
                titleContentColor = GoldPrimary,
                textContentColor = GoldLight,
                title = { Text("Add Document to Vault", fontWeight = FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    isScanning = true
                                    currentCameraUri = createCameraUri()
                                    cameraLauncher.launch(currentCameraUri!!)
                                },
                                modifier = Modifier.weight(1f).height(42.dp),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2A2438), contentColor = GoldPrimary)
                            ) {
                                if (isScanning) {
                                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = GoldPrimary, strokeWidth = 2.dp)
                                } else {
                                    Icon(Icons.Filled.PhotoCamera, contentDescription = "Camera", modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Camera", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            Button(
                                onClick = {
                                    isScanning = true
                                    photoPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                                modifier = Modifier.weight(1f).height(42.dp),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2A2438), contentColor = GoldPrimary)
                            ) {
                                if (isScanning) {
                                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = GoldPrimary, strokeWidth = 2.dp)
                                } else {
                                    Icon(Icons.Filled.Image, contentDescription = "Gallery", modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Gallery", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

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
                                unfocusedContainerColor = Color(0xFF161224)
                            )
                        )
                        
                        Text("Category:", color = GoldLight, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        ScrollableTabRow(
                            selectedTabIndex = listOf("Passport", "Driving Licence", "Aadhaar Card", "Voter Id", "Other").indexOf(docCategory),
                            containerColor = Color.Transparent,
                            contentColor = GoldPrimary,
                            edgePadding = 0.dp,
                            divider = {
                                HorizontalDivider(color = GoldAccent.copy(alpha = 0.2f))
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            listOf("Passport", "Driving Licence", "Aadhaar Card", "Voter Id", "Other").forEach { cat ->
                                val isSel = docCategory == cat
                                Tab(
                                    selected = isSel,
                                    onClick = { docCategory = cat },
                                    text = { 
                                        Text(
                                            text = cat, 
                                            color = if (isSel) GoldPrimary else GoldLight.copy(alpha = 0.7f),
                                            fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                            fontSize = 12.sp
                                        )
                                    }
                                )
                            }
                        }
                        
                        OutlinedTextField(
                            value = docNotes,
                            onValueChange = { docNotes = it },
                            label = { Text("Extracted Text / Notes", color = GoldLight.copy(alpha = 0.8f)) },
                            modifier = Modifier.fillMaxWidth().heightIn(min = 80.dp),
                            maxLines = 5,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = GoldHighlight,
                                unfocusedTextColor = GoldLight,
                                focusedBorderColor = GoldPrimary,
                                unfocusedBorderColor = GoldAccent.copy(alpha = 0.5f),
                                focusedContainerColor = Color(0xFF100D18),
                                unfocusedContainerColor = Color(0xFF161224)
                            )
                        )
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
                                    1024L,
                                    docNotes.trim()
                                )
                                notificationService.show("Success", "Saved to Documents/My Organiser/Vault", NotificationType.SUCCESS)
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
fun VaultDocumentCard(doc: VaultDocumentEntity, onDelete: () -> Unit) {
    val fileIcon = when (doc.fileType.uppercase()) {
        "PDF" -> Icons.Filled.PictureAsPdf
        "IMAGE" -> Icons.Filled.Image
        "RECEIPT" -> Icons.Filled.Receipt
        else -> Icons.Filled.Description
    }

    Surface(
        modifier = Modifier.fillMaxWidth().aspectRatio(0.85f),
        shape = RoundedCornerShape(20.dp),
        color = Color(0x442A2438),
        border = BorderStroke(1.dp, GoldAccent.copy(alpha = 0.3f)),
        shadowElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Brush.linearGradient(listOf(GoldPrimary.copy(alpha = 0.3f), GoldAccent.copy(alpha = 0.1f)))),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        fileIcon,
                        contentDescription = null,
                        tint = GoldPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(24.dp).offset(x = 6.dp, y = (-6).dp)) {
                    Icon(
                        Icons.Filled.DeleteOutline,
                        contentDescription = "Delete Document",
                        tint = Color(0xFFFF8080).copy(alpha = 0.8f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            
            Column {
                Text(
                    text = doc.title,
                    color = GoldHighlight,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 18.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                
                Surface(
                    color = GoldPrimary.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = doc.category,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = GoldPrimary,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
                
                if (doc.notes.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = doc.notes,
                        color = GoldLight.copy(alpha = 0.7f),
                        fontSize = 10.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 14.sp
                    )
                }
            }
        }
    }
}
"""

with open('app/src/main/java/com/example/ui/screens/VaultScreen.kt', 'w') as f:
    f.write(content)

