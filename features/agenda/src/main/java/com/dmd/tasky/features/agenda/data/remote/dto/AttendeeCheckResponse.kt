package com.dmd.tasky.features.agenda.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class AttendeeCheckResponse(
    val email: String,
    val fullName: String,
    val userId: String
)