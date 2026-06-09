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

    fun schedule(context: Context, task: StudyTask, beforeMinutes: Int = 5): String {
        try {
            val date = LocalDate.parse(task.date)
            val time = LocalTime.parse(task.startTime)
            val scheduledTime = ZonedDateTime.of(
                date, time, ZoneId.systemDefault()
            ).minusMinutes(beforeMinutes.toLong())

            val delay = scheduledTime.toInstant().toEpochMilli() - System.currentTimeMillis()

            Log.d("ReminderScheduler", "Scheduling: ${task.subject} at $scheduledTime, delay=${delay}ms (${delay / 60000}min)")

            // Skip if already past
            if (delay <= 0) {
                Log.w("ReminderScheduler", "Task ${task.subject} is in the past, skipping")
                return ""
            }

            val data = Data.Builder()
                .putLong("task_id", task.id)
                .putString("subject", task.subject)
                .putString("time_range", "${task.startTime}-${task.endTime}")
                .build()

            val request = OneTimeWorkRequestBuilder<ReminderWorker>()
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .setInputData(data)
                .addTag("study_reminder_${task.id}")
                .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                .build()

            WorkManager.getInstance(context).enqueue(request)
            Log.d("ReminderScheduler", "Enqueued: ${task.subject}, requestId=${request.id}")
            return request.id.toString()
        } catch (e: Exception) {
            return ""
        }
    }

    fun cancelByTaskId(context: Context, taskId: Long) {
        WorkManager.getInstance(context)
            .cancelAllWorkByTag("study_reminder_$taskId")
    }

    fun cancelAll(context: Context) {
        WorkManager.getInstance(context)
            .cancelAllWorkByTag("study_reminder")
    }
}
