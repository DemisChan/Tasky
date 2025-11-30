package com.dmd.tasky.features.auth.domain.model

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


typealias LoginResult = Result<Unit, AuthError>

typealias RegisterResult = EmptyResult<AuthError>

typealias LogoutResult = EmptyResult<AuthError>