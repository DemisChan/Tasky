package com.dmd.tasky.features.auth.data.repository

import com.dmd.tasky.core.data.token.SessionData
import com.dmd.tasky.core.data.token.TokenManager
import com.dmd.tasky.core.domain.util.Result
import com.dmd.tasky.features.auth.domain.AuthRepository
import com.dmd.tasky.features.auth.domain.model.AuthError
import com.dmd.tasky.features.auth.domain.model.LoginResult
import com.dmd.tasky.features.auth.domain.model.LogoutResult
import com.dmd.tasky.features.auth.domain.model.RegisterResult
import kotlinx.coroutines.delay
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/**
 * Stand-in for [DefaultAuthRepository] used when BuildConfig.USE_FAKE_BACKEND is true
 * (the Tasky backend is no longer reliably reachable).
 *
 * Mirrors the behaviour described in tasky-api.pdf:
 * - register: fullName 4-50 chars, valid email, password >= 9 chars with upper, lower and digit;
 *   400 for validation errors, 409 if the email is taken
 * - login: 401 for unknown email / wrong password; saves a session like the real repository
 *
 * Users live in memory only, so registered accounts are lost when the process dies.
 * Two accounts are always seeded so you can log in on a fresh launch.
 */
class InMemoryAuthRepository(
    private val tokenManager: TokenManager
) : AuthRepository {

    private data class FakeUser(
        val id: String,
        val fullName: String,
        val password: String,
        val tokenLifetimeMs: Long = ACCESS_TOKEN_LIFETIME_MS
    )

    private val users = ConcurrentHashMap<String, FakeUser>()

    init {
        users["demo@tasky.app"] = FakeUser(UUID.randomUUID().toString(), "Demo User", "Demo12345")
        // Documented test account whose token is only valid for 10 seconds.
        users["debug@debug.com"] = FakeUser(
            id = UUID.randomUUID().toString(),
            fullName = "Debug User",
            password = "Debug12345",
            tokenLifetimeMs = DEBUG_TOKEN_LIFETIME_MS
        )
    }

    override suspend fun login(email: String, password: String): LoginResult {
        delay(LATENCY_MS)
        val user = users[email.trim().lowercase()]
        if (user == null || user.password != password) {
            return Result.Error(AuthError.Auth.INVALID_CREDENTIALS)
        }
        tokenManager.saveSession(
            SessionData(
                accessToken = UUID.randomUUID().toString(),
                refreshToken = UUID.randomUUID().toString(),
                username = user.fullName,
                userId = user.id,
                accessTokenExpirationTimestamp = System.currentTimeMillis() + user.tokenLifetimeMs
            )
        )
        return Result.Success(Unit)
    }

    override suspend fun register(
        fullName: String,
        email: String,
        password: String
    ): RegisterResult {
        delay(LATENCY_MS)
        val key = email.trim().lowercase()
        if (fullName.trim().length !in 4..50 || !EMAIL_REGEX.matches(key) || !isValidPassword(password)) {
            return Result.Error(AuthError.Auth.VALIDATION_FAILED)
        }
        val created = FakeUser(UUID.randomUUID().toString(), fullName.trim(), password)
        if (users.putIfAbsent(key, created) != null) {
            return Result.Error(AuthError.Auth.USER_ALREADY_EXISTS)
        }
        return Result.Success(Unit)
    }

    override suspend fun logout(): LogoutResult {
        delay(LATENCY_MS)
        tokenManager.clearSession()
        return Result.Success(Unit)
    }

    private fun isValidPassword(password: String): Boolean {
        return password.length >= 9 &&
            password.any { it.isUpperCase() } &&
            password.any { it.isLowerCase() } &&
            password.any { it.isDigit() }
    }

    private companion object {
        const val LATENCY_MS = 400L
        const val ACCESS_TOKEN_LIFETIME_MS = 60 * 60 * 1000L
        const val DEBUG_TOKEN_LIFETIME_MS = 10 * 1000L
        val EMAIL_REGEX = Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")
    }
}
