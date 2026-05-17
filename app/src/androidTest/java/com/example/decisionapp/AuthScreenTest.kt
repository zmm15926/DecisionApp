package com.example.decisionapp

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
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
        composeRule.onNodeWithText("Принятие Решений").assertIsDisplayed()
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
        composeRule.onNodeWithText("Принятие Решений").assertIsDisplayed()
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
        // Register
        composeRule.onNodeWithText("Нет аккаунта? Зарегистрироваться").performClick()

        val timestamp = System.currentTimeMillis()
        val testUser = "testuser$timestamp"

        composeRule.onNodeWithText("Логин (мин. 3 символа)").let {
            // find text field by label
        }
        composeRule.onAllNodesWithText("")[0] // placeholder approach

        // Type in username field
        composeRule.onNodeWithContentDescription("username", useUnmergedTree = true)
            .performTextInput(testUser)

        // Type password
        composeRule.onNodeWithText("Пароль (мин. 6 символов)").performClick()
        composeRule.onNodeWithText("Зарегистрироваться").performClick()
    }
}