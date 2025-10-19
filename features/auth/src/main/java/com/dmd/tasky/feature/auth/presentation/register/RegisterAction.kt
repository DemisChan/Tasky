package com.dmd.tasky.feature.auth.presentation.register

sealed interface RegisterAction {
    data class FullNameChanged(val fullName: String) : RegisterAction
    data class EmailChanged(val email: String) : RegisterAction
    data class PasswordChanged(val password: String) : RegisterAction
    data object RegisterClicked : RegisterAction
    data object LoginClicked : RegisterAction
}
