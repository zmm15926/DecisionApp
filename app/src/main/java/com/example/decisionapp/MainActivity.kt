package com.example.decisionapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import com.example.decisionapp.ui.theme.DecisionAppTheme
import com.example.decisionapp.presentation.auth.AuthViewModel
import com.example.decisionapp.presentation.navigation.AppNavGraph
import com.example.decisionapp.presentation.navigation.Screen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DecisionAppTheme {
                val authViewModel: AuthViewModel = hiltViewModel()
                val currentUser by authViewModel.currentUser.collectAsState()
                val navController = rememberNavController()

                val startDestination = if (currentUser != null)
                    Screen.DecisionList.route
                else
                    Screen.Login.route

                AppNavGraph(
                    navController = navController,
                    startDestination = startDestination
                )
            }
        }
    }
}