package com.nous.studyplanner.data.dao

import androidx.room.*
import com.nous.studyplanner.data.entity.StudyTask
import kotlinx.coroutines.flow.Flow

@Dao
interface StudyTaskDao {
    @Query("SELECT * FROM study_tasks WHERE planId = :planId ORDER BY date, startTime")
    fun getTasksByPlanId(planId: Long): Flow<List<StudyTask>>

    @Query("SELECT * FROM study_tasks WHERE planId = :planId ORDER BY date, startTime")
    suspend fun getTasksByPlanIdAsList(planId: Long): List<StudyTask>

    @Query("SELECT * FROM study_tasks WHERE date = :date ORDER BY startTime")
    suspend fun getTasksByDate(date: String): List<StudyTask>

    @Query("SELECT * FROM study_tasks WHERE id = :id")
    suspend fun getTaskById(id: Long): StudyTask?

    @Insert
    suspend fun insertAll(tasks: List<StudyTask>)

    @Update
    suspend fun update(task: StudyTask)

    @Query("UPDATE study_tasks SET isCompleted = :completed WHERE id = :id")
    suspend fun setCompleted(id: Long, completed: Boolean)

    @Query("UPDATE study_tasks SET workRequestId = :requestId WHERE id = :id")
    suspend fun setWorkRequestId(id: Long, requestId: String)

    @Query("DELETE FROM study_tasks WHERE planId = :planId")
    suspend fun deleteByPlanId(planId: Long)
}
