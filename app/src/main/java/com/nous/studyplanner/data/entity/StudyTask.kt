package com.nous.studyplanner.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "study_tasks",
    foreignKeys = [
        ForeignKey(
            entity = StudyPlan::class,
            parentColumns = ["id"],
            childColumns = ["planId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("planId"), Index("date")]
)
data class StudyTask(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val planId: Long,
    val date: String,           // "2026-06-07"
    val startTime: String,      // "08:00"
    val endTime: String,        // "10:00"
    val subject: String,        // "数学刷题"
    val reminderBeforeMin: Int = 5,
    val isCompleted: Boolean = false,
    val workRequestId: String? = null
)
