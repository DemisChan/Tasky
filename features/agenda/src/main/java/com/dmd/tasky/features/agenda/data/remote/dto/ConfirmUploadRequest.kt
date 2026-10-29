package com.dmd.tasky.features.agenda.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class ConfirmUploadRequest(
    val uploadedKeys: List<String>  // Server-generated uploadKeys
)