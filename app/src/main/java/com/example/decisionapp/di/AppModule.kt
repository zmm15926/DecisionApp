package com.example.decisionapp.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room
import com.example.decisionapp.data.local.AppDatabase
import com.example.decisionapp.data.local.dao.*
import com.example.decisionapp.data.repository.*
import com.example.decisionapp.domain.repository.*
import com.example.decisionapp.data.local.dao.ChoiceDao
import com.example.decisionapp.data.local.dao.CriterionDao
import com.example.decisionapp.data.local.dao.DecisionDao
import com.example.decisionapp.data.local.dao.ScoreDao
import com.example.decisionapp.data.local.dao.UserDao
import com.example.decisionapp.data.repository.AuthRepositoryImpl
import com.example.decisionapp.data.repository.ChoiceRepositoryImpl
import com.example.decisionapp.data.repository.CriterionRepositoryImpl
import com.example.decisionapp.data.repository.DecisionRepositoryImpl
import com.example.decisionapp.data.repository.ScoreRepositoryImpl
import com.example.decisionapp.domain.repository.AuthRepository
import com.example.decisionapp.domain.repository.ChoiceRepository
import com.example.decisionapp.domain.repository.CriterionRepository
import com.example.decisionapp.domain.repository.DecisionRepository
import com.example.decisionapp.domain.repository.ScoreRepository
import dagger.Provides
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "decision_db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides fun provideUserDao(db: AppDatabase): UserDao = db.userDao()
    @Provides fun provideDecisionDao(db: AppDatabase): DecisionDao = db.decisionDao()
    @Provides fun provideCriterionDao(db: AppDatabase): CriterionDao = db.criterionDao()
    @Provides fun provideChoiceDao(db: AppDatabase): ChoiceDao = db.choiceDao()
    @Provides fun provideScoreDao(db: AppDatabase): ScoreDao = db.scoreDao()

    @Provides
    @Singleton
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> =
        context.dataStore
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds @Singleton
    abstract fun bindDecisionRepository(impl: DecisionRepositoryImpl): DecisionRepository

    @Binds @Singleton
    abstract fun bindCriterionRepository(impl: CriterionRepositoryImpl): CriterionRepository

    @Binds @Singleton
    abstract fun bindChoiceRepository(impl: ChoiceRepositoryImpl): ChoiceRepository

    @Binds @Singleton
    abstract fun bindScoreRepository(impl: ScoreRepositoryImpl): ScoreRepository
}