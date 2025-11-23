package com.dmd.tasky.core.data.di

import android.content.Context
import com.dmd.tasky.core.data.token.DataStoreTokenStorage
import com.dmd.tasky.core.data.token.TokenManager
import com.dmd.tasky.core.data.security.CryptoManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ActivityContext
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object CoreDataModule {

    @Provides
    @Singleton
    fun providesSessionManager(
        @ApplicationContext context: Context,
        cryptoManager: CryptoManager
    ): TokenManager {
        return DataStoreTokenStorage(context, cryptoManager)
    }
}