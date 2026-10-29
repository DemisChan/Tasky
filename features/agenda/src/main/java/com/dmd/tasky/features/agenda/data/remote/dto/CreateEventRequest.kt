package com.dmd.tasky.features.agenda.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class CreateEventRequest(
    val id: String,
    val title: String,
    val description: String,
    val from: String, // ISO 8601
    val to: String,
    val remindAt: String,
    val attendeeIds: List<String>,
    val photoKeys: List<String>, // e.g., ["photo0", "photo1"]
    val updatedAt: String
)
