package com.dmd.tasky.core.data.di

import android.content.Context
import com.dmd.tasky.core.data.BuildConfig
import com.dmd.tasky.core.data.remote.ApiKeyInterceptor
import com.dmd.tasky.core.data.remote.AuthTokenInterceptor
import com.dmd.tasky.core.data.security.CryptoManager
import com.dmd.tasky.core.data.token.DataStoreTokenStorage
import com.dmd.tasky.core.data.token.TokenManager
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object CoreDataModule {

    @Provides
    @Singleton
    fun provideCryptoManager(): CryptoManager {
        return CryptoManager()
    }


    @Provides
    @Singleton
    fun providesSessionManager(
        @ApplicationContext context: Context,
        cryptoManager: CryptoManager
    ): TokenManager {
        return DataStoreTokenStorage(context, cryptoManager)
    }

    @Provides
    @Singleton
    fun provideApiKeyInterceptor(): ApiKeyInterceptor {
        return ApiKeyInterceptor()
    }

    @Provides
    @Singleton
    fun provideAuthTokenInterceptor(tokenManager: TokenManager): AuthTokenInterceptor {
        return AuthTokenInterceptor(tokenManager)
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        apiKeyInterceptor: ApiKeyInterceptor,
        authTokenInterceptor: AuthTokenInterceptor
    ): OkHttpClient {
        val logging = HttpLoggingInterceptor()
        logging.level = HttpLoggingInterceptor.Level.BODY
        return OkHttpClient.Builder()
            .addInterceptor(apiKeyInterceptor)
            .addInterceptor(authTokenInterceptor)
            .addInterceptor(logging)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        val networkJson = Json { ignoreUnknownKeys = true }
        return Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(networkJson.asConverterFactory("application/json".toMediaType()))
            .build()
    }
}