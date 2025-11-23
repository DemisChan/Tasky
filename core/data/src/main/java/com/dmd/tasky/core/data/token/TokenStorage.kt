package com.dmd.tasky.core.data.token

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import timber.log.Timber
import com.dmd.tasky.core.data.security.CryptoManager
import android.util.Base64


private val Context.dataStore by preferencesDataStore("session_prefs")

class DataStoreTokenStorage @Inject constructor(
    private val context: Context,
    private val cryptoManager: CryptoManager
) : TokenManager {
    private companion object {
        val ACCESS_TOKEN = stringPreferencesKey("access_token")
        val REFRESH_TOKEN = stringPreferencesKey("refresh_token")
        val USER_ID = stringPreferencesKey("user_id")
        val USERNAME = stringPreferencesKey("username")
        val ACCESS_TOKEN_EXPIRATION_TIMESTAMP =
            longPreferencesKey("access_token_expiration_timestamp")
    }

    override suspend fun saveSession(sessionData: SessionData) {
        Timber.d("Saving session for user: ${sessionData.username}")

        val encryptedAccessToken = Base64.encodeToString(
            cryptoManager.encrypt(sessionData.accessToken.encodeToByteArray()),
            Base64.DEFAULT
        )
        val encryptedRefreshToken = Base64.encodeToString(
            cryptoManager.encrypt(sessionData.refreshToken.encodeToByteArray()),
            Base64.DEFAULT
        )

        context.dataStore.edit { preferences ->
            preferences[ACCESS_TOKEN] = encryptedAccessToken
            preferences[REFRESH_TOKEN] = encryptedRefreshToken
            preferences[USER_ID] = sessionData.userId
            preferences[USERNAME] = sessionData.username
            preferences[ACCESS_TOKEN_EXPIRATION_TIMESTAMP] =
                sessionData.accessTokenExpirationTimestamp
        }
    }

    override suspend fun getSession(): SessionData? {
        Timber.d("Retrieving session")
        val preferences = context.dataStore.data.first()
        val encryptedAccessToken = preferences[ACCESS_TOKEN] ?: return null
        val encryptedRefreshToken = preferences[REFRESH_TOKEN] ?: return null
        val userId = preferences[USER_ID] ?: return null
        val username = preferences[USERNAME] ?: return null
        val accessTokenExpirationTimestamp =
            preferences[ACCESS_TOKEN_EXPIRATION_TIMESTAMP] ?: return null

        return try {
            val accessToken = cryptoManager.decrypt(
                Base64.decode(encryptedAccessToken, Base64.DEFAULT)
            ).decodeToString()

            val refreshToken = cryptoManager.decrypt(
                Base64.decode(encryptedRefreshToken, Base64.DEFAULT)
            ).decodeToString()

            SessionData(
                accessToken = accessToken,
                refreshToken = refreshToken,
                userId = userId,
                username = username,
                accessTokenExpirationTimestamp = accessTokenExpirationTimestamp,
            )
        } catch (e: Exception) {
            Timber.e(e, "Failed to decrypt session data")
            null
        }
    }

    override suspend fun clearSession() {
        Timber.d("Clearing session")
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }

    override suspend fun isAuthenticated(): Flow<Boolean> {
        return context.dataStore.data.map { preferences ->
            val hasToken = preferences[ACCESS_TOKEN] !=null
            val isValid = isTokenValid()
            hasToken && isValid
        }
    }

    override suspend fun isTokenValid(): Boolean {
        val session = getSession() ?: return false
        val currentTime = System.currentTimeMillis()

        val safetyBufferMs = 5 * 60 * 1000 // 5 minutes in milliseconds

        return currentTime < session.accessTokenExpirationTimestamp - safetyBufferMs
    }
}