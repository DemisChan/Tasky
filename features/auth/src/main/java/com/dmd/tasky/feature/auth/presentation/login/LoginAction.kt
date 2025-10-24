package com.dmd.tasky.feature.auth.presentation.login

sealed interface LoginAction {
    data class EmailChanged(val email: String) : LoginAction
    data class PasswordChanged(val password: String) : LoginAction
    data object LoginClicked : LoginAction
    data object SignUpClicked : LoginAction
    data object PasswordVisibilityChanged : LoginAction
}