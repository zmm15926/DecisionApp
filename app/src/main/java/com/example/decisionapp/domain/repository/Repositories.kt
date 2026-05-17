package com.example.decisionapp.domain.repository

import com.example.decisionapp.domain.model.Choice
import com.example.decisionapp.domain.model.Criterion
import com.example.decisionapp.domain.model.Decision
import com.example.decisionapp.domain.model.DecisionWithDetails
import com.example.decisionapp.domain.model.Score
import com.example.decisionapp.domain.model.User
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    suspend fun register(username: String, password: String): Result<User>
    suspend fun login(username: String, password: String): Result<User>
    suspend fun logout()
    fun getCurrentUser(): Flow<User?>
    suspend fun isLoggedIn(): Boolean
}

interface DecisionRepository {
    fun getAllDecisions(userId: Long): Flow<List<Decision>>
    suspend fun getDecisionById(id: Long): Decision?
    suspend fun insertDecision(decision: Decision): Long
    suspend fun updateDecision(decision: Decision)
    suspend fun deleteDecision(id: Long)
    suspend fun getDecisionWithDetails(id: Long): DecisionWithDetails?
}

interface CriterionRepository {
    fun getCriteriaByDecision(decisionId: Long): Flow<List<Criterion>>
    suspend fun insertCriterion(criterion: Criterion): Long
    suspend fun updateCriterion(criterion: Criterion)
    suspend fun deleteCriterion(id: Long)
    suspend fun deleteCriteriaByDecision(decisionId: Long)
}

interface ChoiceRepository {
    fun getChoicesByDecision(decisionId: Long): Flow<List<Choice>>
    suspend fun insertChoice(choice: Choice): Long
    suspend fun updateChoice(choice: Choice)
    suspend fun deleteChoice(id: Long)
    suspend fun deleteChoicesByDecision(decisionId: Long)
}

interface ScoreRepository {
    suspend fun getScoresByChoice(choiceId: Long): List<Score>
    suspend fun getScoresByDecision(decisionId: Long): List<Score>
    suspend fun insertScore(score: Score): Long
    suspend fun updateScore(score: Score)
    suspend fun upsertScore(score: Score)
    suspend fun deleteScoresByDecision(decisionId: Long)
}