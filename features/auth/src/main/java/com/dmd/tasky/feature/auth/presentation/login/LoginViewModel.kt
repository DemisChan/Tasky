package com.dmd.tasky.feature.auth.presentation.login

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dmd.tasky.feature.auth.domain.AuthRepository
import com.dmd.tasky.feature.auth.domain.model.LoginResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    var state by mutableStateOf(LoginUiState())
        private set

    fun onAction(action: LoginAction) {
        when (action) {
            is LoginAction.EmailChanged -> {
                state = state.copy(email = action.email)
            }
            is LoginAction.PasswordChanged -> {
                state = state.copy(password = action.password)
            }
            is LoginAction.LoginClicked -> {
                login()
            }
            is LoginAction.SignUpClicked -> {
            }
        }
    }

    private fun login() {
        viewModelScope.launch {
            state = state.copy(isLoading = true)
            Timber.d("Login started")
            val result = authRepository.login(state.email, state.password)

            state = state.copy(isLoading = false)
            when (result) {
                is LoginResult.Success -> {
                    Timber.d("Login success")
                }

                is LoginResult.Error -> {
                    Timber.e(result.message)
                    state = state.copy(error = result.message)
                }

                is LoginResult.InvalidCredentials -> {
                    Timber.d("Invalid credentials")
                    state = state.copy(error = "Invalid credentials")
                }
            }
        }
    }
}

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)