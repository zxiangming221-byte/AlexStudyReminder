package com.nous.studyplanner.worker

import android.content.Context
import android.util.Log
import androidx.work.*
import com.nous.studyplanner.data.entity.StudyTask
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit

object ReminderScheduler {

    fun schedule(context: Context, task: StudyTask): String {
        // Skip if reminder disabled
        if (!task.reminderEnabled) {
            Log.d("ReminderScheduler", "Skipping ${task.subject}: reminder disabled")
            return ""
        }

        try {
            val date = LocalDate.parse(task.date)
            val time = LocalTime.parse(task.startTime)
            val scheduledTime = ZonedDateTime.of(date, time, ZoneId.systemDefault())
                .minusMinutes(task.reminderBeforeMin.toLong())

            val delay = scheduledTime.toInstant().toEpochMilli() - System.currentTimeMillis()
            Log.d("ReminderScheduler", "${task.subject} at $scheduledTime, delay=${delay / 60000}min, mode=${getMode(context)}")

            if (delay <= 0) { Log.w("ReminderScheduler", "${task.subject} is past, skip"); return "" }

            val mode = getMode(context)
            val data = Data.Builder()
                .putLong("task_id", task.id)
                .putString("subject", task.subject)
                .putString("time_range", "${task.startTime}-${task.endTime}")
                .putString("mode", mode)
                .build()

            val request = OneTimeWorkRequestBuilder<ReminderWorker>()
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .setInputData(data)
                .addTag("study_reminder_${task.id}")
                .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                .build()

            WorkManager.getInstance(context).enqueue(request)
            Log.d("ReminderScheduler", "Enqueued ${task.subject} id=${request.id}")
            return request.id.toString()
        } catch (e: Exception) {
            Log.e("ReminderScheduler", "Failed to schedule ${task.subject}", e)
            return ""
        }
    }

    fun cancelByTaskId(context: Context, taskId: Long) {
        WorkManager.getInstance(context).cancelAllWorkByTag("study_reminder_$taskId")
    }

    // reminder mode: "sound+vibrate" / "vibrate" / "silent"
    private fun getMode(context: Context): String {
        return context.getSharedPreferences("alex_settings", 0)
            .getString("reminder_mode", "sound+vibrate") ?: "sound+vibrate"
    }
}
