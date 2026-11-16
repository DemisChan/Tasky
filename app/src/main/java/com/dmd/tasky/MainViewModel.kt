package com.dmd.tasky

import androidx.compose.runtime.*
import androidx.lifecycle.*
import com.dmd.tasky.core.data.token.TokenManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * State for the main activity - handles auth checking on app startup.
 *
 * @param isCheckingAuth True while we're checking token validity. Splash screen stays visible.
 * @param isLoggedIn True if user has a valid session. Used for navigation after splash dismisses.
 */
data class MainState(
    val isCheckingAuth: Boolean = true,
    val isLoggedIn: Boolean = false
)

@HiltViewModel
class MainViewModel @Inject constructor(
    private val tokenManager: TokenManager
) : ViewModel() {

    var state by mutableStateOf(MainState())
        private set

    init {
        checkAuthStatus()
    }

    private fun checkAuthStatus() {
        viewModelScope.launch {
            val isValid = tokenManager.isTokenValid()
            state = state.copy(
                isCheckingAuth = false,
                isLoggedIn = isValid
            )
        }
    }
}
