package com.example.util

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity
import com.example.receiver.LedgerReminderReceiver

object NotificationHelper {
    const val CHANNEL_ID = "mylyfe_reminders_channel"
    private const val CHANNEL_NAME = "Reminders"
    private const val CHANNEL_DESC = "Notifications for scheduled events and task deadlines"

    const val MUSIC_CHANNEL_ID = "mylyfe_music_channel"
    private const val MUSIC_CHANNEL_NAME = "Music Player"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESC
                enableVibration(true)
                setShowBadge(true)
                val defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                val audioAttributes = AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .build()
                setSound(defaultSound, audioAttributes)
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun createMusicNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                MUSIC_CHANNEL_ID,
                MUSIC_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Music playback controls and track details"
                setShowBadge(false)
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun scheduleReminder(
        context: Context,
        notificationId: Int,
        title: String,
        message: String,
        timestampMillis: Long,
        type: String, // "EVENT" or "TASK"
        notificationSound: String = "Morning Bell"
    ) {
        // If the timestamp is in the past, do not schedule
        if (timestampMillis <= System.currentTimeMillis()) return

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, LedgerReminderReceiver::class.java).apply {
            putExtra(LedgerReminderReceiver.EXTRA_NOTIFICATION_ID, notificationId)
            putExtra(LedgerReminderReceiver.EXTRA_TITLE, title)
            putExtra(LedgerReminderReceiver.EXTRA_MESSAGE, message)
            putExtra(LedgerReminderReceiver.EXTRA_TYPE, type)
            putExtra(LedgerReminderReceiver.EXTRA_SOUND, notificationSound)
        }

        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId,
            intent,
            flags
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timestampMillis, pendingIntent)
                } else {
                    alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timestampMillis, pendingIntent)
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timestampMillis, pendingIntent)
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, timestampMillis, pendingIntent)
            }
        } catch (_: Exception) {
            // Fallback for security exceptions on exact alarms
            try {
                alarmManager.set(AlarmManager.RTC_WAKEUP, timestampMillis, pendingIntent)
            } catch (_: Exception) {}
        }
    }

    fun cancelReminder(context: Context, notificationId: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, LedgerReminderReceiver::class.java)
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId,
            intent,
            flags
        )
        alarmManager.cancel(pendingIntent)
    }

    fun showNotification(
        context: Context,
        notificationId: Int,
        title: String,
        message: String,
        type: String
    ) {
        createNotificationChannel(context)

        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            openAppIntent,
            flags
        )

        // Format title cleanly without MYLYFE prefix. E.g. "Nitin Birthday today"
        val cleanTitle = title.trim()
        val formattedTitle = if (type == "EVENT" || cleanTitle.contains("Birthday", ignoreCase = true) || cleanTitle.contains("Anniversary", ignoreCase = true)) {
            if (cleanTitle.contains("today", ignoreCase = true)) cleanTitle else "$cleanTitle today"
        } else {
            cleanTitle
        }

        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle(formattedTitle)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setSound(soundUri)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        try {
            val notificationManagerCompat = NotificationManagerCompat.from(context)
            notificationManagerCompat.notify(notificationId, builder.build())
        } catch (_: SecurityException) {
            // Handled when POST_NOTIFICATIONS is not granted
        }
    }

    fun showMusicNotification(
        context: Context,
        trackTitle: String,
        artistName: String,
        isPlaying: Boolean
    ) {
        createMusicNotificationChannel(context)

        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            9999,
            openAppIntent,
            flags
        )

        val builder = NotificationCompat.Builder(context, MUSIC_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentTitle(trackTitle)
            .setContentText(if (artistName.isBlank()) "Playing in Music Player" else artistName)
            .setSubText("🎵 Music Notification")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(isPlaying)
            .setContentIntent(pendingIntent)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

        try {
            val notificationManagerCompat = NotificationManagerCompat.from(context)
            notificationManagerCompat.notify(9999, builder.build())
        } catch (_: SecurityException) {}
    }

    fun cancelMusicNotification(context: Context) {
        try {
            val notificationManagerCompat = NotificationManagerCompat.from(context)
            notificationManagerCompat.cancel(9999)
        } catch (_: Exception) {}
    }
}
