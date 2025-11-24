package com.dmd.tasky.feature.auth.presentation.register

import com.dmd.tasky.core.domain.util.UiText
import com.dmd.tasky.feature.auth.presentation.login.LoginEvent

sealed interface RegisterAction {
    data class FullNameChanged(val fullName: String) : RegisterAction
    data class EmailChanged(val email: String) : RegisterAction
    data class PasswordChanged(val password: String) : RegisterAction
    data object PasswordVisibilityChanged : RegisterAction
    data object RegisterClicked : RegisterAction
    data object LoginClicked : RegisterAction
}

sealed interface RegisterEvent {
    data object Success : RegisterEvent
    data class Error(val error: UiText) : RegisterEvent
}