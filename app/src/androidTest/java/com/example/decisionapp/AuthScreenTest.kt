package com.example.decisionapp

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
class AuthScreenTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @Test
    fun loginScreen_isDisplayed() {
        composeRule.onNodeWithText("Принятие решений", ignoreCase = true).assertIsDisplayed()
        composeRule.onNodeWithText("Войти").assertIsDisplayed()
    }

    @Test
    fun loginScreen_showsErrorOnEmptyCredentials() {
        composeRule.onNodeWithText("Войти").performClick()
        // Error should appear (blank username)
        composeRule.onNodeWithText("Логин не может быть пустым").assertIsDisplayed()
    }

    @Test
    fun navigateToRegisterScreen() {
        composeRule.onNodeWithText("Нет аккаунта? Зарегистрироваться").performClick()
        composeRule.onNodeWithText("Регистрация").assertIsDisplayed()
    }

    @Test
    fun registerScreen_backToLogin() {
        composeRule.onNodeWithText("Нет аккаунта? Зарегистрироваться").performClick()
        composeRule.onNodeWithText("Уже есть аккаунт? Войти").performClick()
        composeRule.onNodeWithText("Принятие решений", ignoreCase = true).assertIsDisplayed()
    }

    @Test
    fun loginScreen_showsPasswordVisibilityToggle() {
        composeRule.onNodeWithText("Пароль").assertIsDisplayed()
    }
}

@HiltAndroidTest
class DecisionFlowTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @Test
    fun fullRegistrationAndDecisionFlow() {
        composeRule.onNodeWithText("Нет аккаунта? Зарегистрироваться").performClick()

        val timestamp = System.currentTimeMillis()
        val testUser = "testuser$timestamp"

        composeRule.onNodeWithTag("RegisterUsernameInput")
            .performTextInput(testUser)

        composeRule.onNodeWithTag("RegisterPasswordInput")
            .performTextInput("password123")

        composeRule.onNodeWithTag("RegisterConfirmPasswordInput")
            .performTextInput("password123")

        composeRule.onNodeWithText("Создать аккаунт").performClick()
    }
}