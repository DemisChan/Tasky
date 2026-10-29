package com.dmd.tasky.features.agenda.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class EventDto(
    val id: String,
    val title: String,
    val attendeeIds: List<String>,
    val description: String,
    val from: String,
    val to: String,
    val photoKeys: List<String>,
    val remindAt: String,
    val updatedAt: String,
)