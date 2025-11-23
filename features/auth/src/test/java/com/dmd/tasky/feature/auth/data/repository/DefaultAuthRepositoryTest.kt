package com.dmd.tasky.feature.auth.data.repository

import com.dmd.tasky.core.domain.util.Result
import com.dmd.tasky.feature.auth.data.remote.AuthApi
import com.dmd.tasky.feature.auth.data.remote.AuthResponse
import com.dmd.tasky.feature.auth.data.remote.dto.LoginRequest
import com.dmd.tasky.feature.auth.data.remote.dto.RegisterRequest
import com.dmd.tasky.feature.auth.domain.model.AuthError
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.coVerify
import com.dmd.tasky.core.data.token.TokenManager
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response

class DefaultAuthRepositoryTest {
    private lateinit var authApi: AuthApi
    private lateinit var tokenManager: TokenManager
    private lateinit var repository: DefaultAuthRepository

    @Before
    fun setUp() {
        authApi = mockk()
        tokenManager = mockk(relaxed = true)
        repository = DefaultAuthRepository(authApi, tokenManager)
    }


    @Test
    fun `login with valid credentials returns success with token`() = runTest {
        val email = "test@test.com"
        val password = "Test12345"
        val expectedToken = "fake_token_123"

        val authResponse = AuthResponse(
            accessToken = expectedToken,
            refreshToken = "refresh_token",
            username = "Test User",
            accessTokenExpirationTimestamp = 1234567890L,
            userId = "user_id",
        )

        coEvery {
            authApi.login(LoginRequest(email, password))
        } returns authResponse

        // When
        val result = repository.login(email, password)

        // Then
        assertTrue(result is Result.Success)
        coVerify { tokenManager.saveSession(any()) }
    }

    @Test
    fun `login with invalid credentials returns INVALID_CREDENTIALS error`() = runTest {
        val email = "test@test.com"
        val password = "Test12345"

        coEvery {
            authApi.login(LoginRequest(email, password))
        } throws HttpException(
            Response.error<AuthResponse>(401, "".toResponseBody(null))
        )

        // When
        val result = repository.login(email, password)

        // Then
        assertTrue("Result should be Error", result is Result.Error)
        assertEquals(
            AuthError.Auth.INVALID_CREDENTIALS,
            (result as Result.Error).error
        )
    }

    @Test
    fun `register with valid data returns success`() = runTest {

        val fullName = "Test User"
        val email = "test@test.com"
        val password = "Test12345"

        coEvery {
            authApi.register(RegisterRequest(fullName, email, password))
        } returns Unit

        // When
        val result = repository.register(fullName, email, password)

        // Then
        assertTrue(result is Result.Success)
    }

    @Test
    fun `register with existing email returns USER_ALREADY_EXISTS error`() = runTest {

        val fullName = "Test User"
        val email = "test@test.com"
        val password = "Test12345"

        coEvery {
            authApi.register(RegisterRequest(fullName, email, password))
        } throws HttpException(
            Response.error<Unit>(
                409,
                """{"status":"CONFLICT","reason":["A user with that email already exists."]}"""
                    .toResponseBody(
                        null
                    )
            )
        )

        // When
        val result = repository.register(fullName, email, password)

        // Then
        assertTrue("Result should be Error", result is Result.Error)
        assertEquals(
            AuthError.Auth.USER_ALREADY_EXISTS,
            (result as Result.Error).error
        )
    }
}