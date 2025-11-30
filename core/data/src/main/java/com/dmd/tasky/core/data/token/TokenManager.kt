package com.dmd.tasky.core.data.token

import kotlinx.coroutines.flow.Flow


interface TokenManager {
    suspend fun saveSession(sessionData: SessionData)
    suspend fun getSession(): SessionData?
    suspend fun clearSession()
    fun isAuthenticated(): Flow<Boolean>
    suspend fun isTokenValid(): Boolean
}

data class SessionData(
    val accessToken: String,
    val refreshToken: String,
    val username: String,
    val userId: String,
    val accessTokenExpirationTimestamp: Long,
)
