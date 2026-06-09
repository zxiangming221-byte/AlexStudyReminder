package com.nous.studyplanner

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class StudyPlannerApp : Application(), Configuration.Provider {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setMinimumLoggingLevel(android.util.Log.INFO)
            .build()

    private fun createNotificationChannel() {
        try {
            val channel = NotificationChannel(
                CHANNEL_ID, "学习提醒", NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "定时提醒你开始学习"
                enableVibration(true)
                setShowBadge(true)
            }
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        } catch (_: Exception) {}
    }

    companion object {
        const val CHANNEL_ID = "study_reminder"
    }
}
