package com.dmd.tasky.features.agenda.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class UpdateEventRequest(
    val title: String,
    val description: String,
    val from: String,
    val to: String,
    val remindAt: String,
    val attendeeIds: List<String>,
    val newPhotoKeys: List<String>,      // Photos to upload
    val deletedPhotoKeys: List<String>,  // Photos to remove
    val isGoing: Boolean,
    val updatedAt: String
)