package com.dmd.tasky.features.agenda.di

import com.dmd.tasky.core.data.token.TokenManager
import com.dmd.tasky.features.agenda.data.remote.AgendaApi
import com.dmd.tasky.features.agenda.data.repository.DefaultAgendaRepository
import com.dmd.tasky.features.agenda.domain.repository.AgendaRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AgendaModule {

    private const val S3_CLIENT = "s3"

    @Provides
    @Singleton
    fun provideAgendaApi(retrofit: Retrofit): AgendaApi {
        return retrofit.create(AgendaApi::class.java)
    }

    // Plain client for pre-signed S3 uploads: must NOT carry the x-api-key / Authorization headers
    @Provides
    @Singleton
    @Named(S3_CLIENT)
    fun provideS3OkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideAgendaRepository(
        api: AgendaApi,
        tokenManager: TokenManager,
        @Named(S3_CLIENT) okHttpClient: OkHttpClient
    ): AgendaRepository {
        return DefaultAgendaRepository(api, tokenManager, okHttpClient)
    }
}