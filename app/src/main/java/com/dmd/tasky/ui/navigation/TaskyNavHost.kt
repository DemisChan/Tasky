package com.dmd.tasky.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import com.dmd.tasky.features.auth.presentation.agenda.AgendaScreen
import com.dmd.tasky.features.auth.presentation.login.TaskyLoginScreen
import com.dmd.tasky.features.auth.presentation.register.TaskyRegisterScreen

@Composable
fun TaskyNavHost(
    modifier: Modifier = Modifier,
    isAuthenticated: Boolean
) {
    val navController = rememberNavController()
    val startDestination = if (isAuthenticated) AgendaGraphRoutes.AgendaGraph else AuthGraphRoutes.AuthGraph

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        navigation<AuthGraphRoutes.AuthGraph>(
            startDestination = AuthGraphRoutes.Login
        ) {
            composable<AuthGraphRoutes.Register> {
                TaskyRegisterScreen(
                    onNavigateToLogin = {
                        navController.navigate(AuthGraphRoutes.Login)
                    },
                    modifier = Modifier.fillMaxSize()
                )

            }
            composable<AuthGraphRoutes.Login> {
                TaskyLoginScreen(
                    onNavigateToRegister = {
                        navController.navigate(AuthGraphRoutes.Register)
                    },
                    onLoginSuccess = {
                        navController.navigate(AgendaGraphRoutes.AgendaGraph) {
                            popUpTo(AuthGraphRoutes.AuthGraph) {
                                inclusive = true
                            }
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
        navigation<AgendaGraphRoutes.AgendaGraph>(
            startDestination = AgendaGraphRoutes.Agenda
        ) {
            composable<AgendaGraphRoutes.Agenda> {
                AgendaScreen(
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}