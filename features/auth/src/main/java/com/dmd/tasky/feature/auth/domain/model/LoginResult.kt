package com.dmd.tasky.feature.auth.domain.model

sealed class LoginResult {
    data class Success(val token: String) : LoginResult()
    data class Error(val message: String) : LoginResult()
    object InvalidCredentials : LoginResult()
}