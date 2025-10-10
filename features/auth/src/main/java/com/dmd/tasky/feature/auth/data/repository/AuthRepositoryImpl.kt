package com.dmd.tasky.feature.auth.data.repository

import com.dmd.tasky.feature.auth.data.remote.AuthApi
import com.dmd.tasky.feature.auth.data.remote.LoginRequest
import com.dmd.tasky.feature.auth.domain.AuthRepository
import com.dmd.tasky.feature.auth.domain.model.LoginResult

class AuthRepositoryImpl(private val api: AuthApi) : AuthRepository {
    override suspend fun login(email: String, password: String): LoginResult {
        return try {
            val response = api.login(LoginRequest(email, password))
            LoginResult.Success(response.accessToken)
        } catch (e: Exception) {
            LoginResult.Error(e.message ?: "Unknown error")
        }
    }
}