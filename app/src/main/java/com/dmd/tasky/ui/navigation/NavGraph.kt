package com.dmd.tasky.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.dmd.tasky.feature.auth.presentation.agenda.AgendaScreen
import com.dmd.tasky.feature.auth.presentation.login.TaskyLoginScreen
import com.dmd.tasky.feature.auth.presentation.register.TaskyRegisterScreen

@Composable
fun TaskyNavHost(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = "register",
        modifier = modifier
    ) {
        composable("register") {
            TaskyRegisterScreen(
                onNavigateToLogin = {
                    navController.navigate("login")
                },
                modifier = Modifier.fillMaxSize()

            )

        }
        composable("login") {
            TaskyLoginScreen(
                onNavigateToRegister = {
                    navController.navigate("register")
                },
                onLoginSuccess = {
                    navController.navigate("agenda") {
                        popUpTo("login") {
                            inclusive = true
                        }
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        }
        composable("agenda"){
            AgendaScreen(
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}