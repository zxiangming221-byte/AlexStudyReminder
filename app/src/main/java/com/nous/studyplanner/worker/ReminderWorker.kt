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
        showNotification(subject, timeRange)
        return Result.success()
    }

    private fun showNotification(subject: String, timeRange: String) {
        // Check permission
        if (ActivityCompat.checkSelfPermission(
                applicationContext, Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) return

        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pending = PendingIntent.getActivity(
            applicationContext, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val notification = NotificationCompat.Builder(
            applicationContext, StudyPlannerApp.CHANNEL_ID
        )
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle(subject)
            .setContentText("⏰ $timeRange — 到时间了，开始学习吧！")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("⏰ $timeRange\n\n$subject\n到时间了，开始学习吧！"))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .setContentIntent(pending)
            .setSound(soundUri)
            .setVibrate(longArrayOf(0, 400, 200, 400))
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .build()

        NotificationManagerCompat.from(applicationContext)
            .notify(subject.hashCode(), notification)
    }
}
