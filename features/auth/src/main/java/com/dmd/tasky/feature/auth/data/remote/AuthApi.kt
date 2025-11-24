package com.dmd.tasky.features.auth.data.remote

import com.dmd.tasky.features.auth.data.remote.dto.LoginRequest
import com.dmd.tasky.features.auth.data.remote.dto.RegisterRequest
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): AuthResponse

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Unit

    @POST("auth/logout")
    suspend fun logout(): Unit
}
