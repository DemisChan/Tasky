package com.dmd.tasky.feature.auth.domain.model

import com.dmd.tasky.core.domain.util.EmptyResult
import com.dmd.tasky.core.domain.util.Error
import com.dmd.tasky.core.domain.util.Result

sealed interface AuthError : Error {
    enum class Network : AuthError {
        NO_INTERNET,           // IOException
        TIMEOUT,               // SocketTimeoutException
        SERVER_ERROR,          // HTTP 500/502/503
        UNKNOWN                // Unexpected exceptions
    }

    enum class Auth : AuthError {
        INVALID_CREDENTIALS,      // 401 for login
        USER_ALREADY_EXISTS,      // 409 for register
        VALIDATION_FAILED         // 400 for register
    }
}

fun AuthError.toUiMessage(): String {
    return when (this) {
        AuthError.Auth.INVALID_CREDENTIALS -> "Invalid email or password"
        AuthError.Auth.USER_ALREADY_EXISTS -> "User with this email already exists"
        AuthError.Auth.VALIDATION_FAILED -> "Validation failed"

        AuthError.Network.NO_INTERNET -> "No Internet connection"
        AuthError.Network.SERVER_ERROR -> "Server error"
        AuthError.Network.TIMEOUT -> "Request timed out"
        AuthError.Network.UNKNOWN -> "Unknown network error"
    }
}


typealias LoginResult = Result<String, AuthError>

typealias RegisterResult = EmptyResult<AuthError>

typealias LogoutResult = EmptyResult<AuthError>