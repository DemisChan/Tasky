package com.dmd.tasky

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dmd.tasky.core.data.token.TokenManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val tokenManager: TokenManager
) : ViewModel() {

    var isAuthenticated by mutableStateOf(false)
        private set

    init {
        viewModelScope.launch {
            isAuthenticated =  tokenManager.isTokenValid()
        }
    }
}
