package com.example.decisionapp

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.Runs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import app.cash.turbine.test
import com.example.decisionapp.domain.model.User
import com.example.decisionapp.domain.usecase.GetCurrentUserUseCase
import com.example.decisionapp.domain.usecase.LoginUseCase
import com.example.decisionapp.domain.usecase.LogoutUseCase
import com.example.decisionapp.domain.usecase.RegisterUseCase
import com.example.decisionapp.presentation.auth.AuthViewModel
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class AuthViewModelTest {

    private val loginUseCase: LoginUseCase = mockk()
    private val registerUseCase: RegisterUseCase = mockk()
    private val logoutUseCase: LogoutUseCase = mockk()
    private val getCurrentUserUseCase: GetCurrentUserUseCase = mockk()
    private val testDispatcher = StandardTestDispatcher()

    private lateinit var viewModel: AuthViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { getCurrentUserUseCase() } returns flowOf(null)
        viewModel = AuthViewModel(loginUseCase, registerUseCase, logoutUseCase, getCurrentUserUseCase)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initialStateIsNotLoadingAndNoError() {
        val state = viewModel.uiState.value
        Assert.assertFalse(state.isLoading)
        Assert.assertNull(state.error)
        Assert.assertFalse(state.success)
    }

    @Test
    fun loginSuccessUpdatesState() = runTest {
        val user = User(id = 1L, username = "test", passwordHash = "hash")
        coEvery { loginUseCase("test", "password123") } returns Result.success(user)

        viewModel.uiState.test {
            awaitItem() // initial

            viewModel.login("test", "password123")
            testDispatcher.scheduler.advanceUntilIdle()

            val loadingState = awaitItem()
            Assert.assertTrue(loadingState.isLoading)

            val successState = awaitItem()
            Assert.assertTrue(successState.success)
            Assert.assertFalse(successState.isLoading)
            Assert.assertNull(successState.error)
        }
    }

    @Test
    fun loginFailureUpdatesError() = runTest {
        coEvery { loginUseCase("wrong", "password123") } returns
                Result.failure(Exception("Пользователь не найден"))

        viewModel.uiState.test {
            awaitItem() // initial
            viewModel.login("wrong", "password123")
            testDispatcher.scheduler.advanceUntilIdle()
            awaitItem() // loading
            val errorState = awaitItem()
            Assert.assertFalse(errorState.isLoading)
            Assert.assertFalse(errorState.success)
            Assert.assertEquals("Пользователь не найден", errorState.error)
        }
    }

    @Test
    fun clearErrorResetsErrorToNull() = runTest {
        coEvery { loginUseCase("wrong", "pass123") } returns Result.failure(Exception("Ошибка"))

        viewModel.login("wrong", "pass123")
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.clearError()
        Assert.assertNull(viewModel.uiState.value.error)
    }

    @Test
    fun registerSuccessUpdatesState() = runTest {
        val user = User(id = 2L, username = "newuser", passwordHash = "hash")
        coEvery { registerUseCase("newuser", "password123", "password123") } returns
                Result.success(user)

        viewModel.register("newuser", "password123", "password123")
        testDispatcher.scheduler.advanceUntilIdle()

        Assert.assertTrue(viewModel.uiState.value.success)
    }

    @Test
    fun logoutCallsUseCase() = runTest {
        coEvery { logoutUseCase() } just Runs

        viewModel.logout()
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { logoutUseCase() }
    }
}