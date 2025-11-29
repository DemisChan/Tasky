package com.dmd.tasky.features.auth.data.repository

import com.dmd.tasky.core.data.token.SessionData
import com.dmd.tasky.core.data.token.TokenManager
import com.dmd.tasky.core.domain.util.Result
import com.dmd.tasky.core.domain.util.asEmptyDataResult
import com.dmd.tasky.core.domain.util.onSuccess
import com.dmd.tasky.features.auth.data.remote.AuthApi
import com.dmd.tasky.features.auth.data.remote.dto.LoginRequest
import com.dmd.tasky.features.auth.data.remote.dto.RegisterRequest
import com.dmd.tasky.features.auth.domain.AuthRepository
import com.dmd.tasky.features.auth.domain.model.AuthError
import com.dmd.tasky.features.auth.domain.model.LoginResult
import com.dmd.tasky.features.auth.domain.model.LogoutResult
import com.dmd.tasky.features.auth.domain.model.RegisterResult
import kotlinx.coroutines.CancellationException
import retrofit2.HttpException
import timber.log.Timber
import java.io.IOException
import java.net.SocketTimeoutException

class DefaultAuthRepository(
    private val api: AuthApi,
    private val tokenManager: TokenManager
) : AuthRepository {
    override suspend fun login(email: String, password: String): LoginResult {
        return safeApiCall { api.login(LoginRequest(email, password)) }
            .onSuccess { response ->
                tokenManager.saveSession(
                    SessionData(
                        accessToken = response.accessToken,
                        refreshToken = response.refreshToken,
                        userId = response.userId,
                        username = response.username,
                        accessTokenExpirationTimestamp = response.accessTokenExpirationTimestamp
                    )
                )
            }.asEmptyDataResult()
    }

    override suspend fun register(
        fullName: String,
        email: String,
        password: String
    ): RegisterResult {
        return safeApiCall {
            api.register(
                RegisterRequest(
                    fullName = fullName,
                    email = email,
                    password = password
                )
            )
        }
    }

    override suspend fun logout(): LogoutResult {
        return safeApiCall { api.logout() }
            .onSuccess {
                tokenManager.clearSession()
            }
    }
}

private suspend fun <T> safeApiCall(apiCall: suspend () -> T): Result<T, AuthError> {
    return try {
        val response = apiCall()
        Result.Success(response)
    } catch (e: HttpException) {
        val code = e.code()
        val errorBody = e.response()?.errorBody()?.string()
        Timber.e("HTTP Error: Code=$code, Body=$errorBody")
        when (code) {
            400 -> Result.Error(AuthError.Auth.VALIDATION_FAILED)
            401 -> Result.Error(AuthError.Auth.INVALID_CREDENTIALS)
            409 -> Result.Error(AuthError.Auth.USER_ALREADY_EXISTS)
            in 500..599 -> Result.Error(AuthError.Network.SERVER_ERROR)
            else -> Result.Error(AuthError.Network.UNKNOWN)
        }
    } catch (e: SocketTimeoutException) {
        Timber.e("Timeout error: ${e.message}")
        Result.Error(AuthError.Network.TIMEOUT)
    } catch (e: IOException) {
        Timber.e("Network error: ${e.message}")
        Result.Error(AuthError.Network.NO_INTERNET)
    } catch (e: Exception) {
        Timber.e("Exception: ${e.message}")
        if (e is CancellationException) throw e
        Result.Error(AuthError.Network.UNKNOWN)
    }
}
