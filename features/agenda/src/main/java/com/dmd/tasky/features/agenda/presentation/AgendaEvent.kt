package com.dmd.tasky.features.agenda.presentation

import java.time.LocalDate

sealed interface AgendaEvent {
    data class OnDateSelected(val date: LocalDate) : AgendaEvent
    data object OnLogoutClicked : AgendaEvent
}
