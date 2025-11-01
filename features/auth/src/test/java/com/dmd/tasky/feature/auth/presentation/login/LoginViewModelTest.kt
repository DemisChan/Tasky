package com.dmd.tasky.feature.auth.presentation.login

import com.dmd.tasky.core.domain.util.Result
import com.dmd.tasky.feature.auth.domain.AuthRepository
import com.dmd.tasky.feature.auth.domain.model.AuthError
import com.dmd.tasky.feature.auth.domain.model.LoginResult
import com.dmd.tasky.feature.auth.presentation.MainCoroutineRule
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class LoginViewModelTest {

    // This is with Mocks

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
        loginViewModel.onAction(LoginAction.EmailChanged(email))
        Assert.assertEquals(email, loginViewModel.state.email)
    }

    @Test
    fun `onPasswordChanged should update password`() {
        val password = "password"
        loginViewModel.onAction(LoginAction.PasswordChanged(password))
        Assert.assertEquals(password, loginViewModel.state.password)
    }

    @Test
    fun `login success should update state correctly`() = runTest {
        val token = "token"
        coEvery { authRepository.login(any(), any()) } returns Result.Success(token)

        loginViewModel.onAction(LoginAction.LoginClicked)

        Assert.assertEquals(null, loginViewModel.state.error)
        Assert.assertEquals(false, loginViewModel.state.isLoading)
    }


    @Test
    fun `login error should update state correctly`() = runTest {
        val errorMessage = "Unknown network error"
        coEvery {
            authRepository.login(
                any(),
                any()
            )
        } returns Result.Error(AuthError.Network.UNKNOWN)

        loginViewModel.onAction(LoginAction.LoginClicked)

        Assert.assertEquals(false, loginViewModel.state.isLoading)
        Assert.assertEquals(errorMessage, loginViewModel.state.error)
    }

    @Test
    fun `login with invalid credentials should update state correctly`() = runTest {
        coEvery {
            authRepository.login(
                any(),
                any()
            )
        } returns Result.Error(AuthError.Auth.INVALID_CREDENTIALS)

        loginViewModel.onAction(LoginAction.LoginClicked)

        Assert.assertEquals(false, loginViewModel.state.isLoading)
        Assert.assertEquals("Invalid email or password", loginViewModel.state.error)
    }
}