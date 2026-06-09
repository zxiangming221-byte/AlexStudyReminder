package com.nous.studyplanner.worker

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.RingtoneManager
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.nous.studyplanner.MainActivity
import com.nous.studyplanner.StudyPlannerApp

class ReminderWorker(ctx: Context, params: WorkerParameters) : Worker(ctx, params) {

    override fun doWork(): Result {
        val subject = inputData.getString("subject") ?: "学习时间到了"
        val timeRange = inputData.getString("time_range") ?: ""
        val mode = inputData.getString("mode") ?: "sound+vibrate"
        showNotification(subject, timeRange, mode)
        return Result.success()
    }

    private fun showNotification(subject: String, timeRange: String, mode: String) {
        if (ActivityCompat.checkSelfPermission(applicationContext, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) return

        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pending = PendingIntent.getActivity(
            applicationContext, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(applicationContext, StudyPlannerApp.CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle(subject)
            .setContentText("⏰ $timeRange — 到时间了！")
            .setStyle(NotificationCompat.BigTextStyle().bigText("⏰ $timeRange\n\n$subject\n开始学习吧！"))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .setContentIntent(pending)

        // Apply mode
        when (mode) {
            "sound+vibrate" -> {
                builder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                builder.setVibrate(longArrayOf(0, 400, 200, 400))
                builder.setDefaults(NotificationCompat.DEFAULT_ALL)
            }
            "vibrate" -> {
                builder.setVibrate(longArrayOf(0, 400, 200, 400))
                builder.setDefaults(NotificationCompat.DEFAULT_VIBRATE)
            }
            "silent" -> {
                // No sound, no vibration - just the notification
            }
        }

        NotificationManagerCompat.from(applicationContext)
            .notify(subject.hashCode(), builder.build())
    }
}
