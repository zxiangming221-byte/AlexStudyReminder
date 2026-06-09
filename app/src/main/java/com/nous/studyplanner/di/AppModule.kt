package com.nous.studyplanner.di

import android.content.Context
import androidx.room.Room
import com.nous.studyplanner.data.AppDatabase
import com.nous.studyplanner.data.dao.StudyPlanDao
import com.nous.studyplanner.data.dao.StudyTaskDao
import com.nous.studyplanner.parser.PlanParser
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "study_planner.db"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideStudyPlanDao(db: AppDatabase): StudyPlanDao = db.studyPlanDao()

    @Provides
    fun provideStudyTaskDao(db: AppDatabase): StudyTaskDao = db.studyTaskDao()

    @Provides
    @Singleton
    fun providePlanParser(): PlanParser = PlanParser()
}
