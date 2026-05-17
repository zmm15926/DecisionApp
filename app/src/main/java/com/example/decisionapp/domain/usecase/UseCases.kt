package com.example.decisionapp.domain.usecase

import com.example.decisionapp.domain.model.Choice
import com.example.decisionapp.domain.model.ChoiceResult
import com.example.decisionapp.domain.model.Criterion
import com.example.decisionapp.domain.model.Decision
import com.example.decisionapp.domain.model.DecisionResult
import com.example.decisionapp.domain.model.DecisionWithDetails
import com.example.decisionapp.domain.model.Score
import com.example.decisionapp.domain.model.User
import com.example.decisionapp.domain.repository.AuthRepository
import com.example.decisionapp.domain.repository.ChoiceRepository
import com.example.decisionapp.domain.repository.CriterionRepository
import com.example.decisionapp.domain.repository.DecisionRepository
import com.example.decisionapp.domain.repository.ScoreRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

// Auth Use Cases
class LoginUseCase @Inject constructor(private val authRepository: AuthRepository) {
    suspend operator fun invoke(username: String, password: String): Result<User> {
        if (username.isBlank()) return Result.failure(IllegalArgumentException("Логин не может быть пустым"))
        if (password.length < 6) return Result.failure(IllegalArgumentException("Пароль минимум 6 символов"))
        return authRepository.login(username.trim(), password)
    }
}

class RegisterUseCase @Inject constructor(private val authRepository: AuthRepository) {
    suspend operator fun invoke(username: String, password: String, confirmPassword: String): Result<User> {
        if (username.isBlank()) return Result.failure(IllegalArgumentException("Логин не может быть пустым"))
        if (username.length < 3) return Result.failure(IllegalArgumentException("Логин минимум 3 символа"))
        if (password.length < 6) return Result.failure(IllegalArgumentException("Пароль минимум 6 символов"))
        if (password != confirmPassword) return Result.failure(IllegalArgumentException("Пароли не совпадают"))
        return authRepository.register(username.trim(), password)
    }
}

class LogoutUseCase @Inject constructor(private val authRepository: AuthRepository) {
    suspend operator fun invoke() = authRepository.logout()
}

class GetCurrentUserUseCase @Inject constructor(private val authRepository: AuthRepository) {
    operator fun invoke(): Flow<User?> = authRepository.getCurrentUser()
}

// Decision Use Cases
class GetDecisionsUseCase @Inject constructor(private val decisionRepository: DecisionRepository) {
    operator fun invoke(userId: Long): Flow<List<Decision>> = decisionRepository.getAllDecisions(userId)
}

class CreateDecisionUseCase @Inject constructor(
    private val decisionRepository: DecisionRepository
) {
    suspend operator fun invoke(userId: Long, title: String, description: String = ""): Result<Long> {
        if (title.isBlank()) return Result.failure(IllegalArgumentException("Название решения не может быть пустым"))
        val decision =
            Decision(userId = userId, title = title.trim(), description = description.trim())
        return Result.success(decisionRepository.insertDecision(decision))
    }
}

class DeleteDecisionUseCase @Inject constructor(
    private val decisionRepository: DecisionRepository,
    private val criterionRepository: CriterionRepository,
    private val choiceRepository: ChoiceRepository,
    private val scoreRepository: ScoreRepository
) {
    suspend operator fun invoke(decisionId: Long) {
        scoreRepository.deleteScoresByDecision(decisionId)
        criterionRepository.deleteCriteriaByDecision(decisionId)
        choiceRepository.deleteChoicesByDecision(decisionId)
        decisionRepository.deleteDecision(decisionId)
    }
}

class GetDecisionWithDetailsUseCase @Inject constructor(
    private val decisionRepository: DecisionRepository
) {
    suspend operator fun invoke(decisionId: Long): DecisionWithDetails? =
        decisionRepository.getDecisionWithDetails(decisionId)
}

// Criterion Use Cases
class GetCriteriaUseCase @Inject constructor(private val criterionRepository: CriterionRepository) {
    operator fun invoke(decisionId: Long): Flow<List<Criterion>> =
        criterionRepository.getCriteriaByDecision(decisionId)
}

class SaveCriterionUseCase @Inject constructor(private val criterionRepository: CriterionRepository) {
    suspend operator fun invoke(criterion: Criterion): Result<Long> {
        if (criterion.name.isBlank()) return Result.failure(IllegalArgumentException("Название критерия не может быть пустым"))
        return if (criterion.id == 0L) {
            Result.success(criterionRepository.insertCriterion(criterion))
        } else {
            criterionRepository.updateCriterion(criterion)
            Result.success(criterion.id)
        }
    }
}

class DeleteCriterionUseCase @Inject constructor(private val criterionRepository: CriterionRepository) {
    suspend operator fun invoke(id: Long) = criterionRepository.deleteCriterion(id)
}

// Choice Use Cases
class GetChoicesUseCase @Inject constructor(private val choiceRepository: ChoiceRepository) {
    operator fun invoke(decisionId: Long): Flow<List<Choice>> =
        choiceRepository.getChoicesByDecision(decisionId)
}

class SaveChoiceUseCase @Inject constructor(private val choiceRepository: ChoiceRepository) {
    suspend operator fun invoke(choice: Choice): Result<Long> {
        if (choice.name.isBlank()) return Result.failure(IllegalArgumentException("Название варианта не может быть пустым"))
        return if (choice.id == 0L) {
            Result.success(choiceRepository.insertChoice(choice))
        } else {
            choiceRepository.updateChoice(choice)
            Result.success(choice.id)
        }
    }
}

class DeleteChoiceUseCase @Inject constructor(private val choiceRepository: ChoiceRepository) {
    suspend operator fun invoke(id: Long) = choiceRepository.deleteChoice(id)
}

// Score Use Cases
class UpsertScoreUseCase @Inject constructor(private val scoreRepository: ScoreRepository) {
    suspend operator fun invoke(score: Score) = scoreRepository.upsertScore(score)
}

// Calculate Result Use Case
class CalculateDecisionResultUseCase @Inject constructor(
    private val decisionRepository: DecisionRepository,
    private val scoreRepository: ScoreRepository
) {
    suspend operator fun invoke(decisionId: Long): Result<DecisionResult> {
        val details = decisionRepository.getDecisionWithDetails(decisionId)
            ?: return Result.failure(Exception("Решение не найдено"))

        val scores = scoreRepository.getScoresByDecision(decisionId)
        val totalWeight = details.criteria.sumOf { it.weight.toDouble() }.toFloat()
            .takeIf { it > 0f } ?: 1f

        val choiceResults = details.choices.map { choice ->
            val choiceScores = scores.filter { it.choiceId == choice.id }
            val scoresByCriterion = mutableMapOf<Long, Float>()
            var weightedTotal = 0f

            details.criteria.forEach { criterion ->
                val score = choiceScores.find { it.criterionId == criterion.id }?.value ?: 0f
                scoresByCriterion[criterion.id] = score
                weightedTotal += score * (criterion.weight / totalWeight)
            }

            ChoiceResult(
                choice = choice,
                totalScore = weightedTotal,
                percentage = 0f, // будет пересчитано
                scoresByCriterion = scoresByCriterion
            )
        }.sortedByDescending { it.totalScore }

        val maxScore = choiceResults.maxOfOrNull { it.totalScore }?.takeIf { it > 0f } ?: 1f
        val rankedWithPercentage = choiceResults.map { result ->
            result.copy(percentage = (result.totalScore / maxScore) * 100f)
        }

        return Result.success(
            DecisionResult(
                decision = details.decision,
                rankedChoices = rankedWithPercentage,
                criteria = details.criteria
            )
        )
    }
}