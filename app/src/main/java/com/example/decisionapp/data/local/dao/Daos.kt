package com.example.decisionapp.data.local.dao

import androidx.room.*
import androidx.room.Query
import com.example.decisionapp.data.local.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    suspend fun getUserByUsername(username: String): UserEntity?

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun getUserById(id: Long): UserEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertUser(user: UserEntity): Long

    @Update
    suspend fun updateUser(user: UserEntity)
}

@Dao
interface DecisionDao {
    @Query("SELECT * FROM decisions WHERE user_id = :userId ORDER BY updated_at DESC")
    fun getAllDecisions(userId: Long): Flow<List<DecisionEntity>>

    @Query("SELECT * FROM decisions WHERE id = :id LIMIT 1")
    suspend fun getDecisionById(id: Long): DecisionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDecision(decision: DecisionEntity): Long

    @Update
    suspend fun updateDecision(decision: DecisionEntity)

    @Query("DELETE FROM decisions WHERE id = :id")
    suspend fun deleteDecision(id: Long)
}

@Dao
interface CriterionDao {
    @Query("SELECT * FROM criteria WHERE decision_id = :decisionId ORDER BY order_index ASC")
    fun getCriteriaByDecision(decisionId: Long): Flow<List<CriterionEntity>>

    @Query("SELECT * FROM criteria WHERE decision_id = :decisionId ORDER BY order_index ASC")
    suspend fun getCriteriaByDecisionSync(decisionId: Long): List<CriterionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCriterion(criterion: CriterionEntity): Long

    @Update
    suspend fun updateCriterion(criterion: CriterionEntity)

    @Query("DELETE FROM criteria WHERE id = :id")
    suspend fun deleteCriterion(id: Long)

    @Query("DELETE FROM criteria WHERE decision_id = :decisionId")
    suspend fun deleteCriteriaByDecision(decisionId: Long)
}

@Dao
interface ChoiceDao {
    @Query("SELECT * FROM choices WHERE decision_id = :decisionId ORDER BY order_index ASC")
    fun getChoicesByDecision(decisionId: Long): Flow<List<ChoiceEntity>>

    @Query("SELECT * FROM choices WHERE decision_id = :decisionId ORDER BY order_index ASC")
    suspend fun getChoicesByDecisionSync(decisionId: Long): List<ChoiceEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChoice(choice: ChoiceEntity): Long

    @Update
    suspend fun updateChoice(choice: ChoiceEntity)

    @Query("DELETE FROM choices WHERE id = :id")
    suspend fun deleteChoice(id: Long)

    @Query("DELETE FROM choices WHERE decision_id = :decisionId")
    suspend fun deleteChoicesByDecision(decisionId: Long)
}

@Dao
interface ScoreDao {
    @Query("SELECT * FROM scores WHERE choice_id = :choiceId")
    suspend fun getScoresByChoice(choiceId: Long): List<ScoreEntity>

    @Query("""
        SELECT s.* FROM scores s
        INNER JOIN choices c ON s.choice_id = c.id
        WHERE c.decision_id = :decisionId
    """)
    suspend fun getScoresByDecision(decisionId: Long): List<ScoreEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScore(score: ScoreEntity): Long

    @Update
    suspend fun updateScore(score: ScoreEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertScore(score: ScoreEntity)

    @Query("""
        DELETE FROM scores WHERE choice_id IN
        (SELECT id FROM choices WHERE decision_id = :decisionId)
    """)
    suspend fun deleteScoresByDecision(decisionId: Long)
}