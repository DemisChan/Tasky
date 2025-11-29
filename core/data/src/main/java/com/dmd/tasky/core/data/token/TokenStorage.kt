package com.dmd.tasky.core.data.token

import android.content.Context
import android.util.Base64
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.dmd.tasky.core.data.local.EncryptedTokenData
import com.dmd.tasky.core.data.security.CryptoManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import timber.log.Timber
import javax.inject.Inject


private val Context.dataStore by preferencesDataStore("session_prefs")

class DataStoreTokenStorage @Inject constructor(
    private val context: Context,
    private val cryptoManager: CryptoManager,
    private val json: Json
) : TokenManager {

    private companion object {
        val ENCRYPTED_TOKENS = stringPreferencesKey("encrypted_tokens")
        val USER_ID = stringPreferencesKey("user_id")
        val USERNAME = stringPreferencesKey("username")
        val ACCESS_TOKEN_EXPIRATION_TIMESTAMP =
            longPreferencesKey("access_token_expiration_timestamp")
    }

    override suspend fun saveSession(sessionData: SessionData) {
        Timber.d("Saving session for user: ${sessionData.username}")

        try {
            val tokenData = EncryptedTokenData(
                accessToken = sessionData.accessToken,
                refreshToken = sessionData.refreshToken
            )
            val tokenJson = json.encodeToString(tokenData)
            val encryptedTokens = Base64.encodeToString(
                cryptoManager.encrypt(tokenJson.encodeToByteArray()),
                Base64.DEFAULT
            )

            context.dataStore.edit { preferences ->
                preferences[ENCRYPTED_TOKENS] = encryptedTokens
                preferences[USER_ID] = sessionData.userId
                preferences[USERNAME] = sessionData.username
                preferences[ACCESS_TOKEN_EXPIRATION_TIMESTAMP] =
                    sessionData.accessTokenExpirationTimestamp
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to save session")
            throw e
        }
    }

    override suspend fun getSession(): SessionData? {
        Timber.d("Retrieving session")
        val preferences = context.dataStore.data.first()
        val encryptedTokens = preferences[ENCRYPTED_TOKENS] ?: return null
        val userId = preferences[USER_ID] ?: return null
        val username = preferences[USERNAME] ?: return null
        val accessTokenExpirationTimestamp =
            preferences[ACCESS_TOKEN_EXPIRATION_TIMESTAMP] ?: return null

        return try {
            val decryptedJson = cryptoManager.decrypt(
                Base64.decode(encryptedTokens, Base64.DEFAULT)
            ).decodeToString()

            val tokenData = json.decodeFromString<EncryptedTokenData>(decryptedJson)

            SessionData(
                accessToken = tokenData.accessToken,
                refreshToken = tokenData.refreshToken,
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
            val hasToken = preferences[ENCRYPTED_TOKENS] != null
            val isValid = isTokenValid()
            hasToken && isValid
        }
    }

    override suspend fun isTokenValid(): Boolean {
        val preferences = context.dataStore.data.first()
        val expirationTimestamp = preferences[ACCESS_TOKEN_EXPIRATION_TIMESTAMP] ?: return false
        val currentTime = System.currentTimeMillis()
        val safetyBufferMs = 5 * 60 * 1000 // 5 minutes in milliseconds
        return currentTime < expirationTimestamp - safetyBufferMs
    }
}