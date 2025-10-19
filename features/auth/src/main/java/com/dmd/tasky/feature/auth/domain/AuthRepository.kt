package com.dmd.tasky.feature.auth.domain

import com.dmd.tasky.feature.auth.domain.model.LoginResult

interface AuthRepository {
    suspend fun login(email: String, password: String): LoginResult
}