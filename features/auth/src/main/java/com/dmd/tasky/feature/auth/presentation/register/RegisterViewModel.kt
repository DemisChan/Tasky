package com.dmd.tasky.feature.auth.presentation.register

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dmd.tasky.core.domain.util.onError
import com.dmd.tasky.core.domain.util.onSuccess
import com.dmd.tasky.feature.auth.domain.AuthRepository
import com.dmd.tasky.feature.auth.domain.model.toUiMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {
    var state by mutableStateOf(RegisterUiState())
        private set

    fun onAction(action: RegisterAction) {
        when (action) {
            is RegisterAction.FullNameChanged -> {
                state = state.copy(fullName = action.fullName)
            }

            is RegisterAction.EmailChanged -> {
                state = state.copy(email = action.email)
            }

            is RegisterAction.PasswordChanged -> {
                state = state.copy(password = action.password)
            }

            is RegisterAction.PasswordVisibilityChanged -> {
                state = state.copy(passwordVisible = !state.passwordVisible)
            }

            is RegisterAction.RegisterClicked -> {
                register()
            }

            is RegisterAction.LoginClicked -> {
                TODO("Navigate to login")
            }
        }
    }

    private fun register() {
        viewModelScope.launch {
            state = state.copy(isLoading = true, error = null)

            Timber.d("📝 Starting registration...")
            Timber.d("   Full Name: '${state.fullName}' (length: ${state.fullName.length})")
            Timber.d("   Email: '${state.email}'")
            Timber.d("   Password: '${state.password}' (length: ${state.password.length})")

            authRepository.register(
                fullName = state.fullName,
                email = state.email,
                password = state.password
            )
                .onSuccess {
                    Timber.d("Registration successful in ViewModel!")
                    state = state.copy(registrationSuccess = true, isLoading = false)
                }
                .onError { error ->
                    Timber.e("Registration error: $error")
                    state = state.copy(error = error.toUiMessage(), isLoading = false)
                }
        }
    }
}

data class RegisterUiState(
    val fullName: String = "",
    val email: String = "",
    val password: String = "",
    var passwordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
    val registrationSuccess: Boolean = false
)
