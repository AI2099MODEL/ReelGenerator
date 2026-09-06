package com.example.ui.components

import android.content.Intent
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

@Composable
fun SpeechToTextButton(
    onResult: (String) -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = Color.Gray
) {
    val context = LocalContext.current
    val notificationService = LocalNotificationService.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val data = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val text = data?.get(0) ?: ""
            if (text.isNotEmpty()) {
                onResult(text)
            }
        }
    }

    IconButton(
        onClick = {
            try {
                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak now...")
                }
                launcher.launch(intent)
            } catch (e: Exception) {
                notificationService.show("Notification", "Voice input not supported on this device.", NotificationType.INFO)
            }
        },
        modifier = modifier
    ) {
        Icon(
            imageVector = Icons.Default.Mic,
            contentDescription = "Voice Input",
            tint = tint
        )
    }
}
