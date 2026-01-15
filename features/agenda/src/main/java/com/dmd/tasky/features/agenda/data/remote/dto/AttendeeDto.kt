package com.dmd.tasky.features.agenda.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class AttendeeDto(
    val email: String,
    val username: String,
    val userId: String,
    val eventId: String,
    val isGoing: Boolean,
    val remindAt: String
)