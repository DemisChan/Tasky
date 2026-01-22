package com.dmd.tasky.features.agenda.presentation

import com.dmd.tasky.features.agenda.domain.model.AgendaItem
import com.dmd.tasky.features.agenda.domain.repository.AgendaError
import java.time.LocalDate

data class AgendaState(
    val selectedDate: LocalDate = LocalDate.now(),
    val items: List<AgendaItem> = emptyList(),
    val isLoading: Boolean = false,
    val error: AgendaError? = null    // Added for error handling
)
