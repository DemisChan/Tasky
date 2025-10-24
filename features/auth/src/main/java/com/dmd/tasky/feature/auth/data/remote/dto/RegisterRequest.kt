package com.dmd.tasky.feature.auth.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class RegisterRequest(
    val fullName: String,
    val email: String,
    val password: String
)
