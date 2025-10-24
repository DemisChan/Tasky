package com.dmd.tasky.feature.auth.domain

import com.dmd.tasky.feature.auth.domain.model.LoginResult
import com.dmd.tasky.feature.auth.domain.model.RegisterResult

interface AuthRepository {
    suspend fun login(email: String, password: String): LoginResult
    suspend fun register(fullName: String, email: String, password: String): RegisterResult
}
