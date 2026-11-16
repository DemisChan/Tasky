package com.dmd.tasky.features.auth.domain

import com.dmd.tasky.features.auth.domain.model.*

interface AuthRepository {
    suspend fun login(email: String, password: String): LoginResult
    suspend fun register(fullName: String, email: String, password: String): RegisterResult

    suspend fun logout(): LogoutResult
}