package com.example.decisionapp

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import io.mockk.Runs
import kotlinx.coroutines.runBlocking
import com.example.decisionapp.domain.model.*
import com.example.decisionapp.domain.repository.*
import com.example.decisionapp.domain.usecase.*
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class LoginUseCaseTest {

    private val authRepository: AuthRepository = mockk()
    private lateinit var loginUseCase: LoginUseCase

    @Before
    fun setup() {
        loginUseCase = LoginUseCase(authRepository)
    }

    @Test
    fun loginWithBlankUsernameReturnsFailure() = runBlocking {
        val result = loginUseCase("", "password123")
        assertTrue(result.isFailure)
        assertEquals("Логин не может быть пустым", result.exceptionOrNull()?.message)
    }

    @Test
    fun loginWithShortPasswordReturnsFailure() = runBlocking {
        val result = loginUseCase("user", "pass")
        assertTrue(result.isFailure)
        assertEquals("Пароль минимум 6 символов", result.exceptionOrNull()?.message)
    }

    @Test
    fun loginWithValidCredentialsCallsRepository() = runBlocking {
        val user = User(id = 1, username = "user", passwordHash = "hash")
        coEvery { authRepository.login("user", "password123") } returns Result.success(user)

        val result = loginUseCase("user", "password123")
        assertTrue(result.isSuccess)
        assertEquals(user, result.getOrNull())
        coVerify { authRepository.login("user", "password123") }
    }

    @Test
    fun loginTrimsUsernameWhitespace() = runBlocking {
        val user = User(id = 1, username = "user", passwordHash = "hash")
        coEvery { authRepository.login("user", "password123") } returns Result.success(user)

        loginUseCase("  user  ", "password123")
        coVerify { authRepository.login("user", "password123") }
    }
}

class RegisterUseCaseTest {

    private val authRepository: AuthRepository = mockk()
    private lateinit var registerUseCase: RegisterUseCase

    @Before
    fun setup() {
        registerUseCase = RegisterUseCase(authRepository)
    }

    @Test
    fun registerWithBlankUsernameReturnsFailure() = runBlocking {
        val result = registerUseCase("", "password", "password")
        assertTrue(result.isFailure)
    }

    @Test
    fun registerWithShortUsernameReturnsFailure() = runBlocking {
        val result = registerUseCase("ab", "password", "password")
        assertTrue(result.isFailure)
        assertEquals("Логин минимум 3 символа", result.exceptionOrNull()?.message)
    }

    @Test
    fun registerWithShortPasswordReturnsFailure() = runBlocking {
        val result = registerUseCase("user", "pass", "pass")
        assertTrue(result.isFailure)
        assertEquals("Пароль минимум 6 символов", result.exceptionOrNull()?.message)
    }

    @Test
    fun registerWithMismatchedPasswordsReturnsFailure() = runBlocking {
        val result = registerUseCase("user", "password123", "different")
        assertTrue(result.isFailure)
        assertEquals("Пароли не совпадают", result.exceptionOrNull()?.message)
    }

    @Test
    fun registerWithValidDataCallsRepository() = runBlocking {
        val user = User(id = 1, username = "user", passwordHash = "hash")
        coEvery { authRepository.register("user", "password123") } returns Result.success(user)

        val result = registerUseCase("user", "password123", "password123")
        assertTrue(result.isSuccess)
    }
}

class CreateDecisionUseCaseTest {

    private val decisionRepository: DecisionRepository = mockk()
    private lateinit var createDecisionUseCase: CreateDecisionUseCase

    @Before
    fun setup() {
        createDecisionUseCase = CreateDecisionUseCase(decisionRepository)
    }

    @Test
    fun createDecisionWithBlankTitleReturnsFailure() = runBlocking {
        val result = createDecisionUseCase(1L, "", "")
        assertTrue(result.isFailure)
        assertEquals("Название решения не может быть пустым", result.exceptionOrNull()?.message)
    }

    @Test
    fun createDecisionWithValidTitleCallsRepository() = runBlocking {
        coEvery { decisionRepository.insertDecision(any()) } returns 42L

        val result = createDecisionUseCase(1L, "Выбор работы", "Описание")
        assertTrue(result.isSuccess)
        assertEquals(42L, result.getOrNull())
    }
}

class CalculateDecisionResultUseCaseTest {

    private val decisionRepository: DecisionRepository = mockk()
    private val scoreRepository: ScoreRepository = mockk()
    private lateinit var calculateUseCase: CalculateDecisionResultUseCase

    @Before
    fun setup() {
        calculateUseCase = CalculateDecisionResultUseCase(decisionRepository, scoreRepository)
    }

    @Test
    fun calculateReturnsFailureWhenDecisionNotFound() = runBlocking {
        coEvery { decisionRepository.getDecisionWithDetails(any()) } returns null

        val result = calculateUseCase(1L)
        assertTrue(result.isFailure)
    }

    @Test
    fun calculateRanksChoicesCorrectly() = runBlocking {
        val decision = Decision(id = 1L, userId = 1L, title = "Test")
        val criteria = listOf(
            Criterion(id = 1L, decisionId = 1L, name = "C1", weight = 1f),
            Criterion(id = 2L, decisionId = 1L, name = "C2", weight = 1f)
        )
        val choices = listOf(
            Choice(id = 1L, decisionId = 1L, name = "Вариант 1"),
            Choice(id = 2L, decisionId = 1L, name = "Вариант 2")
        )
        val scores = listOf(
            Score(choiceId = 1L, criterionId = 1L, value = 8f),
            Score(choiceId = 1L, criterionId = 2L, value = 6f),
            Score(choiceId = 2L, criterionId = 1L, value = 4f),
            Score(choiceId = 2L, criterionId = 2L, value = 3f)
        )

        coEvery { decisionRepository.getDecisionWithDetails(1L) } returns
                DecisionWithDetails(decision, criteria, choices, scores)
        coEvery { scoreRepository.getScoresByDecision(1L) } returns scores

        val result = calculateUseCase(1L)
        assertTrue(result.isSuccess)

        val decisionResult = result.getOrNull()!!
        assertEquals("Вариант 1", decisionResult.rankedChoices.first().choice.name)
        assertEquals(100f, decisionResult.rankedChoices.first().percentage)
        assertTrue(decisionResult.rankedChoices[1].percentage < 100f)
    }
}

class SaveCriterionUseCaseTest {

    private val criterionRepository: CriterionRepository = mockk()
    private lateinit var saveCriterionUseCase: SaveCriterionUseCase

    @Before
    fun setup() {
        saveCriterionUseCase = SaveCriterionUseCase(criterionRepository)
    }

    @Test
    fun saveCriterionWithBlankNameReturnsFailure() = runBlocking {
        val criterion = Criterion(decisionId = 1L, name = "  ")
        val result = saveCriterionUseCase(criterion)
        assertTrue(result.isFailure)
    }

    @Test
    fun insertNewCriterionWhenIdIsZero() = runBlocking {
        val criterion = Criterion(id = 0L, decisionId = 1L, name = "Цена")
        coEvery { criterionRepository.insertCriterion(criterion) } returns 5L

        val result = saveCriterionUseCase(criterion)
        assertTrue(result.isSuccess)
        assertEquals(5L, result.getOrNull())
        coVerify { criterionRepository.insertCriterion(criterion) }
        coVerify(exactly = 0) { criterionRepository.updateCriterion(any()) }
    }

    @Test
    fun updateExistingCriterionWhenIdIsNotZero() = runBlocking {
        val criterion = Criterion(id = 3L, decisionId = 1L, name = "Качество")
        coEvery { criterionRepository.updateCriterion(criterion) } just Runs

        val result = saveCriterionUseCase(criterion)
        assertTrue(result.isSuccess)
        coVerify { criterionRepository.updateCriterion(criterion) }
        coVerify(exactly = 0) { criterionRepository.insertCriterion(any()) }
    }
}