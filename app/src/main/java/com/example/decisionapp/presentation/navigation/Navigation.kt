package com.example.decisionapp.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.decisionapp.presentation.auth.LoginScreen
import com.example.decisionapp.presentation.auth.RegisterScreen
import com.example.decisionapp.presentation.decisions.DecisionListScreen
import com.example.decisionapp.presentation.decisions.CreateDecisionScreen
import com.example.decisionapp.presentation.criteria.CriteriaScreen
import com.example.decisionapp.presentation.criteria.ChoicesScreen
import com.example.decisionapp.presentation.criteria.ScoringScreen
import com.example.decisionapp.presentation.results.ResultScreen

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object DecisionList : Screen("decisions")
    object CreateDecision : Screen("create_decision")
    object Criteria : Screen("criteria/{decisionId}") {
        fun createRoute(decisionId: Long) = "criteria/$decisionId"
    }
    object Choices : Screen("choices/{decisionId}") {
        fun createRoute(decisionId: Long) = "choices/$decisionId"
    }
    object Scoring : Screen("scoring/{decisionId}") {
        fun createRoute(decisionId: Long) = "scoring/$decisionId"
    }
    object Result : Screen("result/{decisionId}") {
        fun createRoute(decisionId: Long) = "result/$decisionId"
    }
}

@Composable
fun AppNavGraph(
    navController: NavHostController = rememberNavController(),
    startDestination: String = Screen.Login.route
) {
    NavHost(navController = navController, startDestination = startDestination) {

        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.DecisionList.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                }
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                onRegisterSuccess = {
                    navController.navigate(Screen.DecisionList.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToLogin = { navController.popBackStack() }
            )
        }

        composable(Screen.DecisionList.route) {
            DecisionListScreen(
                onCreateDecision = { navController.navigate(Screen.CreateDecision.route) },
                onOpenDecision = { id -> navController.navigate(Screen.Criteria.createRoute(id)) },
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.DecisionList.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.CreateDecision.route) {
            CreateDecisionScreen(
                onDecisionCreated = { id ->
                    navController.navigate(Screen.Criteria.createRoute(id)) {
                        popUpTo(Screen.DecisionList.route)
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            Screen.Criteria.route,
            arguments = listOf(navArgument("decisionId") { type = NavType.LongType })
        ) { backStack ->
            val decisionId = backStack.arguments?.getLong("decisionId") ?: return@composable
            CriteriaScreen(
                decisionId = decisionId,
                onNext = { navController.navigate(Screen.Choices.createRoute(decisionId)) },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            Screen.Choices.route,
            arguments = listOf(navArgument("decisionId") { type = NavType.LongType })
        ) { backStack ->
            val decisionId = backStack.arguments?.getLong("decisionId") ?: return@composable
            ChoicesScreen(
                decisionId = decisionId,
                onNext = { navController.navigate(Screen.Scoring.createRoute(decisionId)) },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            Screen.Scoring.route,
            arguments = listOf(navArgument("decisionId") { type = NavType.LongType })
        ) { backStack ->
            val decisionId = backStack.arguments?.getLong("decisionId") ?: return@composable
            ScoringScreen(
                decisionId = decisionId,
                onCalculate = { navController.navigate(Screen.Result.createRoute(decisionId)) },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            Screen.Result.route,
            arguments = listOf(navArgument("decisionId") { type = NavType.LongType })
        ) { backStack ->
            val decisionId = backStack.arguments?.getLong("decisionId") ?: return@composable
            ResultScreen(
                decisionId = decisionId,
                onBack = {
                    navController.navigate(Screen.DecisionList.route) {
                        popUpTo(Screen.DecisionList.route) { inclusive = true }
                    }
                }
            )
        }
    }
}