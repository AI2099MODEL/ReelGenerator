package com.example.util

import android.content.Context
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.worker.TaskReminderWorker
import java.util.concurrent.TimeUnit

object WorkManagerHelper {

    fun scheduleTaskReminder(
        context: Context,
        taskId: Long,
        scheduledTimestamp: Long
    ) {
        val delayMs = scheduledTimestamp - System.currentTimeMillis()
        if (delayMs <= 0) return // Already due or past

        val data = Data.Builder()
            .putLong(TaskReminderWorker.KEY_TASK_ID, taskId)
            .build()

        val workRequest = OneTimeWorkRequestBuilder<TaskReminderWorker>()
            .setInitialDelay(delayMs, TimeUnit.MILLISECONDS)
            .setInputData(data)
            .addTag("task_reminder_$taskId")
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            "task_reminder_work_$taskId",
            ExistingWorkPolicy.REPLACE,
            workRequest
        )
    }

    fun cancelTaskReminder(context: Context, taskId: Long) {
        WorkManager.getInstance(context).cancelUniqueWork("task_reminder_work_$taskId")
    }
}
