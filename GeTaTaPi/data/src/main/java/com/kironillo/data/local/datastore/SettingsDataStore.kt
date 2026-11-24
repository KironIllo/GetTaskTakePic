package com.kironillo.data.local.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

@Singleton
class SettingsDataStore @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {
    private companion object {
        val GEOLOCATION_ENABLED = booleanPreferencesKey("geolocation_enabled")
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val THEME_MODE = stringPreferencesKey("theme_mode")
    }

    val isGeolocationEnabled: Flow<Boolean> = dataStore.data.map { it[GEOLOCATION_ENABLED] ?: false }
    val areNotificationsEnabled: Flow<Boolean> = dataStore.data.map { it[NOTIFICATIONS_ENABLED] ?: true }
    val themeMode: Flow<String> = dataStore.data.map { it[THEME_MODE] ?: "SYSTEM" }

    suspend fun setGeolocationEnabled(enabled: Boolean) { dataStore.edit { it[GEOLOCATION_ENABLED] = enabled } }
    suspend fun setNotificationsEnabled(enabled: Boolean) { dataStore.edit { it[NOTIFICATIONS_ENABLED] = enabled } }
    suspend fun setThemeMode(mode: String) { dataStore.edit { it[THEME_MODE] = mode } }
    suspend fun getGeolocationEnabled(): Boolean = isGeolocationEnabled.first()
    suspend fun getNotificationsEnabled(): Boolean = areNotificationsEnabled.first()
    suspend fun getThemeMode(): String = themeMode.first()
}
