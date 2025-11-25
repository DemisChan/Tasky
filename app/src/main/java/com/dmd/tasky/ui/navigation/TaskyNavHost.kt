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
import com.dmd.tasky.ui.splash.SplashScreen

@Composable
fun TaskyNavHost(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = AuthGraphRoutes.AuthGraph,
        modifier = modifier
    ) {
        navigation<AuthGraphRoutes.AuthGraph>(
            startDestination = AuthGraphRoutes.Splash
        ) {
            composable<AuthGraphRoutes.Splash> {
                SplashScreen(
                    onNavigateToLogin = {
                        navController.navigate(AuthGraphRoutes.Login) {
                            popUpTo(AuthGraphRoutes.Splash) {
                                inclusive = true
                            }
                        }
                    },
                    onNavigateToAgenda = {
                        navController.navigate(AgendaGraphRoutes.AgendaGraph) {
                            popUpTo(AuthGraphRoutes.AuthGraph) {
                                inclusive = true
                            }
                        }
                    }
                )
            }
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