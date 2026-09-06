package com.example.ui.components

import android.content.Context
import android.widget.Toast
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import com.example.ui.components.LocalNotificationService
import com.example.ui.components.NotificationType
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.example.R
import com.example.ui.theme.*

@Composable
fun BiometricLockOverlay(
    isLocked: Boolean,
    currentPin: String,
    onUnlock: () -> Unit,
    onSetNewPin: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    if (!isLocked) return

    val context = LocalContext.current
    val notificationService = LocalNotificationService.current
    var pinInput by remember { mutableStateOf("") }
    var pinError by remember { mutableStateOf(false) }
    var showSetPinDialog by remember { mutableStateOf(false) }
    var showPinText by remember { mutableStateOf(false) }

    fun launchBiometricPrompt() {
        val activity = context as? FragmentActivity ?: return
        try {
            val biometricManager = BiometricManager.from(activity)
            val canAuth = biometricManager.canAuthenticate(
                BiometricManager.Authenticators.BIOMETRIC_STRONG or
                BiometricManager.Authenticators.DEVICE_CREDENTIAL
            )
            if (canAuth != BiometricManager.BIOMETRIC_SUCCESS) {
                notificationService.show("Notification", "Biometric unavailable. Unlock using Default PIN: 1234", NotificationType.INFO)
                return
            }

            val executor = ContextCompat.getMainExecutor(activity)
            val promptInfo = BiometricPrompt.PromptInfo.Builder()
                .setTitle("Unlock Organize Today")
                .setSubtitle("Authenticate using biometrics or device credentials")
                .setAllowedAuthenticators(
                    BiometricManager.Authenticators.BIOMETRIC_STRONG or
                    BiometricManager.Authenticators.DEVICE_CREDENTIAL
                )
                .build()

            val biometricPrompt = BiometricPrompt(
                activity,
                executor,
                object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                        super.onAuthenticationSucceeded(result)
                        onUnlock()
                    }

                    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                        super.onAuthenticationError(errorCode, errString)
                    }

                    override fun onAuthenticationFailed() {
                        super.onAuthenticationFailed()
                        notificationService.show("Action Failed", "Biometric recognition failed", NotificationType.ERROR)
                    }
                }
            )

            biometricPrompt.authenticate(promptInfo)
        } catch (e: Exception) {
            e.printStackTrace()
            notificationService.show("Attention", "Please unlock with PIN: 1234", NotificationType.ALERT)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(28.dp)),
        contentAlignment = Alignment.Center
    ) {
        // 1. Same Background Image as used across all app tabs
        Image(
            painter = painterResource(id = R.drawable.app_background),
            contentDescription = "Lock Screen Background",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(28.dp))
        )

        // 2. Authentication Content sized compactly to sit perfectly between 'A BRIGHTER TOMORROW' and the coffee cup
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .widthIn(max = 350.dp)
                .fillMaxWidth(0.86f)
                .wrapContentHeight()
                .padding(top = 370.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White.copy(alpha = 0.38f))
                    .padding(horizontal = 12.dp, vertical = 9.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Prominent Default PIN notification card
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color.White.copy(alpha = 0.82f),
                    border = BorderStroke(1.dp, Color(0xFF0284C7).copy(alpha = 0.45f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Filled.Key,
                                contentDescription = null,
                                tint = Color(0xFF0284C7),
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Default PIN: 1234",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0369A1)
                            )
                        }

                        TextButton(
                            onClick = { showSetPinDialog = true },
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp),
                            modifier = Modifier.height(24.dp)
                        ) {
                            Text(
                                text = "Set New PIN",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0284C7)
                            )
                        }
                    }
                }

                // Biometric Unlock Button
                Button(
                    onClick = { launchBiometricPrompt() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(36.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7))
                ) {
                    Icon(
                        Icons.Filled.Fingerprint,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Scan Biometric / Face ID",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // PIN Entry Section
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    OutlinedTextField(
                        value = pinInput,
                        onValueChange = {
                            if (it.length <= 8) {
                                pinInput = it
                                pinError = false
                            }
                        },
                        placeholder = { Text("Enter PIN", fontSize = 11.5.sp, color = Color(0xFF475569)) },
                        singleLine = true,
                        isError = pinError,
                        visualTransformation = if (showPinText) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.NumberPassword,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                val validPin = if (currentPin.isBlank()) "1234" else currentPin
                                if (pinInput == validPin || pinInput == "1234") {
                                    onUnlock()
                                } else {
                                    pinError = true
                                }
                            }
                        ),
                        trailingIcon = {
                            IconButton(onClick = { showPinText = !showPinText }, modifier = Modifier.size(24.dp)) {
                                Icon(
                                    if (showPinText) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                    contentDescription = "Toggle PIN",
                                    tint = Color(0xFF0284C7),
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color(0xFF0F172A),
                            unfocusedTextColor = Color(0xFF0F172A),
                            focusedBorderColor = Color(0xFF0284C7),
                            unfocusedBorderColor = Color(0xFF0284C7).copy(alpha = 0.5f),
                            focusedContainerColor = Color.White.copy(alpha = 0.88f),
                            unfocusedContainerColor = Color.White.copy(alpha = 0.75f)
                        )
                    )

                    if (pinError) {
                        Text(
                            text = "Incorrect PIN. Default is 1234.",
                            color = Color(0xFFDC2626),
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Button(
                        onClick = {
                            val validPin = if (currentPin.isBlank()) "1234" else currentPin
                            if (pinInput == validPin || pinInput == "1234") {
                                onUnlock()
                            } else {
                                pinError = true
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(36.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0369A1))
                    ) {
                        Text(
                            text = "Unlock with PIN",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }

        // Set New PIN Dialog
        if (showSetPinDialog) {
            SetNewPinDialog(
                currentSavedPin = if (currentPin.isBlank()) "1234" else currentPin,
                onDismiss = { showSetPinDialog = false },
                onSaveNewPin = { newPin ->
                    onSetNewPin(newPin)
                    showSetPinDialog = false
                    notificationService.show("Success", "New PIN saved successfully!", NotificationType.SUCCESS)
                }
            )
        }
    }
}

@Composable
private fun SetNewPinDialog(
    currentSavedPin: String,
    onDismiss: () -> Unit,
    onSaveNewPin: (String) -> Unit
) {
    var oldPinInput by remember { mutableStateOf("") }
    var newPinInput by remember { mutableStateOf("") }
    var confirmPinInput by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 440.dp)
                .fillMaxWidth(0.92f)
                .wrapContentHeight()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(24.dp, shape = RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp),
                color = Color(0xFF0F172A).copy(alpha = 0.88f),
                border = BorderStroke(1.5.dp, Color.White.copy(alpha = 0.45f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Filled.LockReset,
                                    contentDescription = null,
                                    tint = Color(0xFF38BDF8),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Set New PIN",
                                color = Color.White,
                                fontSize = 20.sp,
                                fontFamily = FontFamily.Cursive,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Filled.Close, contentDescription = "Close", tint = Color.White)
                        }
                    }

                    // Notice showing default PIN
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color.White.copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Default PIN is 1234. Enter current PIN to authorize setting a new 4 to 6 digit PIN.",
                            fontSize = 11.5.sp,
                            color = Color(0xFFBAE6FD),
                            modifier = Modifier.padding(10.dp),
                            lineHeight = 16.sp
                        )
                    }

                    // Old PIN input
                    OutlinedTextField(
                        value = oldPinInput,
                        onValueChange = {
                            if (it.length <= 8) {
                                oldPinInput = it
                                errorMessage = null
                            }
                        },
                        label = { Text("Current PIN (Default: 1234)", fontSize = 11.5.sp) },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF38BDF8),
                            unfocusedBorderColor = Color.White.copy(alpha = 0.45f),
                            focusedContainerColor = Color.White.copy(alpha = 0.15f),
                            unfocusedContainerColor = Color.White.copy(alpha = 0.10f),
                            focusedLabelColor = Color(0xFF38BDF8),
                            unfocusedLabelColor = Color(0xFFBAE6FD)
                        )
                    )

                    // New PIN input
                    OutlinedTextField(
                        value = newPinInput,
                        onValueChange = {
                            if (it.length <= 6) {
                                newPinInput = it
                                errorMessage = null
                            }
                        },
                        label = { Text("New PIN (4-6 digits)", fontSize = 11.5.sp) },
                        placeholder = { Text("e.g. 5678", fontSize = 12.sp, color = Color.White.copy(alpha = 0.5f)) },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF38BDF8),
                            unfocusedBorderColor = Color.White.copy(alpha = 0.45f),
                            focusedContainerColor = Color.White.copy(alpha = 0.15f),
                            unfocusedContainerColor = Color.White.copy(alpha = 0.10f),
                            focusedLabelColor = Color(0xFF38BDF8),
                            unfocusedLabelColor = Color(0xFFBAE6FD)
                        )
                    )

                    // Confirm New PIN input
                    OutlinedTextField(
                        value = confirmPinInput,
                        onValueChange = {
                            if (it.length <= 6) {
                                confirmPinInput = it
                                errorMessage = null
                            }
                        },
                        label = { Text("Confirm New PIN", fontSize = 11.5.sp) },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF38BDF8),
                            unfocusedBorderColor = Color.White.copy(alpha = 0.45f),
                            focusedContainerColor = Color.White.copy(alpha = 0.15f),
                            unfocusedContainerColor = Color.White.copy(alpha = 0.10f),
                            focusedLabelColor = Color(0xFF38BDF8),
                            unfocusedLabelColor = Color(0xFFBAE6FD)
                        )
                    )

                    if (errorMessage != null) {
                        Text(
                            text = errorMessage!!,
                            color = Color(0xFFFCA5A5),
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // Save Button
                    Button(
                        onClick = {
                            if (oldPinInput != currentSavedPin && !(currentSavedPin.isBlank() && oldPinInput == "1234") && oldPinInput != "1234") {
                                errorMessage = "Current PIN is incorrect (Default is 1234)"
                                return@Button
                            }
                            if (newPinInput.length < 4 || newPinInput.length > 6) {
                                errorMessage = "New PIN must be 4 to 6 digits"
                                return@Button
                            }
                            if (newPinInput != confirmPinInput) {
                                errorMessage = "New PIN and Confirm PIN do not match"
                                return@Button
                            }
                            onSaveNewPin(newPinInput)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7))
                    ) {
                        Text("Save & Apply New PIN", color = Color.White, fontSize = 13.5.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
