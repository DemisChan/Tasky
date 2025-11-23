package com.dmd.tasky.feature.auth.presentation.login

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dmd.tasky.core.domain.util.onError
import com.dmd.tasky.core.domain.util.onSuccess
import com.dmd.tasky.feature.auth.domain.AuthRepository
import com.dmd.tasky.core.domain.util.UiText
import com.dmd.tasky.feature.auth.presentation.util.toUiText
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    var state by mutableStateOf(LoginUiState())
        private set

    private val eventChannel = Channel<LoginEvent>()
    val events = eventChannel.receiveAsFlow()

    fun onAction(action: LoginAction) {
        when (action) {
            is LoginAction.EmailChanged -> {
                state = state.copy(email = action.email, error = null)
            }

            is LoginAction.PasswordChanged -> {
                state = state.copy(password = action.password, error = null)
            }

            is LoginAction.PasswordVisibilityChanged -> {
                state = state.copy(passwordVisible = !state.passwordVisible)
            }

            is LoginAction.LoginClicked -> {
                login()
            }
            is LoginAction.SignUpClicked -> {}
        }
    }

    private fun login() {
        viewModelScope.launch {
            state = state.copy(isLoading = true)
            Timber.d("Login started")
            authRepository.login(state.email, state.password)
                .onSuccess {
                    Timber.d("Login successful")
                    state = state.copy(isLoading = false, error = null)
                    eventChannel.send(LoginEvent.Success)
                }
                .onError { error ->
                    Timber.e("Login failed: $error")
                    state = state.copy(isLoading = false, error = error.toUiText())
                    eventChannel.send(LoginEvent.Error(error.toUiText()))
                }
        }
    }
}

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    var passwordVisible: Boolean = false,
    val error: UiText? = null,
)