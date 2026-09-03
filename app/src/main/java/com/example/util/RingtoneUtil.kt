package com.example.util

import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast

object RingtoneUtil {
    fun setRingtone(context: Context, uriString: String) {
        if (!Settings.System.canWrite(context)) {
            val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS)
            intent.data = Uri.parse("package:" + context.packageName)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            Toast.makeText(context, "Please allow permission to modify system settings to set ringtones.", Toast.LENGTH_LONG).show()
            return
        }

        try {
            val uri = Uri.parse(uriString)
            RingtoneManager.setActualDefaultRingtoneUri(context, RingtoneManager.TYPE_RINGTONE, uri)
            Toast.makeText(context, "Ringtone set successfully!", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Failed to set ringtone: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
