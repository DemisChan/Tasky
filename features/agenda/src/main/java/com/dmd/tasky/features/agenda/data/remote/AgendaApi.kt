package com.dmd.tasky.features.agenda.data.remote

import com.dmd.tasky.features.agenda.data.remote.dto.AgendaResponseDto
import retrofit2.http.GET
import retrofit2.http.Query

interface AgendaApi {
    @GET("agenda")
    suspend fun getAgenda(@Query("time") timestamp: Long): AgendaResponseDto
}