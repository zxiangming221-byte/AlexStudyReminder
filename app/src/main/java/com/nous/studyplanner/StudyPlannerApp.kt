package com.nous.studyplanner

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.util.Log
import androidx.work.Configuration
import androidx.work.WorkManager
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class StudyPlannerApp : Application(), Configuration.Provider {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        // Force-init WorkManager so scheduling never silently fails
        try {
            WorkManager.getInstance(this)
        } catch (e: Exception) {
            Log.e("StudyPlannerApp", "WorkManager init failed, retrying manual", e)
            WorkManager.initialize(this, workManagerConfiguration)
        }
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setMinimumLoggingLevel(Log.DEBUG)
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
        } catch (e: Exception) {
            Log.e("StudyPlannerApp", "Channel creation failed", e)
        }
    }

    companion object {
        const val CHANNEL_ID = "study_reminder"
    }
}
