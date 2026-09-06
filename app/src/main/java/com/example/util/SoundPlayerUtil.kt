package com.example.util

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri

object SoundPlayerUtil {
    private var mediaPlayer: MediaPlayer? = null

    fun playNotificationSound(context: Context, soundName: String) {
        try {
            stopSound()
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
            if (audioManager != null) {
                val maxVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM)
                // Set high alarm volume for clear notification
                audioManager.setStreamVolume(AudioManager.STREAM_ALARM, (maxVol * 0.9f).toInt(), 0)
            }

            val soundUri: Uri = when (soundName) {
                "Digital Alarm" -> RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                "Bright Harp", "Morning Birds" -> RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
                "Zen Bell", "Gentle Chime" -> RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                else -> RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            }

            mediaPlayer = MediaPlayer().apply {
                setDataSource(context, soundUri)
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                prepare()
                start()
            }
        } catch (_: Exception) {}
    }

    fun stopSound() {
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null
        } catch (_: Exception) {}
    }
}
