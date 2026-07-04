package com.dmd.tasky.features.agenda.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class CreateEventResponse(
    val event: EventDetailDto,
    val uploadUrls: List<PhotoUploadUrl>
)

@Serializable
data class PhotoUploadUrl(
    val photoKey: String,       // Your original key (e.g., "photo0")
    val uploadKey: String,      // Server-generated UUID
    val url: String            // S3 upload URL
)