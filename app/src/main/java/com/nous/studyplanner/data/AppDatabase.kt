package com.nous.studyplanner.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.nous.studyplanner.data.dao.StudyPlanDao
import com.nous.studyplanner.data.dao.StudyTaskDao
import com.nous.studyplanner.data.entity.StudyPlan
import com.nous.studyplanner.data.entity.StudyTask

@Database(
    entities = [StudyPlan::class, StudyTask::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun studyPlanDao(): StudyPlanDao
    abstract fun studyTaskDao(): StudyTaskDao
}
