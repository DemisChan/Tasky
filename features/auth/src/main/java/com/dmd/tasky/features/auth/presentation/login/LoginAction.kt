package com.dmd.tasky.features.auth.presentation.login

import com.dmd.tasky.core.domain.util.UiText

sealed interface LoginAction {
    data class EmailChanged(val email: String) : LoginAction
    data class PasswordChanged(val password: String) : LoginAction
    data object LoginClicked : LoginAction
    data object SignUpClicked : LoginAction
    data object PasswordVisibilityChanged : LoginAction
}

sealed interface LoginEvent {
    data object Success : LoginEvent
    data class Error(val error: UiText) : LoginEvent
}