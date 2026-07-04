package com.dmd.tasky.features.agenda.domain.model

data class Attendee(
    val email: String,
    val username: String,
    val userId: String,
    val eventId: String,
    val isGoing: Boolean,
    val remindAt: String
)
