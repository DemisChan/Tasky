package com.dmd.tasky.feature.auth.data.repository

import com.dmd.tasky.core.domain.util.Result
import com.dmd.tasky.feature.auth.data.remote.AuthApi
import com.dmd.tasky.feature.auth.data.remote.dto.LoginRequest
import com.dmd.tasky.feature.auth.data.remote.dto.RegisterRequest
import com.dmd.tasky.feature.auth.domain.AuthRepository
import com.dmd.tasky.feature.auth.domain.model.AuthError
import com.dmd.tasky.feature.auth.domain.model.LoginResult
import com.dmd.tasky.feature.auth.domain.model.RegisterResult
import kotlinx.coroutines.CancellationException
import retrofit2.HttpException
import timber.log.Timber
import java.io.IOException
import java.net.SocketTimeoutException

class DefaultAuthRepository(private val api: AuthApi) : AuthRepository {
    override suspend fun login(email: String, password: String): LoginResult {
        return try {
            val response = api.login(LoginRequest(email, password))
            Result.Success(response.accessToken)
        } catch (e: HttpException) {
            val code = e.code()
            val errorBody = e.response()?.errorBody()?.string()
            Timber.e("HTTP Error: Code=$code, Body=$errorBody")
            when (code) {
                in 500..599 -> Result.Error(AuthError.Network.SERVER_ERROR)
                401 -> Result.Error(AuthError.Auth.INVALID_CREDENTIALS)
                else -> Result.Error(AuthError.Network.UNKNOWN)
            }
        } catch (e: SocketTimeoutException) {
            Timber.e("Timeout error: ${e.message}")
            Result.Error(AuthError.Network.TIMEOUT)
        } catch (e: IOException) {
            Timber.e("Network error: ${e.message}")
            Result.Error(AuthError.Network.NO_INTERNET)
        } catch (e: Exception) {
            Timber.e("Exception during login: ${e.message}")
            if (e is CancellationException) throw e
            Result.Error(AuthError.Network.UNKNOWN)
        }
    }

    override suspend fun register(
        fullName: String,
        email: String,
        password: String
    ): RegisterResult {
        return try {
            Timber.d("Attempting register with: name='$fullName', email='$email'")
            val response = api.register(
                RegisterRequest(
                    fullName = fullName,
                    email = email,
                    password = password
                )
            )
            Timber.d("Register successful! Status code: ${response.code()}")
            Result.Success(Unit)
        } catch (e: HttpException) {
            val code = e.code()
            val errorBody = e.response()?.errorBody()?.string()
            Timber.e("HTTP Error: Code=$code, Body=$errorBody")
            when (code) {
                409 -> Result.Error(AuthError.Auth.USER_ALREADY_EXISTS)
                400 -> Result.Error(AuthError.Auth.VALIDATION_FAILED)
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
            Timber.e("Exception during register: ${e.message}")
            if (e is CancellationException) throw e
            Result.Error(AuthError.Network.UNKNOWN)
        }
    }
}
