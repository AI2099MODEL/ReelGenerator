package com.example.util

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import java.util.Locale

@Composable
fun rememberSpeechToTextLauncher(
    onSpeechResult: (String) -> Unit,
    onListeningStarted: (() -> Unit)? = null,
    onError: ((String) -> Unit)? = null
): () -> Unit {
    val context = LocalContext.current

    val speechRecognizerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            val matches = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val spokenText = matches?.firstOrNull() ?: ""
            if (spokenText.isNotBlank()) {
                onSpeechResult(spokenText)
            } else {
                onError?.invoke("No speech detected")
            }
        } else {
            onError?.invoke("Speech recognition canceled")
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            launchRecognizerIntent(context, speechRecognizerLauncher, onListeningStarted, onError)
        } else {
            Toast.makeText(context, "Microphone permission is required for voice input", Toast.LENGTH_SHORT).show()
            onError?.invoke("Microphone permission denied")
        }
    }

    return {
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            launchRecognizerIntent(context, speechRecognizerLauncher, onListeningStarted, onError)
        } else {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }
}

private fun launchRecognizerIntent(
    context: Context,
    launcher: androidx.activity.result.ActivityResultLauncher<Intent>,
    onListeningStarted: (() -> Unit)?,
    onError: ((String) -> Unit)?
) {
    try {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak title or schedule note...")
        }
        onListeningStarted?.invoke()
        launcher.launch(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "Voice recognition not supported on this device: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        onError?.invoke("Speech recognizer unavailable")
    }
}

class TextToSpeechState(private val context: Context) : TextToSpeech.OnInitListener {
    private var tts: TextToSpeech? = null
    var isInitialized by mutableStateOf(false)
        private set
    var isSpeaking by mutableStateOf(false)
        private set

    init {
        tts = TextToSpeech(context.applicationContext, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(Locale.getDefault())
            if (result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED) {
                isInitialized = true
            }
        }
    }

    fun speak(text: String) {
        if (isInitialized && text.isNotBlank()) {
            stop()
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "Utterance_${System.currentTimeMillis()}")
            isSpeaking = true
        } else if (!isInitialized) {
            Toast.makeText(context, "Voice Output engine initializing...", Toast.LENGTH_SHORT).show()
        }
    }

    fun stop() {
        if (tts?.isSpeaking == true) {
            tts?.stop()
        }
        isSpeaking = false
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
    }
}

@Composable
fun rememberTextToSpeechHelper(): TextToSpeechState {
    val context = LocalContext.current
    val ttsState = remember { TextToSpeechState(context) }

    DisposableEffect(Unit) {
        onDispose {
            ttsState.shutdown()
        }
    }

    return ttsState
}
