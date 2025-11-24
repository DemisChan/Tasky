package com.dmd.tasky.features.auth.presentation.login

import app.cash.turbine.test
import com.dmd.tasky.core.domain.util.Result
import com.dmd.tasky.core.domain.util.UiText
import com.dmd.tasky.features.auth.R
import com.dmd.tasky.features.auth.domain.AuthRepository
import com.dmd.tasky.features.auth.domain.model.AuthError
import com.dmd.tasky.features.auth.presentation.MainCoroutineRule
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
    fun `login success should update state and emit Success event`() = runTest {
        coEvery { authRepository.login(any(), any()) } returns Result.Success(Unit)

        loginViewModel.events.test {
            loginViewModel.onAction(LoginAction.LoginClicked)

            val event = awaitItem()
            Assert.assertTrue(event is LoginEvent.Success)

            Assert.assertEquals(null, loginViewModel.state.error)
            Assert.assertEquals(false, loginViewModel.state.isLoading)
        }
    }


    @Test
    fun `login error should update state and emit Error event`() = runTest {
        coEvery {
            authRepository.login(any(), any())
        } returns Result.Error(AuthError.Network.UNKNOWN)

        loginViewModel.events.test {
            loginViewModel.onAction(LoginAction.LoginClicked)

            val event = awaitItem()
            Assert.assertTrue(event is LoginEvent.Error)
            val expectedError = UiText.StringResource(R.string.unknown_network_error)
            Assert.assertEquals(expectedError, (event as LoginEvent.Error).error)

            Assert.assertEquals(false, loginViewModel.state.isLoading)
            Assert.assertEquals(expectedError, loginViewModel.state.error)
        }
    }

    @Test
    fun `login with invalid credentials should emit Error event`() = runTest {
        coEvery {
            authRepository.login(any(), any())
        } returns Result.Error(AuthError.Auth.INVALID_CREDENTIALS)

        loginViewModel.events.test {
            loginViewModel.onAction(LoginAction.LoginClicked)

            val event = awaitItem()
            Assert.assertTrue(event is LoginEvent.Error)

            val expectedError = UiText.StringResource(R.string.error_invalid_credentials)
            Assert.assertEquals(expectedError, (event as LoginEvent.Error).error)
            Assert.assertEquals(expectedError, loginViewModel.state.error)
        }
    }

    @Test
    fun `typing email should clear error`() = runTest {
        coEvery {
            authRepository.login(
                any(),
                any()
            )
        } returns Result.Error(AuthError.Auth.INVALID_CREDENTIALS)

        loginViewModel.events.test {
            loginViewModel.onAction(LoginAction.LoginClicked)
            awaitItem()

            loginViewModel.onAction(LoginAction.EmailChanged("new@test.com"))

            Assert.assertEquals(null, loginViewModel.state.error)

            expectNoEvents()
        }
    }
}