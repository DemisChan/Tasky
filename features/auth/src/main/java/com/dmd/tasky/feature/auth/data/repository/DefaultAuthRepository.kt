package com.dmd.tasky.feature.auth.data.repository

import com.dmd.tasky.feature.auth.data.remote.AuthApi
import com.dmd.tasky.feature.auth.data.remote.dto.LoginRequest
import com.dmd.tasky.feature.auth.data.remote.dto.RegisterRequest
import com.dmd.tasky.feature.auth.domain.AuthRepository
import com.dmd.tasky.feature.auth.domain.model.LoginResult
import com.dmd.tasky.feature.auth.domain.model.RegisterResult
import kotlinx.coroutines.CancellationException
import retrofit2.HttpException
import timber.log.Timber

class DefaultAuthRepository(private val api: AuthApi) : AuthRepository {
    override suspend fun login(email: String, password: String): LoginResult {
        return try {
            val response = api.login(LoginRequest(email, password))
            LoginResult.Success(response.accessToken)
        } catch (e: Exception) {
            if(e is CancellationException) throw e
            LoginResult.Error(e.message ?: "Unknown error")
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
            RegisterResult.Success
            
        } catch (e: HttpException) {
            val code = e.code()
            val errorBody = e.response()?.errorBody()?.string()
            
            Timber.e("HTTP Error: Code=$code, Body=$errorBody")
            
            when (code) {
                409 -> {
                    Timber.d("409 Conflict - User already exists")
                    RegisterResult.UserAlreadyExists
                }
                400 -> {
                    Timber.d("400 Bad Request - Validation error: $errorBody")
                    RegisterResult.Error(errorBody ?: "Validation failed")
                }
                else -> {
                    Timber.d("Unexpected error code: $code")
                    RegisterResult.Error(errorBody ?: "Unknown error")
                }
            }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            
            Timber.e("Exception during register: ${e.javaClass.simpleName}: ${e.message}")
            e.printStackTrace()
            
            RegisterResult.Error(e.message ?: "Unknown error")
        }
    }
}
