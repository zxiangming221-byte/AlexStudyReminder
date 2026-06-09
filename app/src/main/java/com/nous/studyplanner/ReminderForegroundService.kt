package com.nous.studyplanner

import android.app.*
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.room.Room
import com.nous.studyplanner.data.AppDatabase
import com.nous.studyplanner.data.entity.StudyTask
import kotlinx.coroutines.*

class ReminderForegroundService : Service() {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private lateinit var db: AppDatabase
    private val firedTasks = mutableSetOf<Long>() // prevent duplicate fires

    override fun onCreate() {
        super.onCreate()
        db = Room.databaseBuilder(this, AppDatabase::class.java, "study_planner.db")
            .fallbackToDestructiveMigration().build()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIF_ID, buildPersistentNotification())
        Log.d("ReminderService", "Foreground service started")

        scope.launch {
            while (isActive) {
                checkReminders()
                delay(30_000L) // 30-second tick
            }
        }
        return START_STICKY
    }

    private suspend fun checkReminders() {
        try {
            val today = java.time.LocalDate.now().toString()
            val now = java.time.LocalTime.now()
            val nowStr = String.format("%02d:%02d", now.hour, now.minute)

            val tasks = db.studyTaskDao().getTasksByDate(today)
            val mode = getSharedPreferences("alex_settings", 0)
                .getString("reminder_mode", "sound+vibrate") ?: "sound+vibrate"

            for (task in tasks) {
                if (!task.reminderEnabled) continue
                if (task.isCompleted) continue
                if (task.startTime != nowStr) continue
                if (firedTasks.contains(task.id)) continue

                firedTasks.add(task.id)
                Log.d("ReminderService", "Firing: ${task.subject} at $nowStr mode=$mode")
                showReminder(task, mode)
            }

            // Clean up fired tasks from past hours
            if (now.minute == 0) firedTasks.clear()
        } catch (e: Exception) {
            Log.e("ReminderService", "Check failed", e)
        }
    }

    private fun showReminder(task: StudyTask, mode: String) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pending = PendingIntent.getActivity(this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val builder = NotificationCompat.Builder(this, StudyPlannerApp.CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle(task.subject)
            .setContentText("⏰ ${task.startTime}-${task.endTime} — 到时间了！")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("⏰ ${task.startTime}-${task.endTime}\n\n${task.subject}\n开始学习吧！"))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .setContentIntent(pending)

        when (mode) {
            "sound+vibrate" -> {
                builder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                builder.setVibrate(longArrayOf(0, 500, 300, 500))
                builder.setDefaults(NotificationCompat.DEFAULT_ALL)
            }
            "vibrate" -> {
                builder.setVibrate(longArrayOf(0, 500, 300, 500))
                builder.setDefaults(NotificationCompat.DEFAULT_VIBRATE)
            }
            "silent" -> { /* just the popup */ }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                == android.content.pm.PackageManager.PERMISSION_GRANTED
            ) {
                NotificationManagerCompat.from(this).notify(task.id.toInt(), builder.build())
            }
        } else {
            NotificationManagerCompat.from(this).notify(task.id.toInt(), builder.build())
        }
    }

    private fun buildPersistentNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pending = PendingIntent.getActivity(this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        // Create a separate low-importance channel for the persistent notification
        val channelId = "service_persistent"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "后台服务", NotificationManager.IMPORTANCE_LOW).apply {
                description = "保持提醒服务运行"
                setShowBadge(false)
            }
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }

        return NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle("AlexStudyReminder")
            .setContentText("正在运行 · 到点自动提醒")
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(pending)
            .build()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }

    companion object {
        private const val NOTIF_ID = 9999

        fun start(ctx: Context) {
            val intent = Intent(ctx, ReminderForegroundService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                ctx.startForegroundService(intent)
            } else {
                ctx.startService(intent)
            }
        }

        fun stop(ctx: Context) {
            ctx.stopService(Intent(ctx, ReminderForegroundService::class.java))
        }
    }
}
