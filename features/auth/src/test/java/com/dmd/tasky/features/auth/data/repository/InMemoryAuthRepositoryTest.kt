package com.dmd.tasky.features.auth.data.repository

import com.dmd.tasky.core.data.token.TokenManager
import com.dmd.tasky.core.domain.util.Result
import com.dmd.tasky.features.auth.domain.model.AuthError
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue

class InMemoryAuthRepositoryTest {
    private lateinit var tokenManager: TokenManager
    private lateinit var repository: InMemoryAuthRepository

    @Before
    fun setUp() {
        tokenManager = mockk(relaxed = true)
        repository = InMemoryAuthRepository(tokenManager)
    }

    @Test
    fun `seeded demo user can log in and a session is saved`() = runTest {
        val result = repository.login("demo@tasky.app", "Demo12345")

        assertTrue(result is Result.Success)
        coVerify { tokenManager.saveSession(any()) }
    }

    @Test
    fun `wrong password returns INVALID_CREDENTIALS`() = runTest {
        assertEquals(
            Result.Error(AuthError.Auth.INVALID_CREDENTIALS),
            repository.login("demo@tasky.app", "wrong")
        )
    }

    @Test
    fun `registered user can log in afterwards`() = runTest {
        assertTrue(repository.register("New User", "new@user.com", "Passw0rdOk") is Result.Success)

        assertTrue(repository.login("NEW@user.com", "Passw0rdOk") is Result.Success)
    }

    @Test
    fun `registering an existing email returns USER_ALREADY_EXISTS`() = runTest {
        assertEquals(
            Result.Error(AuthError.Auth.USER_ALREADY_EXISTS),
            repository.register("Demo User", "demo@tasky.app", "Passw0rdOk")
        )
    }

    @Test
    fun `weak password, short name or bad email return VALIDATION_FAILED`() = runTest {
        val failed = Result.Error(AuthError.Auth.VALIDATION_FAILED)
        assertEquals(failed, repository.register("New User", "a@b.com", "short1A"))
        assertEquals(failed, repository.register("Abc", "a@b.com", "Passw0rdOk"))
        assertEquals(failed, repository.register("New User", "not-an-email", "Passw0rdOk"))
    }

    @Test
    fun `logout clears the session`() = runTest {
        repository.logout()

        coVerify { tokenManager.clearSession() }
    }
}
