package com.example

import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.example.ui.LedgerViewModel
import com.example.ui.screens.MainLedgerScreen
import com.example.ui.theme.*
import com.example.util.NotificationHelper
import java.util.concurrent.Executor

class MainActivity : FragmentActivity() {
    private val viewModel: LedgerViewModel by viewModels()
    private lateinit var prefs: SharedPreferences
    private var isBiometricEnabledState = mutableStateOf(true)
    private var isUnlockedState = mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        prefs = getSharedPreferences("mylyfe_prefs", MODE_PRIVATE)
        val biometricEnabled = prefs.getBoolean("biometric_lock_enabled", false)
        isBiometricEnabledState.value = biometricEnabled
        isUnlockedState.value = !biometricEnabled

        // Initialize notification channel
        NotificationHelper.createNotificationChannel(this)

        setContent {
            LedgerTheme {
                Surface(
                    color = androidx.compose.ui.graphics.Color.Transparent
                ) {
                    val isUnlocked by isUnlockedState
                    val isBiometricEnabled by isBiometricEnabledState

                    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
                        Image(painter = painterResource(id = R.drawable.app_background), contentDescription = null, contentScale = androidx.compose.ui.layout.ContentScale.Crop, modifier = Modifier.fillMaxSize()) // ShaderAnimation110Background(
                        MainLedgerScreen(viewModel = viewModel)

                        if (isBiometricEnabled && !isUnlocked) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(RoseQuartzBg)
                                    .zIndex(10f),
                                contentAlignment = Alignment.Center
                            ) {
                                Surface(
                                    modifier = Modifier
                                        .padding(24.dp)
                                        .fillMaxWidth()
                                        .wrapContentHeight(),
                                    shape = RoundedCornerShape(20.dp),
                                    color = MaterialTheme.colorScheme.surface,
                                    border = BorderStroke(1.dp, RoseQuartzContainerHighest),
                                    shadowElevation = 8.dp
                                ) {
                                    Column(
                                        modifier = Modifier.padding(24.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(16.dp)
                                    ) {
                                        Surface(
                                            modifier = Modifier.size(64.dp),
                                            shape = RoundedCornerShape(32.dp),
                                            color = RoseQuartzPrimary
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    imageVector = Icons.Filled.Fingerprint,
                                                    contentDescription = "Biometric Lock",
                                                    tint = RoseQuartzOnPrimary,
                                                    modifier = Modifier.size(36.dp)
                                                )
                                            }
                                        }

                                        Text(
                                            text = "MyLyfe Secure",
                                            fontFamily = FontFamily.Serif,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 22.sp,
                                            color = RoseQuartzTextPrimary
                                        )

                                        Text(
                                            text = "Authenticate with biometrics (Fingerprint / Face ID) to access your secure records and chat.",
                                            fontSize = 13.sp,
                                            color = RoseQuartzTextSecondary,
                                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                        )

                                        Button(
                                            onClick = { triggerBiometricPrompt() },
                                            colors = ButtonDefaults.buttonColors(containerColor = RoseQuartzPrimary),
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier.fillMaxWidth().height(44.dp)
                                        ) {
                                            Icon(Icons.Filled.Fingerprint, contentDescription = null, modifier = Modifier.size(18.dp))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("Unlock with Biometrics", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        }

                                        // Enable/Disable Biometric Lock Toggle on First Screen
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = "Enable Biometric Lock",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = RoseQuartzTextPrimary
                                            )
                                            Switch(
                                                checked = isBiometricEnabled,
                                                onCheckedChange = { enabled ->
                                                    isBiometricEnabledState.value = enabled
                                                    prefs.edit().putBoolean("biometric_lock_enabled", enabled).apply()
                                                    if (!enabled) {
                                                        isUnlockedState.value = true
                                                    }
                                                },
                                                colors = SwitchDefaults.colors(checkedThumbColor = RoseQuartzPrimary)
                                            )
                                        }

                                        TextButton(
                                            onClick = {
                                                isUnlockedState.value = true
                                            }
                                        ) {
                                            Text("Bypass / Quick Unlock (Demo mode)", fontSize = 11.sp, color = RoseQuartzTextSecondary)
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

    override fun onResume() {
        super.onResume()
        if (isBiometricEnabledState.value) {
            triggerBiometricPrompt()
        }
    }

    private fun triggerBiometricPrompt() {
        if (!isBiometricEnabledState.value) return
        val executor: Executor = ContextCompat.getMainExecutor(this)
        val biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    isUnlockedState.value = true
                    Toast.makeText(applicationContext, "Authentication succeeded!", Toast.LENGTH_SHORT).show()
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    Toast.makeText(applicationContext, "Authentication failed. Try again.", Toast.LENGTH_SHORT).show()
                }
            })

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Unlock MyLyfe")
            .setSubtitle("Confirm your biometric credential to access the application")
            .setNegativeButtonText("Cancel")
            .build()

        try {
            biometricPrompt.authenticate(promptInfo)
        } catch (e: Exception) {
            isUnlockedState.value = true
        }
    }
}
