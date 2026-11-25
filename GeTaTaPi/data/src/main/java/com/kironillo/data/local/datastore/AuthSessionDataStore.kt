package com.kironillo.data.local.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthSessionDataStore @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {
    private companion object {
        val ACCESS_TOKEN = stringPreferencesKey("auth_access_token")
        val REFRESH_TOKEN = stringPreferencesKey("auth_refresh_token")
        val USER_ID = stringPreferencesKey("auth_user_id")
        val EXPIRES_AT = longPreferencesKey("auth_expires_at")
    }

    suspend fun saveSession(
        accessToken: String,
        refreshToken: String,
        userId: String?,
        expiresIn: Long?
    ) {
        dataStore.edit { preferences ->
            preferences[ACCESS_TOKEN] = accessToken
            preferences[REFRESH_TOKEN] = refreshToken
            if (userId != null) {
                preferences[USER_ID] = userId
            } else {
                preferences.remove(USER_ID)
            }
            if (expiresIn != null) {
                preferences[EXPIRES_AT] =
                    System.currentTimeMillis() + expiresIn * 1000
            }
        }
    }

    suspend fun getAccessToken(): String? =
        dataStore.data.first()[ACCESS_TOKEN]

    suspend fun getRefreshToken(): String? =
        dataStore.data.first()[REFRESH_TOKEN]

    suspend fun getUserId(): String? =
        dataStore.data.first()[USER_ID]

    suspend fun clearSession() {
        dataStore.edit { preferences ->
            preferences.remove(ACCESS_TOKEN)
            preferences.remove(REFRESH_TOKEN)
            preferences.remove(USER_ID)
            preferences.remove(EXPIRES_AT)
        }
    }
    suspend fun isTokenExpired(): Boolean {
        val expiresAt = dataStore.data.first()[EXPIRES_AT] ?: return true

        return System.currentTimeMillis() >= expiresAt - 60_000
    }
}
