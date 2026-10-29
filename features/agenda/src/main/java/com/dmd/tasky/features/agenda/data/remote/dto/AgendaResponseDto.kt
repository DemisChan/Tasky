package com.dmd.tasky.features.agenda.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class AgendaResponseDto(
    val events: List<EventDto>,
    val tasks: List<TaskDto>,
    val reminders: List<ReminderDto>
)