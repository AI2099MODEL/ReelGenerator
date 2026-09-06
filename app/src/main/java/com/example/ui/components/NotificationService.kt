package com.example.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

class NotificationService {
    var activeNotification by mutableStateOf<AppNotification?>(null)
        private set

    fun show(title: String, message: String, type: NotificationType = NotificationType.INFO) {
        activeNotification = AppNotification(title = title, message = message, type = type)
    }

    fun dismiss() {
        activeNotification = null
    }
}

val LocalNotificationService = compositionLocalOf<NotificationService> {
    error("No NotificationService provided")
}
