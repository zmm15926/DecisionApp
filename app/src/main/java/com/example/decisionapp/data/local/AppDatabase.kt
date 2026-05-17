package com.example.decisionapp.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.decisionapp.data.local.dao.ChoiceDao
import com.example.decisionapp.data.local.dao.CriterionDao
import com.example.decisionapp.data.local.dao.DecisionDao
import com.example.decisionapp.data.local.dao.ScoreDao
import com.example.decisionapp.data.local.dao.UserDao
import com.example.decisionapp.data.local.entity.ChoiceEntity
import com.example.decisionapp.data.local.entity.CriterionEntity
import com.example.decisionapp.data.local.entity.DecisionEntity
import com.example.decisionapp.data.local.entity.ScoreEntity
import com.example.decisionapp.data.local.entity.UserEntity


@Database(
    entities = [
        UserEntity::class,
        DecisionEntity::class,
        CriterionEntity::class,
        ChoiceEntity::class,
        ScoreEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun decisionDao(): DecisionDao
    abstract fun criterionDao(): CriterionDao
    abstract fun choiceDao(): ChoiceDao
    abstract fun scoreDao(): ScoreDao
}