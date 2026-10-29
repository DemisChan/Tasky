package com.dmd.tasky.features.auth.data.remote

import com.dmd.tasky.features.auth.data.remote.dto.*
import retrofit2.http.*

interface AuthApi {
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): AuthResponse

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Unit

    @POST("auth/logout")
    suspend fun logout(): Unit
}