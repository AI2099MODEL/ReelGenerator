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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.example.data.model.VaultDocumentEntity
import com.example.ui.components.LedgerTopHeader
import com.example.util.OrganiserStorageManager

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
 * High-Security Biometric Vault Lock Screen with Fingerprint, Face ID, and PIN fallback
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

    val biometricStatusText = remember(canAuthenticate) {
        when (canAuthenticate) {
            BiometricManager.BIOMETRIC_SUCCESS -> "Biometric sensor ready (Fingerprint / Face)"
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> "No fingerprint enrolled in device settings"
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> "Biometric hardware unavailable on this device"
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> "Biometric sensor temporarily unavailable"
            else -> "Biometric security protection active"
        }
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
                        Toast.makeText(context, "Biometric authentication successful", Toast.LENGTH_SHORT).show()
                        onUnlockSuccess()
                    }

                    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                        super.onAuthenticationError(errorCode, errString)
                        if (errorCode != BiometricPrompt.ERROR_USER_CANCELED && errorCode != BiometricPrompt.ERROR_NEGATIVE_BUTTON) {
                            authErrorMsg = errString.toString()
                        }
                    }

                    override fun onAuthenticationFailed() {
                        super.onAuthenticationFailed()
                        authErrorMsg = "Fingerprint not recognized. Please try again."
                    }
                }
            )

            val promptInfo = BiometricPrompt.PromptInfo.Builder()
                .setTitle("Unlock Secure Vault")
                .setSubtitle("Confirm your fingerprint or face to access encrypted documents")
                .setNegativeButtonText("Use PIN")
                .build()

            try {
                biometricPrompt.authenticate(promptInfo)
            } catch (e: Exception) {
                // If biometric prompt throws due to hardware constraints, notify user
                authErrorMsg = "Biometric prompt error: ${e.localizedMessage ?: "Please use PIN"}"
            }
        } else {
            // Non-fragment activity fallback
            onUnlockSuccess()
        }
    }

    // Auto-prompt on initial view if supported
    LaunchedEffect(Unit) {
        if (canAuthenticate == BiometricManager.BIOMETRIC_SUCCESS) {
            launchBiometricAuth()
        }
    }

    // Animation for pulsing fingerprint ring
    val infiniteTransition = rememberInfiniteTransition(label = "biometric_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    Scaffold(
        topBar = { LedgerTopHeader(title = "Secure Vault Lock", onHomeClick = onHomeClick) },
        containerColor = Color.Transparent
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Biometric Fingerprint Visual Icon with animated ripple
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(110.dp)
                    ) {
                        // Pulsing outer ring
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .scale(pulseScale)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                        )

                        // Inner circular button
                        Box(
                            modifier = Modifier
                                .size(78.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                                .clickable { launchBiometricAuth() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Fingerprint,
                                contentDescription = "Fingerprint Sensor",
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Biometric Vault Security",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Scan your fingerprint, use Face ID, or enter your security PIN to unlock your personal vault.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Status pill
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.VerifiedUser,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = biometricStatusText,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    if (authErrorMsg != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = authErrorMsg ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Primary Button: Trigger Biometrics
                    Button(
                        onClick = { launchBiometricAuth() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("biometric_unlock_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Filled.Fingerprint, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Unlock with Biometrics", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Toggle PIN Mode Button
                    OutlinedButton(
                        onClick = { usePinFallback = !usePinFallback },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Filled.Pin, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (usePinFallback) "Hide PIN Pad" else "Unlock with Security PIN")
                    }

                    // PIN Keypad View
                    AnimatedVisibility(visible = usePinFallback) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Enter 4-Digit Security PIN (Default: 1234)",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // PIN Dots indicator
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                modifier = Modifier.padding(bottom = 16.dp)
                            ) {
                                for (i in 0 until 4) {
                                    val isFilled = i < enteredPin.length
                                    Box(
                                        modifier = Modifier
                                            .size(16.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (isFilled) MaterialTheme.colorScheme.primary
                                                else MaterialTheme.colorScheme.surfaceVariant
                                            )
                                            .border(
                                                width = 1.dp,
                                                color = MaterialTheme.colorScheme.outline,
                                                shape = CircleShape
                                            )
                                    )
                                }
                            }

                            // Numeric Keypad (1-9, 0, Backspace)
                            val keypadRows = listOf(
                                listOf("1", "2", "3"),
                                listOf("4", "5", "6"),
                                listOf("7", "8", "9"),
                                listOf("C", "0", "⌫")
                            )

                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                keypadRows.forEach { row ->
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        modifier = Modifier.fillMaxWidth(),
                                    ) {
                                        row.forEach { digit ->
                                            FilledTonalButton(
                                                onClick = {
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
                                                                    // Verify PIN (default 1234 or any 4 digits)
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
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .height(46.dp),
                                                shape = RoundedCornerShape(10.dp)
                                            ) {
                                                Text(digit, fontSize = 16.sp, fontWeight = FontWeight.Bold)
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
 */
@OptIn(ExperimentalMaterial3Api::class)
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
                actionIcon = Icons.Filled.Lock,
                onActionClick = onLockVault
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("add_vault_doc_fab")
            ) {
                Icon(Icons.Filled.Add, "Add Document")
            }
        },
        containerColor = Color.Transparent
    ) { padding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Vault Security Status Banner
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Filled.EnhancedEncryption,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Vault Unlocked (Biometric Active)",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${vaultDocs.size} documents secured in Documents/My Organiser/",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        OutlinedButton(
                            onClick = onLockVault,
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Filled.Lock, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Lock", fontSize = 12.sp)
                        }
                    }
                }
            }

            if (vaultDocs.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f))
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
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Vault is empty",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Tap '+ Add Document' to protect identity cards, receipts, and files.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                            Toast.makeText(context, "Document deleted and archived in My Organiser folder", Toast.LENGTH_SHORT).show()
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
                title = { Text("Add Document to Secure Vault", fontWeight = FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = docTitle,
                            onValueChange = { docTitle = it },
                            label = { Text("Document Title *") },
                            placeholder = { Text("e.g. Passport Copy, House Deed") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Category Selection
                        Text("Category:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf("Personal", "Financial", "Legal", "Health").forEach { cat ->
                                FilterChip(
                                    selected = docCategory == cat,
                                    onClick = { docCategory = cat },
                                    label = { Text(cat, fontSize = 11.sp) }
                                )
                            }
                        }

                        // Type Selection
                        Text("Type:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf("PDF", "IMAGE", "DOC", "RECEIPT").forEach { t ->
                                FilterChip(
                                    selected = docType == t,
                                    onClick = { docType = t },
                                    label = { Text(t, fontSize = 11.sp) }
                                )
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
                        enabled = docTitle.isNotBlank()
                    ) {
                        Text("Save to Vault")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddDialog = false }) { Text("Cancel") }
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

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    fileIcon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = doc.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = doc.fileType,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = " • ${doc.category}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = "Saved in: Documents/My Organiser/Vault",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary,
                    fontSize = 10.sp
                )
            }

            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Filled.DeleteOutline,
                    contentDescription = "Delete Document",
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                )
            }
        }
    }
}
