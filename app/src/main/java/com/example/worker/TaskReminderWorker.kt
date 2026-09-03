package com.example.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.data.AppDatabase
import com.example.util.NotificationHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class TaskReminderWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val taskId = inputData.getLong(KEY_TASK_ID, -1L)
        if (taskId == -1L) return@withContext Result.failure()

        val db = AppDatabase.getDatabase(context)
        val task = db.taskDao().getTaskById(taskId)
        
        // Check if task exists, is not completed, and still has notifyMe = true
        if (task != null && !task.isCompleted && task.notifyMe) {
            val title = task.title
            val message = if (task.description.isNotBlank()) task.description else "Task due in My Checklist"
            
            NotificationHelper.showNotification(
                context = context,
                notificationId = task.notificationScheduledId.takeIf { it != 0 } ?: taskId.toInt(),
                title = title,
                message = message,
                type = "TASK"
            )
        }
        
        Result.success()
    }

    companion object {
        const val KEY_TASK_ID = "key_task_id"
    }
}
