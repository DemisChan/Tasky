package com.dmd.tasky.feature.auth.domain.model

sealed class RegisterResult {
    object Success : RegisterResult()
    data class Error(val message: String) : RegisterResult()
    object UserAlreadyExists : RegisterResult()
}