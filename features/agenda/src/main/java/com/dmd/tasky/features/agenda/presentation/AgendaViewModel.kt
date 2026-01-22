package com.dmd.tasky.features.agenda.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dmd.tasky.core.domain.util.Result
import com.dmd.tasky.features.agenda.domain.usecase.GetAgendaUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class AgendaViewModel @Inject constructor(
    private val getAgendaUseCase: GetAgendaUseCase
) : ViewModel() {

    var state by mutableStateOf(AgendaState())
        private set

    init {
        fetchAgenda(state.selectedDate)
    }

    fun onEvent(event: AgendaEvent) {
        when (event) {
            is AgendaEvent.OnDateSelected -> {
                state = state.copy(selectedDate = event.date)
                fetchAgenda(event.date)
            }
            AgendaEvent.OnLogoutClicked -> {
                // TODO: Handle logout
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
}
