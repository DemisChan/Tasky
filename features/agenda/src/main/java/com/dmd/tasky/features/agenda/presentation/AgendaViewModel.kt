package com.dmd.tasky.features.agenda.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dmd.tasky.core.domain.util.Result
import com.dmd.tasky.features.agenda.domain.model.AgendaItem
import com.dmd.tasky.features.agenda.domain.repository.AgendaError
import com.dmd.tasky.features.agenda.domain.usecase.GetAgendaUseCase
import com.dmd.tasky.features.agenda.domain.usecase.LogoutUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class AgendaViewModel @Inject constructor(
    private val getAgendaUseCase: GetAgendaUseCase,
    private val logoutUseCase: LogoutUseCase
) : ViewModel() {

    var state by mutableStateOf(AgendaState())
        private set

    private val eventChannel = Channel<AgendaEvent>()
    val events = eventChannel.receiveAsFlow()

    init {
        fetchAgenda(state.selectedDate)
    }

    fun onEvent(event: AgendaEvent) {
        when (event) {
            is AgendaEvent.OnDateSelected -> {
                state = state.copy(selectedDate = event.date)
                fetchAgenda(event.date)
            }

            is AgendaEvent.OnLogoutClicked -> {
                logout()
            }
        }
    }

    private fun fetchAgenda(date: LocalDate) {
        viewModelScope.launch {
            state = state.copy(isLoading = true, error = null)

            getAgendaUseCase(date).collect { result ->
                // Handle the Result type properly
                state = when (result) {
                    is Result.Success -> state.copy(
                        items = result.data,
                        isLoading = false,
                        error = null
                    )

                    is Result.Error -> state.copy(
                        items = emptyList(),
                        isLoading = false,
                        error = result.error
                    )
                }
            }
        }
    }
    private fun logout() {
        viewModelScope.launch {
            logoutUseCase()
            // Navigation handled by TaskyNavHost callback
        }
    }
}

data class AgendaState(
    val selectedDate: LocalDate = LocalDate.now(),
    val items: List<AgendaItem> = emptyList(),
    val isLoading: Boolean = false,
    val error: AgendaError? = null    // Added for error handling
)
