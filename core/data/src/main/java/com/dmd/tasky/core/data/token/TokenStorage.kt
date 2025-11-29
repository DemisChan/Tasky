package com.dmd.tasky.core.data.token

import android.content.Context
import android.util.Base64
import androidx.datastore.preferences.core.edit
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
        val SESSION_DATA = stringPreferencesKey("session_data")
    }

    override suspend fun saveSession(sessionData: SessionData) {
        Timber.d("Saving session for user: ${sessionData.username}")

        try {
            val tokenData = EncryptedTokenData(
                accessToken = sessionData.accessToken,
                refreshToken = sessionData.refreshToken,
                userId = sessionData.userId,
                username = sessionData.username,
                accessTokenExpirationTimestamp = sessionData.accessTokenExpirationTimestamp
            )
            val tokenJson = json.encodeToString(tokenData)
            val encryptedData = Base64.encodeToString(
                cryptoManager.encrypt(tokenJson.encodeToByteArray()),
                Base64.DEFAULT
            )

            context.dataStore.edit { preferences ->
                preferences[SESSION_DATA] = encryptedData
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to save session")
            throw e
        }
    }

    override suspend fun getSession(): SessionData? {
        Timber.d("Retrieving session")
        val preferences = context.dataStore.data.first()
        val encryptedData = preferences[SESSION_DATA] ?: return null

        return try {
            val decryptedJson = cryptoManager.decrypt(
                Base64.decode(encryptedData, Base64.DEFAULT)
            ).decodeToString()

            val tokenData = json.decodeFromString<EncryptedTokenData>(decryptedJson)

            SessionData(
                accessToken = tokenData.accessToken,
                refreshToken = tokenData.refreshToken,
                userId = tokenData.userId,
                username = tokenData.username,
                accessTokenExpirationTimestamp = tokenData.accessTokenExpirationTimestamp,
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


    override suspend fun isTokenValid(): Boolean {
        val session = getSession() ?: return false
        val expirationTimestamp = session.accessTokenExpirationTimestamp
        val currentTime = System.currentTimeMillis()
        val safetyBufferMs = 5 * 60 * 1000 // 5 minutes
        return currentTime < expirationTimestamp - safetyBufferMs
    }

    override fun isAuthenticated(): Flow<Boolean> {
        return context.dataStore.data.map { preferences ->
            val encryptedData = preferences[SESSION_DATA]
            if (encryptedData == null) {
                false
            } else {
                try {
                    val decryptedJson = cryptoManager.decrypt(
                        Base64.decode(encryptedData, Base64.DEFAULT)
                    ).decodeToString()

                    val tokenData = json.decodeFromString<EncryptedTokenData>(decryptedJson)
                    val safetyBufferMs = 5 * 60 * 1000
                    tokenData.accessTokenExpirationTimestamp - safetyBufferMs > System.currentTimeMillis()
                } catch (e: Exception) {
                    Timber.e(e, "Failed to validate session")
                    false
                }
            }
        }
    }
}