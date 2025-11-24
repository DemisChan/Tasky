package com.dmd.tasky.features.auth.domain

import com.dmd.tasky.features.auth.domain.model.LoginResult
import com.dmd.tasky.features.auth.domain.model.LogoutResult
import com.dmd.tasky.features.auth.domain.model.RegisterResult

interface AuthRepository {
    suspend fun login(email: String, password: String): LoginResult
    suspend fun register(fullName: String, email: String, password: String): RegisterResult

    suspend fun logout(): LogoutResult
}
