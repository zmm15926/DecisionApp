package com.example.decisionapp.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import com.example.decisionapp.data.local.*
import com.example.decisionapp.data.local.dao.*
import com.example.decisionapp.data.local.entity.UserEntity
import com.example.decisionapp.domain.model.*
import com.example.decisionapp.domain.repository.*
import com.example.decisionapp.data.local.dao.ChoiceDao
import com.example.decisionapp.data.local.dao.CriterionDao
import com.example.decisionapp.data.local.dao.DecisionDao
import com.example.decisionapp.data.local.dao.ScoreDao
import com.example.decisionapp.data.local.dao.UserDao
import com.example.decisionapp.data.local.toDomain
import com.example.decisionapp.data.local.toEntity
import com.example.decisionapp.domain.model.Choice
import com.example.decisionapp.domain.model.Criterion
import com.example.decisionapp.domain.model.Decision
import com.example.decisionapp.domain.model.DecisionWithDetails
import com.example.decisionapp.domain.model.Score
import com.example.decisionapp.domain.model.User
import com.example.decisionapp.domain.repository.AuthRepository
import com.example.decisionapp.domain.repository.ChoiceRepository
import com.example.decisionapp.domain.repository.CriterionRepository
import com.example.decisionapp.domain.repository.DecisionRepository
import com.example.decisionapp.domain.repository.ScoreRepository
import kotlinx.coroutines.flow.*
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.text.get

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val userDao: UserDao,
    private val dataStore: DataStore<androidx.datastore.preferences.core.Preferences>
) : AuthRepository {

    companion object {
        val USER_ID_KEY = longPreferencesKey("current_user_id")
        val USERNAME_KEY = stringPreferencesKey("current_username")
    }

    private fun hashPassword(password: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(password.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    override suspend fun register(username: String, password: String): Result<User> {
        return try {
            val existing = userDao.getUserByUsername(username)
            if (existing != null) return Result.failure(Exception("Пользователь с таким именем уже существует"))
            val hash = hashPassword(password)
            val id = userDao.insertUser(UserEntity(username = username, passwordHash = hash))
            val user = User(id = id, username = username, passwordHash = hash)
            saveSession(user)
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun login(username: String, password: String): Result<User> {
        return try {
            val entity = userDao.getUserByUsername(username)
                ?: return Result.failure(Exception("Пользователь не найден"))
            val hash = hashPassword(password)
            if (entity.passwordHash != hash) return Result.failure(Exception("Неверный пароль"))
            val user = entity.toDomain()
            saveSession(user)
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun logout() {
        dataStore.edit { it.clear() }
    }

    override fun getCurrentUser(): Flow<User?> {
        return dataStore.data.map { prefs ->
            val id = prefs[USER_ID_KEY] ?: return@map null
            val username = prefs[USERNAME_KEY] ?: return@map null
            User(id = id, username = username, passwordHash = "")
        }
    }

    override suspend fun isLoggedIn(): Boolean {
        return dataStore.data.first()[USER_ID_KEY] != null
    }

    private suspend fun saveSession(user: User) {
        dataStore.edit { prefs ->
            prefs[USER_ID_KEY] = user.id
            prefs[USERNAME_KEY] = user.username
        }
    }
}

@Singleton
class DecisionRepositoryImpl @Inject constructor(
    private val decisionDao: DecisionDao,
    private val criterionDao: CriterionDao,
    private val choiceDao: ChoiceDao,
    private val scoreDao: ScoreDao
) : DecisionRepository {
    override fun getAllDecisions(userId: Long): Flow<List<Decision>> =
        decisionDao.getAllDecisions(userId).map { list -> list.map { it.toDomain() } }

    override suspend fun getDecisionById(id: Long): Decision? =
        decisionDao.getDecisionById(id)?.toDomain()

    override suspend fun insertDecision(decision: Decision): Long =
        decisionDao.insertDecision(decision.toEntity())

    override suspend fun updateDecision(decision: Decision) =
        decisionDao.updateDecision(decision.copy(updatedAt = System.currentTimeMillis()).toEntity())

    override suspend fun deleteDecision(id: Long) = decisionDao.deleteDecision(id)

    override suspend fun getDecisionWithDetails(id: Long): DecisionWithDetails? {
        val decision = decisionDao.getDecisionById(id)?.toDomain() ?: return null
        val criteria = criterionDao.getCriteriaByDecisionSync(id).map { it.toDomain() }
        val choices = choiceDao.getChoicesByDecisionSync(id).map { it.toDomain() }
        val scores = scoreDao.getScoresByDecision(id).map { it.toDomain() }
        return DecisionWithDetails(decision, criteria, choices, scores)
    }
}

@Singleton
class CriterionRepositoryImpl @Inject constructor(private val criterionDao: CriterionDao) :
    CriterionRepository {
    override fun getCriteriaByDecision(decisionId: Long): Flow<List<Criterion>> =
        criterionDao.getCriteriaByDecision(decisionId).map { it.map { e -> e.toDomain() } }

    override suspend fun insertCriterion(criterion: Criterion): Long =
        criterionDao.insertCriterion(criterion.toEntity())

    override suspend fun updateCriterion(criterion: Criterion) =
        criterionDao.updateCriterion(criterion.toEntity())

    override suspend fun deleteCriterion(id: Long) = criterionDao.deleteCriterion(id)

    override suspend fun deleteCriteriaByDecision(decisionId: Long) =
        criterionDao.deleteCriteriaByDecision(decisionId)
}

@Singleton
class ChoiceRepositoryImpl @Inject constructor(private val choiceDao: ChoiceDao) :
    ChoiceRepository {
    override fun getChoicesByDecision(decisionId: Long): Flow<List<Choice>> =
        choiceDao.getChoicesByDecision(decisionId).map { it.map { e -> e.toDomain() } }

    override suspend fun insertChoice(choice: Choice): Long =
        choiceDao.insertChoice(choice.toEntity())

    override suspend fun updateChoice(choice: Choice) = choiceDao.updateChoice(choice.toEntity())

    override suspend fun deleteChoice(id: Long) = choiceDao.deleteChoice(id)

    override suspend fun deleteChoicesByDecision(decisionId: Long) =
        choiceDao.deleteChoicesByDecision(decisionId)
}

@Singleton
class ScoreRepositoryImpl @Inject constructor(private val scoreDao: ScoreDao) : ScoreRepository {
    override suspend fun getScoresByChoice(choiceId: Long): List<Score> =
        scoreDao.getScoresByChoice(choiceId).map { it.toDomain() }

    override suspend fun getScoresByDecision(decisionId: Long): List<Score> =
        scoreDao.getScoresByDecision(decisionId).map { it.toDomain() }

    override suspend fun insertScore(score: Score): Long =
        scoreDao.insertScore(score.toEntity())

    override suspend fun updateScore(score: Score) = scoreDao.updateScore(score.toEntity())

    override suspend fun upsertScore(score: Score) = scoreDao.upsertScore(score.toEntity())

    override suspend fun deleteScoresByDecision(decisionId: Long) =
        scoreDao.deleteScoresByDecision(decisionId)
}