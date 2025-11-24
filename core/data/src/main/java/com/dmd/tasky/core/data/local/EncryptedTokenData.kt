package com.dmd.tasky.core.data.local

import kotlinx.serialization.Serializable


@Serializable
data class EncryptedTokenData(
    val accessToken: String,
    val refreshToken: String
)
