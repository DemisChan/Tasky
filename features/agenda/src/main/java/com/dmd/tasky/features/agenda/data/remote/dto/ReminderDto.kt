package com.dmd.tasky.features.agenda.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class ReminderDto(
    val id: String,
    val title: String,
    val description: String,
    val remindAt: String,
    val updatedAt: String,
    val time: String,
)