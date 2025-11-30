package com.dmd.tasky.features.auth.data.remote

import kotlinx.serialization.Serializable

@Serializable
data class AuthResponse(
    val accessToken: String,
    val refreshToken: String,
    val username: String,
    val userId: String,
    val accessTokenExpirationTimestamp: Long,
)