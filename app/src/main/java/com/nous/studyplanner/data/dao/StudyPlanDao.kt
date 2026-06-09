package com.nous.studyplanner.data.dao

import androidx.room.*
import com.nous.studyplanner.data.entity.StudyPlan
import kotlinx.coroutines.flow.Flow

@Dao
interface StudyPlanDao {
    @Query("SELECT * FROM study_plans ORDER BY createdAt DESC")
    fun getAllPlans(): Flow<List<StudyPlan>>

    @Query("SELECT * FROM study_plans WHERE id = :id")
    suspend fun getPlanById(id: Long): StudyPlan?

    @Insert
    suspend fun insert(plan: StudyPlan): Long

    @Delete
    suspend fun delete(plan: StudyPlan)

    @Query("DELETE FROM study_plans WHERE id = :id")
    suspend fun deleteById(id: Long)
}
