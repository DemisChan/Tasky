package com.dmd.tasky.features.agenda.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class EventDetailDto(
    val id: String,
    val title: String,
    val description: String,
    val from: String,
    val to: String,
    val remindAt: String,
    val hostId: String,
    val isUserEventCreator: Boolean,
    val attendees: List<AttendeeDto>,
    val photoKeys: List<PhotoDto>
)