package com.dmd.tasky.features.auth.presentation.register

import FakeAuthRepository
import app.cash.turbine.test
import com.dmd.tasky.core.domain.util.Result
import com.dmd.tasky.core.domain.util.UiText
import com.dmd.tasky.features.auth.R
import com.dmd.tasky.features.auth.domain.model.AuthError
import com.dmd.tasky.features.auth.presentation.MainCoroutineRule
import com.dmd.tasky.features.auth.presentation.util.toUiText
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class RegisterViewModelTest {

    // This is with Fakes

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    private lateinit var registerViewModel: RegisterViewModel
    private lateinit var authRepository: FakeAuthRepository

    @Before
    fun setUp() {
        authRepository = FakeAuthRepository()
        registerViewModel = RegisterViewModel(authRepository)
    }

    @Test
    fun `Initial state verification`() {
        assert(registerViewModel.state == RegisterUiState())
    }

    @Test
    fun `onAction FullNameChanged updates state`() {
        registerViewModel.onAction(RegisterAction.FullNameChanged("Test"))
        assert(registerViewModel.state.fullName == "Test")
    }

    @Test
    fun `onAction EmailChanged updates state`() {
        registerViewModel.onAction(RegisterAction.EmailChanged("Test@test.com"))
        assert(registerViewModel.state.email == "Test@test.com")
    }

    @Test
    fun `onAction PasswordChanged updates state`() {
        registerViewModel.onAction(RegisterAction.PasswordChanged("Test"))
        assert(registerViewModel.state.password == "Test")

    }

    @Test
    fun `RegisterClicked success scenario`() = runTest {
        authRepository.registerResult = Result.Success(Unit)

        registerViewModel.onAction(RegisterAction.FullNameChanged("Test"))
        registerViewModel.onAction(RegisterAction.EmailChanged("Test@test.com"))
        registerViewModel.onAction(RegisterAction.PasswordChanged("Test"))
        registerViewModel.events.test {
            registerViewModel.onAction(RegisterAction.RegisterClicked)

            assertEquals(true, registerViewModel.state.isLoading)
            assertEquals(null, registerViewModel.state.error)
            assertEquals(
                "A success event should be emitted",
                RegisterEvent.Success,
                awaitItem()
            )
        }
    }


    @Test
    fun `RegisterClicked user already exists scenario`() = runTest {
        authRepository.registerResult = Result.Error(AuthError.Auth.USER_ALREADY_EXISTS)
        registerViewModel.onAction(RegisterAction.FullNameChanged("Test"))
        registerViewModel.onAction(RegisterAction.EmailChanged("Test@test.com"))
        registerViewModel.onAction(RegisterAction.PasswordChanged("Test"))

        registerViewModel.events.test {
            registerViewModel.onAction(RegisterAction.RegisterClicked)
            assertEquals(true, registerViewModel.state.isLoading)
            assertEquals(
                "An error event should be emitted",
                RegisterEvent.Error(AuthError.Auth.USER_ALREADY_EXISTS.toUiText()),
                awaitItem()
            )
        }
        advanceUntilIdle()
        assertEquals(false, registerViewModel.state.isLoading)
        assertEquals(AuthError.Auth.USER_ALREADY_EXISTS.toUiText(), registerViewModel.state.error)

    }

    @Test
    fun `RegisterClicked generic error scenario`() = runTest {

        authRepository.registerResult = Result.Error(AuthError.Network.UNKNOWN)

        registerViewModel.onAction(RegisterAction.FullNameChanged("Test"))
        registerViewModel.onAction(RegisterAction.EmailChanged("Test@test.com"))
        registerViewModel.onAction(RegisterAction.PasswordChanged("Test"))


        registerViewModel.events.test {
            registerViewModel.onAction(RegisterAction.RegisterClicked)
            assertEquals(true, registerViewModel.state.isLoading)
            assertEquals(
                "An error event should be emitted",
                RegisterEvent.Error(AuthError.Network.UNKNOWN.toUiText()),
                awaitItem()
            )
            advanceUntilIdle()
            assertEquals(false, registerViewModel.state.isLoading)
            assertEquals(AuthError.Network.UNKNOWN.toUiText(), registerViewModel.state.error)
            expectNoEvents()
        }
    }

    @Test
    fun `RegisterClicked clears previous error`() = runTest {
        authRepository.registerResult = Result.Error(AuthError.Network.UNKNOWN)


        registerViewModel.events.test {
            registerViewModel.onAction(RegisterAction.RegisterClicked)
            advanceUntilIdle()
            assertEquals(false, registerViewModel.state.isLoading)
            assertEquals(
                "An error event should be emitted",
                RegisterEvent.Error(AuthError.Network.UNKNOWN.toUiText()),
                awaitItem()
            )
            authRepository.registerResult = Result.Success(Unit)
            registerViewModel.onAction(RegisterAction.RegisterClicked)
            advanceUntilIdle()
            assertEquals(
                "A success event should be emitted",
                RegisterEvent.Success,
                awaitItem()
            )
            assertEquals(null, registerViewModel.state.error)
            assertEquals(false, registerViewModel.state.isLoading)
        }
    }

    @Test
    fun `RegisterClicked validation failed scenario`() = runTest {
        authRepository.registerResult = Result.Error(AuthError.Auth.VALIDATION_FAILED)

        registerViewModel.events.test {
            registerViewModel.onAction(RegisterAction.FullNameChanged("Jo"))
            registerViewModel.onAction(RegisterAction.EmailChanged("test@test.com"))
            registerViewModel.onAction(RegisterAction.PasswordChanged("short"))
            registerViewModel.onAction(RegisterAction.RegisterClicked)
            advanceUntilIdle()

            assertEquals(
                "An error event should be emitted",
                RegisterEvent.Error(AuthError.Auth.VALIDATION_FAILED.toUiText()),
                awaitItem()
            )
            assertEquals(
                UiText.StringResource(R.string.error_validation_failed),
                registerViewModel.state.error
            )
        }
    }
}