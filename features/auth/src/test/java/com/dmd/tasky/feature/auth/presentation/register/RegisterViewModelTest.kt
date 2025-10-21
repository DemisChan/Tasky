package com.dmd.tasky.feature.auth.presentation.register

import FakeAuthRepository
import com.dmd.tasky.feature.auth.domain.model.RegisterResult
import com.dmd.tasky.feature.auth.presentation.MainCoroutineRule
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
        authRepository.registerResult = RegisterResult.Success
        registerViewModel.onAction(RegisterAction.FullNameChanged("Test"))
        registerViewModel.onAction(RegisterAction.FullNameChanged("Test"))
        registerViewModel.onAction(RegisterAction.EmailChanged("Test@test.com"))
        registerViewModel.onAction(RegisterAction.PasswordChanged("Test"))

        registerViewModel.onAction(RegisterAction.RegisterClicked)
        advanceUntilIdle()

        assertEquals(true, registerViewModel.state.registrationSuccess)
        assertEquals(false, registerViewModel.state.isLoading)
        assertEquals(null, registerViewModel.state.error)

    }


    @Test
    fun `RegisterClicked user already exists scenario`() = runTest {
        authRepository.registerResult = RegisterResult.UserAlreadyExists
        registerViewModel.onAction(RegisterAction.FullNameChanged("Test"))
        registerViewModel.onAction(RegisterAction.EmailChanged("Test@test.com"))
        registerViewModel.onAction(RegisterAction.PasswordChanged("Test"))

        registerViewModel.onAction(RegisterAction.RegisterClicked)
        advanceUntilIdle()
        assertEquals(false, registerViewModel.state.registrationSuccess)
        assertEquals(false, registerViewModel.state.isLoading)
        assertEquals(
            "This email is already registered. Try logging in?",
            registerViewModel.state.error
        )
    }

    @Test
    fun `RegisterClicked generic error scenario`() = runTest {

        authRepository.registerResult = RegisterResult.Error("Test error")

        registerViewModel.onAction(RegisterAction.FullNameChanged("Test"))
        registerViewModel.onAction(RegisterAction.EmailChanged("Test@test.com"))
        registerViewModel.onAction(RegisterAction.PasswordChanged("Test"))

        registerViewModel.onAction(RegisterAction.RegisterClicked)
        advanceUntilIdle()


        assertEquals(false, registerViewModel.state.registrationSuccess)
        assertEquals(false, registerViewModel.state.isLoading)
        assertEquals("Test error", registerViewModel.state.error)
    }

    @Test
    fun `RegisterClicked clears previous error`() = runTest {
        authRepository.registerResult = RegisterResult.Error("Test error")
        registerViewModel.onAction(RegisterAction.RegisterClicked)
        advanceUntilIdle()

        assertEquals("Test error", registerViewModel.state.error)

        authRepository.registerResult = RegisterResult.Success
        registerViewModel.onAction(RegisterAction.RegisterClicked)

        assertEquals(null, registerViewModel.state.error)

        advanceUntilIdle()
        assertEquals(true, registerViewModel.state.registrationSuccess)


    }
}