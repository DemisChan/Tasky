package com.dmd.tasky.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import com.dmd.tasky.feature.auth.presentation.agenda.AgendaScreen
import com.dmd.tasky.feature.auth.presentation.login.TaskyLoginScreen
import com.dmd.tasky.feature.auth.presentation.register.TaskyRegisterScreen

@Composable
fun TaskyNavHost(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = AuthGraph,
        modifier = modifier
    ) {
        navigation<AuthGraph>(
            startDestination = AuthRoute.Login
        ) {
            composable<AuthRoute.Register> {
                TaskyRegisterScreen(
                    onNavigateToLogin = {
                        navController.navigate(AuthRoute.Login)
                    },
                    modifier = Modifier.fillMaxSize()
                )

            }
            composable<AuthRoute.Login> {
                TaskyLoginScreen(
                    onNavigateToRegister = {
                        navController.navigate(AuthRoute.Register)
                    },
                    onLoginSuccess = {
                        navController.navigate(AgendaGraph) {
                            popUpTo(AuthGraph) {
                                inclusive = true
                            }
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
        navigation<AgendaGraph>(
            startDestination = AgendaRoute.Agenda
        ) {
            composable<AgendaRoute.Agenda> {
                AgendaScreen(
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}