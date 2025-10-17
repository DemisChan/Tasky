package com.dmd.tasky.feature.auth.presentation

import com.dmd.tasky.feature.auth.domain.AuthRepository
import com.dmd.tasky.feature.auth.domain.model.LoginResult
import com.dmd.tasky.feature.auth.presentation.login.LoginViewModel
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class LoginViewModelTest {

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    private lateinit var loginViewModel: LoginViewModel
    private lateinit var authRepository: AuthRepository

    @Before
    fun setUp() {
        authRepository = mockk()
        loginViewModel = LoginViewModel(authRepository)
    }

    @Test
    fun `onEmailChanged should update email`() {
        val email = "test@test.com"
        loginViewModel.onEmailChanged(email)
        assertEquals(email, loginViewModel.state.email)
    }

    @Test
    fun `onPasswordChanged should update password`() {
        val password = "password"
        loginViewModel.onPasswordChanged(password)
        assertEquals(password, loginViewModel.state.password)
    }

    @Test
    fun `login success should update state correctly`() = runTest {
        val token = "token"
        coEvery { authRepository.login(any(), any()) } returns LoginResult.Success(token)

        loginViewModel.onLoginClicked()

        assertEquals(null, loginViewModel.state.error)
        assertEquals(false, loginViewModel.state.isLoading)
    }


    @Test
    fun `login error should update state correctly`() = runTest {
        val errorMessage = "error"
        coEvery { authRepository.login(any(), any()) } returns LoginResult.Error(errorMessage)

        loginViewModel.onLoginClicked()

        assertEquals(false, loginViewModel.state.isLoading)
        assertEquals(errorMessage, loginViewModel.state.error)
    }

    @Test
    fun `login with invalid credentials should update state correctly`() = runTest {
        coEvery { authRepository.login(any(), any()) } returns LoginResult.InvalidCredentials

        loginViewModel.onLoginClicked()

        assertEquals(false, loginViewModel.state.isLoading)
        assertEquals("Invalid credentials", loginViewModel.state.error)
    }
}