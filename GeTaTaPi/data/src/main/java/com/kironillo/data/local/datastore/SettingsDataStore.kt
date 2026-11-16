package com.kironillo.data.local.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class SettingsDataStore @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {
    private companion object {
        val GEOLOCATION_ENABLED = booleanPreferencesKey("geolocation_enabled")
    }

    val isGeolocationEnabled: Flow<Boolean> =
        dataStore.data.map { preferences ->
            preferences[GEOLOCATION_ENABLED] ?: false
        }

    suspend fun setGeolocationEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[GEOLOCATION_ENABLED] = enabled
        }
    }
}
