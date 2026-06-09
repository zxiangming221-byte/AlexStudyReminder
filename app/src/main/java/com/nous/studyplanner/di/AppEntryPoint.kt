package com.nous.studyplanner.di

import com.nous.studyplanner.data.dao.StudyPlanDao
import com.nous.studyplanner.data.dao.StudyTaskDao
import com.nous.studyplanner.parser.PlanParser
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface AppEntryPoint {
    fun studyPlanDao(): StudyPlanDao
    fun studyTaskDao(): StudyTaskDao
    fun planParser(): PlanParser
}
