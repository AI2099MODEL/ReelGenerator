package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.util.NotificationHelper

class LedgerReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // Re-setup channel on boot
            NotificationHelper.createNotificationChannel(context)
            return
        }

        val notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, (System.currentTimeMillis() % 100000).toInt())
        val title = intent.getStringExtra(EXTRA_TITLE) ?: "Reminder"
        val message = intent.getStringExtra(EXTRA_MESSAGE) ?: "You have a scheduled reminder in MyLyfe."
        val type = intent.getStringExtra(EXTRA_TYPE) ?: "GENERAL"
        val soundName = intent.getStringExtra(EXTRA_SOUND) ?: "Morning Bell"

        // Play default notification sound
        com.example.util.SoundPlayerUtil.playNotificationSound(context, soundName)

        NotificationHelper.showNotification(
            context = context,
            notificationId = notificationId,
            title = title,
            message = message,
            type = type
        )
    }

    companion object {
        const val EXTRA_NOTIFICATION_ID = "extra_notification_id"
        const val EXTRA_TITLE = "extra_title"
        const val EXTRA_MESSAGE = "extra_message"
        const val EXTRA_TYPE = "extra_type"
        const val EXTRA_SOUND = "extra_sound"
    }
}
