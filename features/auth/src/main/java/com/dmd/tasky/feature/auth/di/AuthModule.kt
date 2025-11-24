package com.dmd.tasky.features.auth.di

import com.dmd.tasky.core.data.token.TokenManager
import com.dmd.tasky.features.auth.data.remote.AuthApi
import com.dmd.tasky.features.auth.data.repository.DefaultAuthRepository
import com.dmd.tasky.features.auth.domain.AuthRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AuthModule {

    @Provides
    @Singleton
    fun provideAuthApi(retrofit: Retrofit): AuthApi {
        return retrofit.create(AuthApi::class.java)
    }

    @Provides
    @Singleton
    fun provideAuthRepository(api: AuthApi, tokenManager: TokenManager): AuthRepository {
        return DefaultAuthRepository(api, tokenManager)
    }
}